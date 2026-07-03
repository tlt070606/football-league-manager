package league.ui;

import league.model.Standing;
import league.service.LeagueManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * 积分榜面板
 * 功能：显示排名表格 + 积分柱状图
 */
public class StandingPanel extends JPanel {
    private LeagueManager manager;
    private JTable table;
    private StandingTableModel tableModel;
    private BarChartPanel barChart;
    private JLabel statusLabel;
    private JComboBox<String> groupFilter;
    private String selectedGroup = "全部";  // 当前筛选的小组

    /** 创建积分榜面板，包含排名表格、柱状图和状态栏 */
    public StandingPanel() {
        this.manager = LeagueManager.getInstance();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== 顶部：标题 + 小组筛选 + 刷新按钮 =====
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("世界杯积分榜");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 小组筛选
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.add(new JLabel("小组:"));
        groupFilter = new JComboBox<>(new String[]{"全部", "A", "B", "C", "D", "E", "F", "G", "H"});
        groupFilter.addActionListener(e -> {
            selectedGroup = (String) groupFilter.getSelectedItem();
            refreshData();
        });
        centerPanel.add(groupFilter);
        topPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("刷新积分榜");
        refreshBtn.addActionListener(e -> refreshData());
        btnPanel.add(refreshBtn);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== 中部：积分表格（占据上半部分） =====
        tableModel = new StandingTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new StandingCellRenderer());

        // 列宽设置
        int[] widths = {40, 120, 45, 35, 35, 35, 45, 45, 55, 45};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(600, 200));
        add(tableScroll, BorderLayout.CENTER);

        // ===== 下部：柱状图 + 状态栏 =====
        // BorderLayout.SOUTH 只能容纳一个组件，用包裹面板容纳柱状图和状态栏
        JPanel southWrapper = new JPanel(new BorderLayout());
        barChart = new BarChartPanel();
        southWrapper.add(barChart, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        southWrapper.add(statusLabel, BorderLayout.SOUTH);

        add(southWrapper, BorderLayout.SOUTH);

        refreshData();
    }

    /** 刷新积分数据（外部调用） */
    public void refreshData() {
        manager.updateStandings();
        List<Standing> allStandings = manager.getStandings();

        // 按小组筛选
        List<Standing> filtered;
        if ("全部".equals(selectedGroup)) {
            filtered = allStandings;
        } else {
            filtered = new java.util.ArrayList<>();
            for (Standing s : allStandings) {
                var team = manager.getTeam(s.getTeamId());
                if (team != null && selectedGroup.equals(team.getGroup())) {
                    filtered.add(s);
                }
            }
        }

        tableModel.setStandings(filtered);
        barChart.updateData(filtered);

        int totalMatches = manager.getAllMatches().size();
        long played = manager.getAllMatches().stream()
                .filter(m -> m.isPlayed()).count();
        String groupLabel = "全部".equals(selectedGroup) ? "全部" : selectedGroup + "组";
        statusLabel.setText(String.format(
                "  %s | %d 支球队 | %d 场比赛 | 已完成 %d 场",
                groupLabel, filtered.size(), totalMatches, played));
    }
}

/** 积分榜表格数据模型 */
class StandingTableModel extends AbstractTableModel {
    private final String[] columns = {
            "排名", "球队", "场次", "胜", "平", "负", "进球", "失球", "净胜球", "积分"
    };
    private List<Standing> standings = List.of();

    public void setStandings(List<Standing> standings) {
        this.standings = standings;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() { return standings.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Standing s = standings.get(row);
        switch (col) {
            case 0: return row + 1;  // 排名
            case 1: return s.getTeamName();
            case 2: return s.getPlayed();
            case 3: return s.getWon();
            case 4: return s.getDrawn();
            case 5: return s.getLost();
            case 6: return s.getGoalsFor();
            case 7: return s.getGoalsAgainst();
            case 8: return s.getGoalDiff();
            case 9: return s.getPoints();
            default: return "";
        }
    }
}

/** 自定义单元格渲染器 — 前3名特殊背景色 */
class StandingCellRenderer extends DefaultTableCellRenderer {
    private static final Color GOLD_BG = new Color(255, 255, 230);
    private static final Color SILVER_BG = new Color(245, 245, 245);
    private static final Color BRONZE_BG = new Color(255, 240, 230);
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            if (row == 0) {
                c.setBackground(GOLD_BG);
                if (column == 0) setFont(getFont().deriveFont(Font.BOLD));
            } else if (row == 1) {
                c.setBackground(SILVER_BG);
            } else if (row == 2) {
                c.setBackground(BRONZE_BG);
            } else {
                c.setBackground(Color.WHITE);
            }
        }

        // 积分列加粗
        if (column == 9) {
            setFont(getFont().deriveFont(Font.BOLD));
        }

        setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
        return c;
    }
}
