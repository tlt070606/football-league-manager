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

    public MainFrame() {
        this.manager = LeagueManager.getInstance();
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("足球联赛积分管理系统 — 中超联赛");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // 窗口居中

        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 忽略，使用默认外观
        }

        // 设置全局字体（支持中文显示）
        setUIFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

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
        JLabel statusLabel = new JLabel("  欢迎使用足球联赛积分管理系统");
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
