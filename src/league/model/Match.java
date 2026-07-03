package league.model;

import java.util.UUID;

/**
 * 比赛模型类
 * 存储一场比赛的完整信息：对阵双方、比分、日期、场地等
 *
 * 设计说明：homeTeamId/awayTeamId 存的是球队 ID 而非 Team 对象引用，
 * 避免 Gson 序列化时产生循环引用和冗余数据。
 */
public class Match {
    private String id;
    private int round;              // 轮次 (1 ~ 2N-2)
    private String homeTeamId;      // 主队ID
    private String awayTeamId;      // 客队ID
    private int homeScore;          // 主队进球，-1 表示未进行
    private int awayScore;          // 客队进球，-1 表示未进行
    private String date;            // 比赛日期（格式: "2025-03-01"）
    private String stadium;         // 比赛场地
    private boolean played;         // 是否已进行

    /** 无参构造器（Gson 需要） */
    public Match() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.homeScore = -1;
        this.awayScore = -1;
        this.played = false;
    }

    /** 完整构造器 */
    public Match(int round, String homeTeamId, String awayTeamId, String stadium) {
        this();
        this.round = round;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadium = stadium;
    }

    // ========== 业务方法 ==========

    /** 返回获胜方球队ID，平局返回 null，未比赛返回 null */
    public String getWinTeamId() {
        if (!played) return null;
        if (homeScore > awayScore) return homeTeamId;
        if (awayScore > homeScore) return awayTeamId;
        return null; // 平局
    }

    /** 返回主队视角的净胜球 */
    public int getHomeGoalDiff() {
        if (!played) return 0;
        return homeScore - awayScore;
    }

    /** 返回客队视角的净胜球 */
    public int getAwayGoalDiff() {
        if (!played) return 0;
        return awayScore - homeScore;
    }

    /** 获取比分显示字符串，如 "2:1" 或 "-:-" (未赛) */
    public String getScoreDisplay() {
        if (!played) return "- : -";
        return homeScore + " : " + awayScore;
    }

    /** 是否为平局 */
    public boolean isDraw() {
        return played && homeScore == awayScore;
    }

    // ========== Getter / Setter ==========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }

    public String getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(String homeTeamId) { this.homeTeamId = homeTeamId; }

    public String getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(String awayTeamId) { this.awayTeamId = awayTeamId; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStadium() { return stadium; }
    public void setStadium(String stadium) { this.stadium = stadium; }

    public boolean isPlayed() { return played; }
    public void setPlayed(boolean played) { this.played = played; }

    @Override
    public String toString() {
        return "第" + round + "轮 " + homeTeamId + " vs " + awayTeamId;
    }
}
