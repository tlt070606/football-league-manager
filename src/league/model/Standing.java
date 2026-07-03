package league.model;

/**
 * 积分记录类
 * 存储一支球队在联赛中的各项统计数据。
 * 注意：排名排序规则在 LeagueManager.updateStandings() 中实现，
 * 而非本类的 compareTo。规则：积分 > 净胜球 > 进球数 > 队名。
 *
 * 积分计算: 胜=3分, 平=1分, 负=0分
 */
public class Standing {
    private String teamId;
    private String teamName;
    private int played;          // 已赛场次
    private int won;             // 胜场
    private int drawn;           // 平场
    private int lost;            // 负场
    private int goalsFor;        // 进球数
    private int goalsAgainst;    // 失球数
    private int goalDiff;        // 净胜球（进球 - 失球）
    private int points;          // 积分

    public Standing() {}

    /**
     * 构造器
     * @param teamId   球队唯一编号
     * @param teamName 球队名称
     */
    public Standing(String teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    /**
     * 根据一场比赛的结果更新本队的统计数据
     * @param match      比赛数据（包含比分等信息，必须已完赛）
     * @param isHomeTeam 当前球队是否为该场比赛的主队；
     *                   true 时取主队进球作为本方进球，
     *                   false 时取客队进球作为本方进球
     */
    public void recordMatch(Match match, boolean isHomeTeam) {
        played++;

        int scored, conceded;
        if (isHomeTeam) {
            scored = match.getHomeScore();
            conceded = match.getAwayScore();
        } else {
            scored = match.getAwayScore();
            conceded = match.getHomeScore();
        }

        goalsFor += scored;
        goalsAgainst += conceded;
        goalDiff = goalsFor - goalsAgainst;

        if (scored > conceded) {
            won++;
            points += 3;
        } else if (scored == conceded) {
            drawn++;
            points += 1;
        } else {
            lost++;
        }
    }

    // ========== Getter / Setter ==========

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }

    public int getDrawn() { return drawn; }
    public void setDrawn(int drawn) { this.drawn = drawn; }

    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }

    public int getGoalsFor() { return goalsFor; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }

    public int getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }

    public int getGoalDiff() { return goalDiff; }
    public void setGoalDiff(int goalDiff) { this.goalDiff = goalDiff; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    @Override
    public String toString() {
        return String.format("%s: %d场 %d胜 %d平 %d负 %d分 (净胜球%d)",
                teamName, played, won, drawn, lost, points, goalDiff);
    }
}
