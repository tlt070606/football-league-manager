package league.ui;

import league.model.Team;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 球队编辑对话框
 * 用于添加或编辑球队信息：名称、教练、主场、球员名单
 */
public class TeamDialog extends JDialog {
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
    }

    /** 保存操作：校验 → 构建 Team 对象 → 关闭对话框 */
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

    /** 用户是否点击了"保存" */
    public boolean isConfirmed() {
        return confirmed;
    }

    /** 获取编辑后的 Team 对象 */
    public Team getTeam() {
        return team;
    }
}
