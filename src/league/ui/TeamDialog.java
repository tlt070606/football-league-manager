package league.ui;

import league.model.Team;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 球队编辑对话框
 * 用于添加或编辑球队信息：队徽、名称、教练、主场、球员名单
 */
public class TeamDialog extends JDialog {
    private static final String LOGO_DIR = "assets" + File.separator + "logos";

    private JLabel logoPreview;          // 队徽预览（80×80）
    private String selectedLogoPath;     // 用户选中的队徽源文件路径

    private JTextField nameField;
    private JTextField coachField;
    private JTextField stadiumField;
    private JTextArea playersArea;
    private boolean confirmed = false;
    private Team team;

    /**
     * @param owner    父窗口
     * @param existing 要编辑的球队（为 null 时表示添加模式）
     */
    public TeamDialog(JFrame owner, Team existing) {
        super(owner, existing == null ? "添加球队" : "编辑球队", true);
        this.team = existing;
        initUI();
        if (existing != null) {
            loadTeamData(existing);
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== 队徽选择行 =====
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        logoPreview = new JLabel("点击选择队徽", SwingConstants.CENTER);
        logoPreview.setPreferredSize(new Dimension(80, 80));
        logoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        logoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        // 编辑模式下加载已有队徽
        if (team != null && team.getLogo() != null && !team.getLogo().isEmpty()) {
            File logoFile = new File(team.getLogo());
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(logoFile.getAbsolutePath())
                        .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
                logoPreview.setIcon(icon);
                logoPreview.setText("");
            }
        }
        logoPanel.add(logoPreview);

        JButton chooseLogoBtn = new JButton("选择队徽");
        chooseLogoBtn.addActionListener(e -> chooseLogo());
        logoPanel.add(chooseLogoBtn);

        JButton clearLogoBtn = new JButton("清除");
        clearLogoBtn.addActionListener(e -> {
            selectedLogoPath = null;
            logoPreview.setIcon(null);
            logoPreview.setText("点击选择队徽");
        });
        logoPanel.add(clearLogoBtn);

        mainPanel.add(logoPanel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 球队名称
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("球队名称*:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);
        gbc.weightx = 0;

        // 主教练
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("主教练:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        coachField = new JTextField(20);
        formPanel.add(coachField, gbc);
        gbc.weightx = 0;

        // 主场
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("主场:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        stadiumField = new JTextField(20);
        formPanel.add(stadiumField, gbc);
        gbc.weightx = 0;

        // 球员名单
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("球员名单:"), gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        playersArea = new JTextArea(5, 20);
        playersArea.setToolTipText("每行一个球员姓名");
        JScrollPane scrollPane = new JScrollPane(playersArea);
        formPanel.add(scrollPane, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        // 按 Enter 触发保存
        getRootPane().setDefaultButton(saveBtn);
    }

    /** 将已有球队数据加载到表单 */
    private void loadTeamData(Team t) {
        nameField.setText(t.getName());
        coachField.setText(t.getCoach());
        stadiumField.setText(t.getHomeStadium());
        playersArea.setText(String.join("\n", t.getPlayers()));
        // 已有队徽
        if (t.getLogo() != null && !t.getLogo().isEmpty()) {
            File logoFile = new File(t.getLogo());
            if (logoFile.exists()) {
                selectedLogoPath = logoFile.getAbsolutePath();
                ImageIcon icon = new ImageIcon(new ImageIcon(logoFile.getAbsolutePath())
                        .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
                logoPreview.setIcon(icon);
                logoPreview.setText("");
            }
        }
    }

    /** 打开文件选择器选取队徽图片 */
    private void chooseLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择队徽图片");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件 (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        // 从 assets/logos 目录开始浏览（如果存在）
        File logoDir = new File(LOGO_DIR);
        if (logoDir.exists()) {
            chooser.setCurrentDirectory(logoDir);
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            selectedLogoPath = file.getAbsolutePath();
            // 预览
            ImageIcon icon = new ImageIcon(new ImageIcon(file.getAbsolutePath())
                    .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            logoPreview.setIcon(icon);
            logoPreview.setText("");
        }
    }

    /** 保存操作：校验 → 复制队徽 → 构建 Team 对象 → 关闭对话框 */
    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "球队名称不能为空！", "输入错误", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (team == null) {
            team = new Team();
        }
        team.setName(name);
        team.setCoach(coachField.getText().trim());
        team.setHomeStadium(stadiumField.getText().trim());

        // 队徽：复制到 assets/logos/{teamId}.png
        if (selectedLogoPath != null) {
            try {
                File srcFile = new File(selectedLogoPath);
                if (srcFile.exists()) {
                    File logoDir = new File(LOGO_DIR);
                    if (!logoDir.exists()) {
                        logoDir.mkdirs();
                    }
                    String ext = selectedLogoPath.substring(selectedLogoPath.lastIndexOf('.'));
                    File destFile = new File(logoDir, team.getId() + ext.toLowerCase());
                    Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    // 存储相对于项目根目录的路径
                    team.setLogo(LOGO_DIR + File.separator + team.getId() + ext.toLowerCase());
                }
            } catch (IOException e) {
                System.err.println("复制队徽文件失败: " + e.getMessage());
            }
        }
        // 如果 selectedLogoPath 为 null（用户清除了队徽），保持原有 logo 不变
        // 注意：这里无法区分"没选"和"清除"，清除时点击"清除"按钮会将 selectedLogoPath 设为 null

        // 球员名单：按行分割，过滤空行
        String[] lines = playersArea.getText().split("\n");
        List<String> players = new ArrayList<>();
        for (String line : lines) {
            String p = line.trim();
            if (!p.isEmpty()) {
                players.add(p);
            }
        }
        team.setPlayers(players);

        confirmed = true;
        dispose();
    }

    /**
     * 用户是否点击了"保存"
     * @return true 表示用户确认保存
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * 获取编辑后的 Team 对象
     * @return 编辑后的 Team（保存前为 null）
     */
    public Team getTeam() {
        return team;
    }
}
