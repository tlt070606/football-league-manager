package league.ui;

import league.model.Match;
import league.service.LeagueManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * 比赛管理面板
 * 功能：按轮次查看赛程、生成赛程、录入比分
 */
public class MatchPanel extends JPanel {
    private LeagueManager manager;
    private JTable table;
    private MatchTableModel tableModel;
    private JComboBox<String> roundCombo;
    private JLabel statusLabel;
    private JButton generateBtn;
    private JButton scoreBtn;

    public MatchPanel() {
        this.manager = LeagueManager.getInstance();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== 顶部：轮次选择 + 按钮 =====
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("选择轮次:"));
        roundCombo = new JComboBox<>();
        roundCombo.setPreferredSize(new Dimension(80, 25));
        roundCombo.addActionListener(e -> refreshRound());
        leftPanel.add(roundCombo);

        generateBtn = new JButton("生成赛程");
        generateBtn.addActionListener(e -> generateSchedule());
        leftPanel.add(generateBtn);

        topPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scoreBtn = new JButton("录入比分");
        scoreBtn.addActionListener(e -> recordScore());
        rightPanel.add(scoreBtn);
        topPanel.add(rightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== 中部：比赛表格 =====
        tableModel = new MatchTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        // 列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // 轮次
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // 主队
        table.getColumnModel().getColumn(2).setPreferredWidth(60);   // 比分
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // 客队
        table.getColumnModel().getColumn(4).setPreferredWidth(140);  // 场地
        table.getColumnModel().getColumn(5).setPreferredWidth(65);   // 状态

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ===== 底部状态栏 =====
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        add(statusLabel, BorderLayout.SOUTH);

        // 双击录入比分
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    recordScore();
                }
            }
        });

        updateRoundCombo();
    }

    /** 外部调用刷新 */
    public void refreshData() {
        updateRoundCombo();
        refreshRound();
        updateButtons();
    }

    private void updateRoundCombo() {
        roundCombo.removeAllItems();
        int totalRounds = manager.getTotalRounds();
        if (totalRounds == 0) {
            roundCombo.addItem("无赛程");
        } else {
            for (int i = 1; i <= totalRounds; i++) {
                roundCombo.addItem("第" + i + "轮");
            }
        }
    }

    private void refreshRound() {
        int idx = roundCombo.getSelectedIndex();
        if (idx < 0 || manager.getTotalRounds() == 0) {
            tableModel.setMatches(List.of(), manager);
            updateStatus(0, 0);
            return;
        }
        int round = idx + 1;
        List<Match> matches = manager.getMatchesByRound(round);
        tableModel.setMatches(matches, manager);

        long played = matches.stream().filter(Match::isPlayed).count();
        updateStatus(round, (int) played);
    }

    private void updateStatus(int round, int played) {
        int total = manager.getAllMatches().size();
        long allPlayed = manager.getAllMatches().stream().filter(Match::isPlayed).count();
        statusLabel.setText(String.format(
                "  第 %d 轮 | 本轮 %d/%d 场已完成 | 总计 %d/%d 场已完成",
                round, played, tableModel.getRowCount(), allPlayed, total));
    }

    private void updateButtons() {
        boolean hasTeams = manager.getTeamCount() >= 2;
        boolean hasSchedule = manager.hasSchedule();
        generateBtn.setEnabled(hasTeams);
        scoreBtn.setEnabled(hasSchedule);
    }

    private void generateSchedule() {
        int teamCount = manager.getTeamCount();
        if (teamCount < 2) {
            JOptionPane.showMessageDialog(this,
                    "至少需要 2 支球队才能生成赛程！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (manager.hasSchedule()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "已有赛程数据。重新生成将清除当前赛程和所有比分记录。\n确认重新生成？",
                    "确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION) return;
        }

        try {
            manager.generateSchedule();
            updateRoundCombo();
            refreshRound();
            updateButtons();

            int rounds = manager.getTotalRounds();
            int matches = manager.getAllMatches().size();
            JOptionPane.showMessageDialog(this,
                    String.format("赛程生成成功！\n%d 支球队，双循环共 %d 轮 %d 场比赛。",
                            teamCount, rounds, matches),
                    "成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordScore() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一场比赛", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Match match = tableModel.getMatchAt(row);
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

        String homeName = manager.getTeam(match.getHomeTeamId()).getName();
        String awayName = manager.getTeam(match.getAwayTeamId()).getName();

        ScoreDialog dialog = new ScoreDialog(parent, homeName, awayName,
                match.getHomeScore(), match.getAwayScore());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                manager.recordMatchResult(match.getId(),
                        dialog.getHomeScore(), dialog.getAwayScore());
                refreshRound();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

/** 比赛表格数据模型 */
class MatchTableModel extends AbstractTableModel {
    private final String[] columns = {"轮次", "主队", "比分", "客队", "场地", "状态"};
    private List<Match> matches = List.of();
    private LeagueManager manager;

    public void setMatches(List<Match> matches, LeagueManager manager) {
        this.matches = matches;
        this.manager = manager;
        fireTableDataChanged();
    }

    public Match getMatchAt(int row) {
        return matches.get(row);
    }

    @Override
    public int getRowCount() { return matches.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Match m = matches.get(row);
        switch (col) {
            case 0: return m.getRound();
            case 1: return getTeamName(m.getHomeTeamId());
            case 2: return m.getScoreDisplay();
            case 3: return getTeamName(m.getAwayTeamId());
            case 4: return m.getStadium();
            case 5: return m.isPlayed() ? "已赛" : "未赛";
            default: return "";
        }
    }

    private String getTeamName(String id) {
        if (manager == null) return id;
        var team = manager.getTeam(id);
        return team != null ? team.getName() : id;
    }
}
