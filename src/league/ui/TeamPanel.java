package league.ui;

import league.model.Team;
import league.service.LeagueManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * 球队管理面板
 * 功能：查看球队列表、添加、编辑、删除、搜索球队
 */
public class TeamPanel extends JPanel {
    private LeagueManager manager;
    private JTable table;
    private TeamTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;

    /** 创建球队管理面板，初始化布局、表格和按钮事件 */
    public TeamPanel() {
        this.manager = LeagueManager.getInstance();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== 顶部：搜索栏 + 按钮 =====
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        // 搜索区域
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("搜索:"));
        searchField = new JTextField(15);
        searchField.setToolTipText("输入球队名称或教练姓名搜索");
        JButton searchBtn = new JButton("搜索");
        JButton resetBtn = new JButton("显示全部");

        searchBtn.addActionListener(e -> doSearch());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            refreshTable(manager.getAllTeams());
        });
        // 回车触发搜索
        searchField.addActionListener(e -> doSearch());

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // 操作按钮
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("添加球队");
        JButton editBtn = new JButton("编辑球队");
        JButton deleteBtn = new JButton("删除球队");

        addBtn.addActionListener(e -> addTeam());
        editBtn.addActionListener(e -> editTeam());
        deleteBtn.addActionListener(e -> deleteTeam());

        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        topPanel.add(actionPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // ===== 中部：球队表格 =====
        tableModel = new TeamTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        // 设置列宽: 队徽 | 编号 | 名称 | 小组 | 教练 | 主场 | 人数
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // 队徽
        table.getColumnModel().getColumn(1).setPreferredWidth(60);   // 编号
        table.getColumnModel().getColumn(2).setPreferredWidth(110);  // 名称
        table.getColumnModel().getColumn(3).setPreferredWidth(40);   // 小组
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // 教练
        table.getColumnModel().getColumn(5).setPreferredWidth(130);  // 主场
        table.getColumnModel().getColumn(6).setPreferredWidth(45);   // 人数

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ===== 底部：状态栏 =====
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        add(statusLabel, BorderLayout.SOUTH);

        // 双击表格行可编辑
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editTeam();
                }
            }
        });

        refreshTable(manager.getAllTeams());
    }

    /** 刷新外部数据（切换标签页时调用） */
    public void refreshData() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            refreshTable(manager.getAllTeams());
        } else {
            refreshTable(manager.searchTeams(kw));
        }
    }

    private void refreshTable(List<Team> teams) {
        tableModel.setTeams(teams);
        updateStatus(teams.size());
    }

    private void updateStatus(int count) {
        statusLabel.setText(String.format("  共 %d 支球队", count));
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        List<Team> result = manager.searchTeams(keyword);
        refreshTable(result);
        if (keyword.isEmpty()) {
            statusLabel.setText(String.format("  共 %d 支球队", manager.getTeamCount()));
        } else {
            statusLabel.setText(String.format("  搜索 \"%s\" — 找到 %d 条结果", keyword, result.size()));
        }
    }

    private void addTeam() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TeamDialog dialog = new TeamDialog(parent, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                manager.addTeam(dialog.getTeam());
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "球队添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editTeam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一支球队", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Team selected = tableModel.getTeamAt(row);
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TeamDialog dialog = new TeamDialog(parent, selected);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                manager.updateTeam(dialog.getTeam());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTeam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一支球队", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Team selected = tableModel.getTeamAt(row);
        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除球队 \"" + selected.getName() + "\" 吗？\n"
                        + "注意：已有比赛记录的球队无法删除。",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                manager.deleteTeam(selected.getId());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "删除失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

/** 球队表格数据模型 */
class TeamTableModel extends AbstractTableModel {
    private final String[] columns = {"队徽", "编号", "球队名称", "小组", "主教练", "主场", "人数"};
    private List<Team> teams = List.of();

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        fireTableDataChanged();
    }

    public Team getTeamAt(int row) {
        return teams.get(row);
    }

    @Override
    public int getRowCount() { return teams.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Class<?> getColumnClass(int col) {
        // 队徽列返回 ImageIcon 类型，让 JTable 自动渲染图标
        return col == 0 ? ImageIcon.class : String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Team t = teams.get(row);
        switch (col) {
            case 0: // 队徽：加载为小图标
                if (t.getLogo() != null && !t.getLogo().isEmpty()) {
                    java.io.File f = new java.io.File(t.getLogo());
                    if (f.exists()) {
                        return new ImageIcon(new ImageIcon(f.getAbsolutePath())
                                .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                    }
                }
                return null;
            case 1: return t.getId();
            case 2: return t.getName();
            case 3: return t.getGroup() != null ? t.getGroup() : "-";
            case 4: return t.getCoach();
            case 5: return t.getHomeStadium();
            case 6: return t.getPlayerCount() + "人";
            default: return "";
        }
    }
}
