package league.ui;

import league.model.Standing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * 积分柱状图组件
 * 用 Graphics2D 手绘柱状图，展示各队积分对比
 *
 * 特点：
 *   - 前3名用金银铜色区分
 *   - 其余用蓝色
 *   - 自动适配面板大小
 */
public class BarChartPanel extends JPanel {
    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color SILVER = new Color(192, 192, 192);
    private static final Color BRONZE = new Color(205, 127, 50);
    private static final Color BLUE = new Color(70, 130, 200);
    private static final Color BG_COLOR = new Color(250, 250, 250);
    private static final Color GRID_COLOR = new Color(230, 230, 230);

    private List<Standing> standings;

    /** 创建积分柱状图组件，默认尺寸 700×300，背景白色 */
    public BarChartPanel() {
        setPreferredSize(new Dimension(700, 300));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createTitledBorder("积分对比图（前3名金银铜高亮）"));
    }

    /**
     * 更新积分数据并重绘柱状图
     * @param standings 排序后的球队积分列表
     */
    public void updateData(List<Standing> standings) {
        this.standings = standings;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (standings == null || standings.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 边距
        int marginLeft = 60;
        int marginRight = 30;
        int marginTop = 30;
        int marginBottom = 70;

        int chartWidth = width - marginLeft - marginRight;
        int chartHeight = height - marginTop - marginBottom;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        // 找到最大积分值作为 Y 轴上限
        int maxPoints = standings.stream()
                .mapToInt(Standing::getPoints)
                .max().orElse(1);
        // Y 轴上限向上取整到 5 的倍数
        int yMax = ((maxPoints / 5) + 1) * 5;
        if (yMax == 0) yMax = 5;

        // 绘制网格线和 Y 轴标签
        g2d.setColor(GRID_COLOR);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = marginTop + chartHeight - (chartHeight * i / gridLines);
            g2d.drawLine(marginLeft, y, width - marginRight, y);
            int val = yMax * i / gridLines;
            String label = String.valueOf(val);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.setColor(Color.GRAY);
            g2d.drawString(label, marginLeft - fm.stringWidth(label) - 5,
                    y + fm.getAscent() / 2);
            g2d.setColor(GRID_COLOR);
        }

        // 绘制坐标轴
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartHeight);
        g2d.drawLine(marginLeft, marginTop + chartHeight,
                width - marginRight, marginTop + chartHeight);

        // 计算柱状图布局
        int teamCount = standings.size();
        int barWidth = Math.min(50, (chartWidth / teamCount) - 10);
        int gap = (chartWidth - barWidth * teamCount) / (teamCount + 1);

        // 绘制柱状图和 X 轴标签
        for (int i = 0; i < teamCount; i++) {
            Standing s = standings.get(i);
            int x = marginLeft + gap + i * (barWidth + gap);
            int barHeight = (int) ((double) s.getPoints() / yMax * chartHeight);
            int y = marginTop + chartHeight - barHeight;

            // 根据排名选择颜色
            Color barColor;
            if (i == 0) barColor = GOLD;
            else if (i == 1) barColor = SILVER;
            else if (i == 2) barColor = BRONZE;
            else barColor = BLUE;

            // 绘制柱体
            GradientPaint gradient = new GradientPaint(
                    x, y, barColor.brighter(),
                    x, marginTop + chartHeight, barColor.darker());
            g2d.setPaint(gradient);
            g2d.fill(new Rectangle2D.Double(x, y, barWidth, barHeight));

            // 柱体边框
            g2d.setColor(barColor.darker());
            g2d.draw(new Rectangle2D.Double(x, y, barWidth, barHeight));

            // 柱体上方显示积分
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String pointStr = String.valueOf(s.getPoints());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(pointStr,
                    x + (barWidth - fm.stringWidth(pointStr)) / 2,
                    y - 5);

            // X 轴球队名称（倾斜显示以节省空间）
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            String name = s.getTeamName();
            if (name.length() > 4) name = name.substring(0, 4);

            // 如果球队名较长，旋转绘制
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x + barWidth / 2, marginTop + chartHeight + 10);
            g2dCopy.rotate(Math.toRadians(-30));
            FontMetrics fm2 = g2dCopy.getFontMetrics();
            g2dCopy.drawString(name, -fm2.stringWidth(name) / 2, 0);
            g2dCopy.dispose();
        }
    }
}
