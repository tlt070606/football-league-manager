package league.service;

import league.model.Match;
import league.model.Standing;
import league.model.Team;
import league.util.ScheduleGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 联赛管理核心控制器 — 单例模式
 *
 * 职责：
 *   - 球队的增删改查
 *   - 赛程自动生成（委托给 ScheduleGenerator）
 *   - 比赛结果录入
 *   - 积分榜动态计算（遍历所有已完赛比赛，重新计算）
 *   - 数据持久化（委托给 FileManager）
 *
 * 使用方式：LeagueManager lm = LeagueManager.getInstance();
 */
public class LeagueManager {
    private static LeagueManager instance;

    private Map<String, Team> teams;           // 球队 Map, key=id
    private List<Match> matches;               // 所有比赛
    private List<Standing> standings;          // 当前积分榜
    private FileManager fileManager;

    private LeagueManager() {
        this.fileManager = new FileManager();
        this.teams = new LinkedHashMap<>();
        this.matches = new ArrayList<>();
        this.standings = new ArrayList<>();
    }

    /** 获取单例实例 */
    public static LeagueManager getInstance() {
        if (instance == null) {
            instance = new LeagueManager();
        }
        return instance;
    }

    // ==================== 球队管理 ====================

    /** 添加球队 */
    public void addTeam(Team team) {
        if (team == null || team.getName() == null || team.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("球队名称不能为空");
        }
        // 检查重名
        for (Team t : teams.values()) {
            if (t.getName().equals(team.getName().trim())) {
                throw new IllegalArgumentException("球队名称已存在: " + team.getName());
            }
        }
        team.setName(team.getName().trim());
        teams.put(team.getId(), team);
        saveData();
    }

    /** 更新球队信息 */
    public void updateTeam(Team updated) {
        if (!teams.containsKey(updated.getId())) {
            throw new IllegalArgumentException("球队不存在: " + updated.getId());
        }
        teams.put(updated.getId(), updated);
        saveData();
    }

    /** 删除球队（如果已有比赛记录则不允许删除） */
    public void deleteTeam(String teamId) {
        // 检查是否有比赛涉及该队
        for (Match m : matches) {
            if (m.getHomeTeamId().equals(teamId) || m.getAwayTeamId().equals(teamId)) {
                throw new IllegalStateException("该球队已有比赛记录，无法删除。请先清除相关比赛数据。");
            }
        }
        if (teams.remove(teamId) != null) {
            saveData();
        }
    }

    /** 根据 ID 获取球队 */
    public Team getTeam(String id) {
        return teams.get(id);
    }

    /** 获取所有球队（按名称排序） */
    public List<Team> getAllTeams() {
        return teams.values().stream()
                .sorted(Comparator.comparing(Team::getName))
                .collect(Collectors.toList());
    }

    /** 获取球队总数 */
    public int getTeamCount() {
        return teams.size();
    }

