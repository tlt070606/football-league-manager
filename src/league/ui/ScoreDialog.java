package league.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 比分录入对话框
 * 显示对阵双方，用 JSpinner 输入比分
 */
public class ScoreDialog extends JDialog {
    private JSpinner homeScoreSpinner;
    private JSpinner awayScoreSpinner;
    private boolean confirmed = false;

    /**
     * @param owner    父窗口
     * @param homeName 主队名称
     * @param awayName 客队名称
     * @param homeScore 已有主队比分（-1 表示未录入）
     * @param awayScore 已有客队比分
     */
    public ScoreDialog(JFrame owner, String homeName, String awayName,
                       int homeScore, int awayScore) {
        super(owner, "录入比分", true);
        initUI(homeName, awayName, homeScore, awayScore);
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI(String homeName, String awayName, int hs, int as) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 中部分数输入
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);

        // 主队标签
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel homeLabel = new JLabel(homeName);
        homeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        homeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(homeLabel, gbc);

        // VS 标签
        gbc.gridx = 1; gbc.gridy = 0;
        JLabel vsLabel = new JLabel("  VS  ");
        vsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        vsLabel.setForeground(Color.GRAY);
        centerPanel.add(vsLabel, gbc);

        // 客队标签
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel awayLabel = new JLabel(awayName);
        awayLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        awayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(awayLabel, gbc);

        // 主队比分输入
        gbc.gridx = 0; gbc.gridy = 1;
        SpinnerNumberModel homeModel = new SpinnerNumberModel(
                hs >= 0 ? hs : 0, 0, 99, 1);
        homeScoreSpinner = new JSpinner(homeModel);
        homeScoreSpinner.setFont(new Font("Arial", Font.BOLD, 20));
        JPanel homeSpinPanel = new JPanel();
        homeSpinPanel.add(homeScoreSpinner);
        centerPanel.add(homeSpinPanel, gbc);

        // 冒号
        gbc.gridx = 1; gbc.gridy = 1;
        JLabel colonLabel = new JLabel("  :  ");
        colonLabel.setFont(new Font("Arial", Font.BOLD, 20));
        centerPanel.add(colonLabel, gbc);

        // 客队比分输入
        gbc.gridx = 2; gbc.gridy = 1;
        SpinnerNumberModel awayModel = new SpinnerNumberModel(
                as >= 0 ? as : 0, 0, 99, 1);
        awayScoreSpinner = new JSpinner(awayModel);
        awayScoreSpinner.setFont(new Font("Arial", Font.BOLD, 20));
        JPanel awaySpinPanel = new JPanel();
        awaySpinPanel.add(awayScoreSpinner);
        centerPanel.add(awaySpinPanel, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        JButton confirmBtn = new JButton("确认录入");
        JButton cancelBtn = new JButton("取消");

        confirmBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        getRootPane().setDefaultButton(confirmBtn);
    }

    public boolean isConfirmed() { return confirmed; }
    public int getHomeScore() { return (int) homeScoreSpinner.getValue(); }
    public int getAwayScore() { return (int) awayScoreSpinner.getValue(); }
}
