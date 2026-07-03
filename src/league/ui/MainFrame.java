package league.ui;

import league.service.LeagueManager;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗口
 * 使用 JTabbedPane 整合三个功能面板：球队管理、比赛管理、积分榜
 */
public class MainFrame extends JFrame {
    private LeagueManager manager;
    private TeamPanel teamPanel;
    private MatchPanel matchPanel;
    private StandingPanel standingPanel;
    private JTabbedPane tabbedPane;

    /** 创建主窗口，初始化 LeagueManager、UI 布局并加载已有数据 */
    public MainFrame() {
        this.manager = LeagueManager.getInstance();
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("世界杯小组赛积分管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 740);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null); // 窗口居中

        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 忽略，使用默认外观
        }

        // 设置全局字体（支持中文显示）
        setUIFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        // ===== 菜单栏 =====
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        JMenuItem resetItem = new JMenuItem("重置数据");
        resetItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "⚠️ 重置将清除所有比赛记录，恢复 32 支世界杯球队初始数据。\n确认继续？",
                    "确认重置", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                manager.resetToPreset();
                teamPanel.refreshData();
                matchPanel.refreshData();
                standingPanel.refreshData();
                JOptionPane.showMessageDialog(this,
                        "数据已重置！", "完成", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(resetItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // ===== 标签页面板 =====
        tabbedPane = new JTabbedPane();

        teamPanel = new TeamPanel();
        matchPanel = new MatchPanel();
        standingPanel = new StandingPanel();

        tabbedPane.addTab("球队管理", teamPanel);
        tabbedPane.addTab("比赛管理", matchPanel);
        tabbedPane.addTab("积分榜", standingPanel);

        // 切换标签页时自动刷新数据
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            switch (idx) {
                case 0: teamPanel.refreshData(); break;
                case 1: matchPanel.refreshData(); break;
                case 2: standingPanel.refreshData(); break;
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // ===== 底部状态栏 =====
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("  欢迎使用世界杯小组赛积分管理系统 | 32 支球队 · 8 个小组");
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    /** 启动时加载已有数据 */
    private void loadData() {
        manager.loadData();
        teamPanel.refreshData();
        matchPanel.refreshData();
        standingPanel.refreshData();
    }

    /** 递归设置所有组件的默认字体 */
    private static void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }
}