    /** 搜索球队（按名称模糊匹配） */
    public List<Team> searchTeams(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTeams();
        }
        String kw = keyword.trim().toLowerCase();
        return teams.values().stream()
                .filter(t -> t.getName().toLowerCase().contains(kw)
                        || t.getCoach().toLowerCase().contains(kw))
                .sorted(Comparator.comparing(Team::getName))
                .collect(Collectors.toList());
    }

    // ==================== 比赛管理 ====================

    /** 自动生成主客场双循环赛程（会清除旧赛程） */
    public void generateSchedule() {
        if (teams.size() < 2) {
            throw new IllegalStateException("至少需要2支球队才能生成赛程");
        }
        List<Team> teamList = new ArrayList<>(teams.values());
        matches = ScheduleGenerator.generate(teamList);
        saveData();
    }

    /** 录入比赛结果 */
    public void recordMatchResult(String matchId, int homeScore, int awayScore) {
        Match match = findMatchById(matchId);
        if (match == null) {
            throw new IllegalArgumentException("比赛不存在: " + matchId);
        }
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("比分不能为负数");
        }

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setPlayed(true);

        // 录入比分后自动更新积分榜
        updateStandings();
        saveData();
    }

    /** 获取指定轮次的比赛 */
    public List<Match> getMatchesByRound(int round) {
        return matches.stream()
                .filter(m -> m.getRound() == round)
                .sorted(Comparator.comparing(Match::getDate))
                .collect(Collectors.toList());
    }

    /** 获取所有比赛 */
    public List<Match> getAllMatches() {
        return new ArrayList<>(matches);
    }

    /** 获取联赛总轮数 */
    public int getTotalRounds() {
        if (matches.isEmpty()) return 0;
        return matches.stream()
                .mapToInt(Match::getRound)
                .max().orElse(0);
    }

    /** 是否有已生成的赛程 */
    public boolean hasSchedule() {
        return !matches.isEmpty();
    }

    /** 查找比赛 */
    private Match findMatchById(String matchId) {
        return matches.stream()
                .filter(m -> m.getId().equals(matchId))
                .findFirst().orElse(null);
    }

    // ==================== 积分计算 ====================

    /**
     * 更新积分榜
     * 遍历所有已完成的比赛，为每队重新计算统计数据，然后按规则排序
     */
    public void updateStandings() {
        // 初始化每队的积分记录
        Map<String, Standing> standingMap = new LinkedHashMap<>();
        for (Team t : teams.values()) {
            Standing s = new Standing(t.getId(), t.getName());
            standingMap.put(t.getId(), s);
        }

        // 统计所有已完赛的比赛
        for (Match m : matches) {
            if (!m.isPlayed()) continue;

            Standing homeStanding = standingMap.get(m.getHomeTeamId());
            Standing awayStanding = standingMap.get(m.getAwayTeamId());

            if (homeStanding != null) {
                homeStanding.recordMatch(m, true);
            }
            if (awayStanding != null) {
                awayStanding.recordMatch(m, false);
            }
        }

        // 排序：积分 > 净胜球 > 进球 > 队名
        standings = standingMap.values().stream()
                .sorted(Comparator
                        .comparingInt(Standing::getPoints).reversed()
                        .thenComparingInt(Standing::getGoalDiff).reversed()
                        .thenComparingInt(Standing::getGoalsFor).reversed()
                        .thenComparing(Standing::getTeamName))
                .collect(Collectors.toList());
    }

    /** 获取当前积分榜（排序后） */
    public List<Standing> getStandings() {
        return new ArrayList<>(standings);
    }

    // ==================== 数据持久化 ====================

    /** 保存所有数据 */
    public void saveData() {
        fileManager.saveTeams(teams);
        fileManager.saveMatches(matches);
    }

    /** 加载数据（程序启动时调用），无已有数据时自动预置中超球队 */
    public void loadData() {
        if (fileManager.hasExistingData()) {
            teams = fileManager.loadTeams();
            matches = fileManager.loadMatches();
            updateStandings();
        } else {
            initPresetData();
            saveData();
        }
    }

    /** 首次运行时预置 8 支中超球队 */
    private void initPresetData() {
        String[][] preset = {
            {"上海海港", "凯文·穆斯卡特", "上汽浦东足球场",
             "颜骏凌\n蒋光太\n王燊超\n奥斯卡\n武磊"},
            {"山东泰山", "崔康熙", "济南奥体中心",
             "王大雷\n郑铮\n刘洋\n克雷桑\n费莱尼"},
            {"北京国安", "里卡多·苏亚雷斯", "北京工人体育场",
             "韩佳奇\n恩加德乌\n李磊\n张稀哲\n法比奥"},
            {"上海申花", "莱昂尼德·斯卢茨基", "上海体育场",
             "鲍亚雄\n朱辰杰\n蒋圣龙\n吴曦\n马莱莱"},
            {"成都蓉城", "徐正源", "凤凰山体育公园",
             "张岩\n理查德\n周定洋\n费利佩\n艾克森"},
            {"武汉三镇", "里卡多·罗德里格斯", "武汉体育中心",
             "刘殿座\n朴志洙\n邓涵文\n斯坦丘\n戴维森"},
            {"天津津门虎", "于根伟", "天津奥体中心",
             "方镜淇\n安杜哈尔\n巴顿\n谢维军\n贝里奇"},
            {"浙江队", "乔迪·温亚尔斯", "杭州黄龙体育中心",
             "赵博\n卢卡斯\n岳鑫\n弗兰克\n穆谢奎"},
        };

        for (String[] data : preset) {
            Team team = new Team(data[0], data[1], data[2]);
            List<String> players = new ArrayList<>();
            for (String p : data[3].split("\n")) {
                players.add(p.trim());
            }
            team.setPlayers(players);
            teams.put(team.getId(), team);
        }
        updateStandings();
    }

    /** 获取已有球队的 ID 集合（供 UI 查询名称用） */
    public Map<String, String> getTeamIdNameMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Team t : teams.values()) {
            map.put(t.getId(), t.getName());
        }
        return map;
    }
}
