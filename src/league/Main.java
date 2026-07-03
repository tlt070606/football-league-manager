package league;

import league.ui.MainFrame;

import javax.swing.*;

/**
 * 程序入口
 *
 * 启动足球联赛积分管理系统 GUI
 */
public class Main {
    public static void main(String[] args) {
        // 在事件调度线程中创建和显示 GUI（Swing 线程安全要求）
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
