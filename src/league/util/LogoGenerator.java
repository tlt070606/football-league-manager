package league.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 队徽自动生成器
 *
 * 为每支球队生成简单的圆形队徽：
 *   - 按小组使用不同底色（8 种颜色对应 A-H 组）
 *   - 中央显示球队名称首字
 *   - 输出为 128×128 PNG 文件到 assets/logos/
 */
public class LogoGenerator {

    /** 8 组配色 */
    private static final Color[] GROUP_COLORS = {
        new Color(220, 50, 50),    // A 组 — 红
        new Color(50, 120, 220),   // B 组 — 蓝
        new Color(50, 180, 80),    // C 组 — 绿
        new Color(240, 160, 30),   // D 组 — 橙
        new Color(160, 50, 200),   // E 组 — 紫
        new Color(30, 170, 180),   // F 组 — 青
        new Color(220, 180, 30),   // G 组 — 金
        new Color(200, 100, 140),  // H 组 — 粉
    };

    /**
     * 为指定球队生成队徽 PNG 文件
     * @param teamId   球队 ID（用作文件名）
     * @param teamName 球队名称（取首字作为图标文字）
     * @param group    小组字母 A-H（决定底色）
     * @return 生成的 PNG 文件路径，失败返回 null
     */
    public static String generate(String teamId, String teamName, String group) {
        int size = 128;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 透明背景
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, size, size);
        g2d.setComposite(AlphaComposite.SrcOver);

        // 底色圆形
        int colorIndex = 0;
        if (group != null && group.length() == 1) {
            char g = group.charAt(0);
            if (g >= 'A' && g <= 'H') colorIndex = g - 'A';
        }
        g2d.setColor(GROUP_COLORS[colorIndex]);
        g2d.fill(new Ellipse2D.Double(4, 4, size - 8, size - 8));

        // 圆形边框
        g2d.setColor(GROUP_COLORS[colorIndex].darker());
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(new Ellipse2D.Double(4, 4, size - 8, size - 8));

        // 内圈装饰线
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(16, 16, size - 32, size - 32));

        // 队名首字
        String text = teamName.substring(0, 1);
        g2d.setColor(Color.WHITE);
        Font font = new Font("Microsoft YaHei", Font.BOLD, 56);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (size - fm.stringWidth(text)) / 2;
        int textY = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);

        g2d.dispose();

        // 写入文件
        try {
            File dir = new File("assets" + File.separator + "logos");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, teamId + ".png");
            ImageIO.write(img, "PNG", file);
            return file.getPath();
        } catch (IOException e) {
            System.err.println("生成队徽失败: " + e.getMessage());
            return null;
        }
    }
}
