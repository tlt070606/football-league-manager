package league.service;

import league.model.Match;
import league.model.Standing;
import league.model.Team;
import league.util.LogoGenerator;
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
    /** 单例实例（延迟初始化，非线程安全，Swing 桌面应用为单线程场景无需同步） */
    private static LeagueManager instance;

    private Map<String, Team> teams;           // 球队 Map, key=id
    private List<Match> matches;               // 所有比赛
    private List<Standing> standings;          // 当前积分榜
    private FileManager fileManager;           // JSON 持久化委托

    private LeagueManager() {
        this.fileManager = new FileManager();
        this.teams = new LinkedHashMap<>();
        this.matches = new ArrayList<>();
        this.standings = new ArrayList<>();
    }

    /**
     * 获取单例实例
     * @return LeagueManager 单例对象
     */
    public static LeagueManager getInstance() {
        if (instance == null) {
            instance = new LeagueManager();
        }
        return instance;
    }

    // ==================== 球队管理 ====================

    /**
     * 添加球队
     * @param team 要添加的球队对象
     * @throws IllegalArgumentException 球队名称为空或重名时抛出
     */
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

    /**
     * 更新球队信息
     * @param updated 更新后的球队对象（通过 id 匹配已有球队）
     * @throws IllegalArgumentException 球队不存在时抛出
     */
    public void updateTeam(Team updated) {
        if (!teams.containsKey(updated.getId())) {
            throw new IllegalArgumentException("球队不存在: " + updated.getId());
        }
        teams.put(updated.getId(), updated);
        saveData();
    }

    /**
     * 删除球队（已有比赛记录的球队不可删除）
     * @param teamId 要删除的球队 ID
     * @throws IllegalStateException 球队已有比赛记录时抛出
     */
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

    /**
     * 根据 ID 获取球队
     * @param id 球队 ID
     * @return Team 对象，不存在返回 null
     */
    public Team getTeam(String id) {
        return teams.get(id);
    }

    /**
     * 获取所有球队（按名称排序）
     * @return 按名称排序的球队列表
     */
    public List<Team> getAllTeams() {
        return teams.values().stream()
                .sorted(Comparator.comparing(Team::getName))
                .collect(Collectors.toList());
    }

    /**
     * 获取球队总数
     * @return 球队总数
     */
    public int getTeamCount() {
        return teams.size();
    }

    /**
     * 搜索球队（按名称或教练模糊匹配）
     * @param keyword 搜索关键词，为空时返回全部
     * @return 匹配的球队列表（按名称排序）
     */
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

    /** 每支球队的比赛轮次数 */
    private static final int MAX_ROUNDS = 16;

    // ==================== 比赛管理 ====================

    /**
     * 自动生成主客场双循环赛程（最多 16 轮，会清除旧赛程和所有比分记录）
     * @throws IllegalStateException 球队数量少于 2 时抛出
     */
    public void generateSchedule() {
        if (teams.size() < 2) {
            throw new IllegalStateException("至少需要2支球队才能生成赛程");
        }
        List<Team> teamList = new ArrayList<>(teams.values());
        matches = ScheduleGenerator.generate(teamList, MAX_ROUNDS);
        saveData();
    }

    /**
     * 录入比赛结果（录入后自动更新积分榜）
     * @param matchId   比赛 ID
     * @param homeScore 主队进球数
     * @param awayScore 客队进球数
     * @throws IllegalArgumentException 比赛不存在或比分为负数时抛出
     */
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

    /**
     * 获取指定轮次的比赛
     * @param round 轮次
     * @return 该轮次的所有比赛列表
     */
    public List<Match> getMatchesByRound(int round) {
        return matches.stream()
                .filter(m -> m.getRound() == round)
                .sorted(Comparator.comparing(Match::getDate))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有比赛
     * @return 所有比赛的副本列表
     */
    public List<Match> getAllMatches() {
        return new ArrayList<>(matches);
    }

    /**
     * 获取联赛总轮数
     * @return 总轮数，若无比赛则返回 0
     */
    public int getTotalRounds() {
        if (matches.isEmpty()) return 0;
        return matches.stream()
                .mapToInt(Match::getRound)
                .max().orElse(0);
    }

    /**
     * 是否有已生成的赛程
     * @return true 表示已有赛程数据
     */
    public boolean hasSchedule() {
        return !matches.isEmpty();
    }

    /**
     * 一键随机模拟全部比赛：为所有未赛比赛随机生成比分
     *
     * 比分分布模拟世界杯真实数据（加权随机）：
     *   1-0 (18%), 2-1 (16%), 2-0 (14%), 1-1 (12%),
     *   0-0 (8%), 3-0 (7%), 3-1 (7%), 3-2 (5%),
     *   4-0 (3%), 4-1 (3%), 2-2 (3%), 其他 (4%)
     */
    public void randomSimulateAll() {
        // 比分池（加权分布）
        int[][] scorePool = {
            {1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0}, // 1-0 x18
            {2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},{2,1},             // 2-1 x16
            {2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},{2,0},                         // 2-0 x14
            {1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},{1,1},                                     // 1-1 x12
            {0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},                                                             // 0-0 x8
            {3,0},{3,0},{3,0},{3,0},{3,0},{3,0},{3,0},                                                                   // 3-0 x7
            {3,1},{3,1},{3,1},{3,1},{3,1},{3,1},{3,1},                                                                   // 3-1 x7
            {3,2},{3,2},{3,2},{3,2},{3,2},                                                                               // 3-2 x5
            {4,0},{4,0},{4,0},                                                                                           // 4-0 x3
            {4,1},{4,1},{4,1},                                                                                           // 4-1 x3
            {2,2},{2,2},{2,2},                                                                                           // 2-2 x3
            {0,1},{0,2},{1,2},{4,2},                                                                                     // 其他 x4
        };

        Random random = new Random();
        for (Match m : matches) {
            if (!m.isPlayed()) {
                int[] score = scorePool[random.nextInt(scorePool.length)];
                m.setHomeScore(score[0]);
                m.setAwayScore(score[1]);
                m.setPlayed(true);
            }
        }
        updateStandings();
        saveData();
    }

    /**
     * 清除所有比赛数据（保留球队不变）
     */
    public void clearAllMatches() {
        matches.clear();
        standings.clear();
        saveData();
    }

    /**
     * 一键重置：清除所有比赛 + 恢复 32 队预设数据
     * 先清空 teams map，再调用 initPresetData 重新写入
     */
    public void resetToPreset() {
        teams.clear();
        matches.clear();
        standings.clear();
        initPresetData();
        saveData();
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

        // 排序规则（优先级从高到低）：积分 > 净胜球 > 总进球数 > 队名（字母序作为最终稳定排序）
        standings = standingMap.values().stream()
                .sorted(Comparator
                        .comparingInt(Standing::getPoints).reversed()
                        .thenComparingInt(Standing::getGoalDiff).reversed()
                        .thenComparingInt(Standing::getGoalsFor).reversed()
                        .thenComparing(Standing::getTeamName))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前积分榜
     * @return 排序后的积分榜列表（积分 > 净胜球 > 进球数 > 队名）
     */
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

    /** 首次运行时预置 32 支世界杯球队（8 组 × 4 队） */
    private void initPresetData() {
        // 格式: {名称, 小组, 教练, 主场, 球员\n球员\n...}
        String[][] preset = {
            // ===== A 组 =====
            {"美国",       "A", "格雷格·贝尔哈特",   "梅赛德斯-奔驰体育场",
             "马特·特纳\n克里斯蒂安·普利西奇\n韦斯顿·麦肯尼\n蒂莫西·维阿\n吉奥·雷纳"},
            {"英格兰",     "A", "加雷斯·索斯盖特",   "温布利球场",
             "乔丹·皮克福德\n哈里·凯恩\n裘德·贝林厄姆\n菲尔·福登\n德克兰·赖斯"},
            {"伊朗",       "A", "阿米尔·加莱诺伊",   "阿扎迪体育场",
             "阿里雷扎·贝兰万德\n梅赫迪·塔雷米\n萨达尔·阿兹蒙\n阿里·戈利扎德\n赛义德·埃扎托拉希"},
            {"威尔士",     "A", "罗布·佩奇",         "加的夫城体育场",
             "丹尼·沃德\n加雷斯·贝尔\n阿隆·拉姆齐\n本·戴维斯\n丹尼尔·詹姆斯"},
            // ===== B 组 =====
            {"阿根廷",     "B", "利昂内尔·斯卡洛尼", "纪念碑球场",
             "埃米利亚诺·马丁内斯\n利昂内尔·梅西\n胡利安·阿尔瓦雷斯\n安赫尔·迪马利亚\n恩佐·费尔南德斯"},
            {"墨西哥",     "B", "海梅·洛萨诺",       "阿兹特克体育场",
             "吉列尔莫·奥乔亚\n伊尔文·洛萨诺\n劳尔·希门尼斯\n埃德森·阿尔瓦雷斯\n奥贝林·皮内达"},
            {"波兰",       "B", "米哈乌·普罗别日",   "华沙国家体育场",
             "沃伊切赫·什琴斯尼\n罗伯特·莱万多夫斯基\n彼得·泽林斯基\n扬·贝德纳雷克\n卡罗尔·斯维德斯基"},
            {"中国",       "B", "布兰科·伊万科维奇", "北京国家体育场（鸟巢）",
             "颜骏凌\n武磊\n蒋光太\n张玉宁\n戴伟浚"},
            // ===== C 组 =====
            {"法国",       "C", "迪迪埃·德尚",       "法兰西体育场",
             "迈克·迈尼昂\n基利安·姆巴佩\n安托万·格里兹曼\n奥雷利安·楚阿梅尼\n奥斯曼·登贝莱"},
            {"丹麦",       "C", "卡斯帕·尤尔曼德",   "帕肯体育场",
             "卡斯帕·舒梅切尔\n克里斯蒂安·埃里克森\n拉斯穆斯·霍伊伦\n皮埃尔·霍伊别尔\n安德烈亚斯·克里斯滕森"},
            {"突尼斯",     "C", "贾莱尔·卡德里",     "拉迪斯奥林匹克体育场",
             "艾门·达门\n瓦赫比·哈兹里\n优素福·姆萨克尼\n埃利耶斯·斯希里\n蒙塔萨尔·塔尔比"},
            {"秘鲁",       "C", "豪尔赫·福萨蒂",     "利马国家体育场",
             "佩德罗·加莱塞\n詹卢卡·拉帕杜拉\n安德烈·卡里略\n雷纳托·塔皮亚\n路易斯·阿德文库拉"},
            // ===== D 组 =====
            {"西班牙",     "D", "路易斯·德拉富恩特", "伯纳乌球场",
             "乌奈·西蒙\n阿尔瓦罗·莫拉塔\n佩德里\n加维\n罗德里"},
            {"德国",       "D", "尤利安·纳格尔斯曼", "安联球场",
             "曼努埃尔·诺伊尔\n贾马尔·穆西亚拉\n弗洛里安·维尔茨\n凯·哈弗茨\n约书亚·基米希"},
            {"日本",       "D", "森保一",             "埼玉体育场",
             "权田修一\n三笘薰\n久保建英\n堂安律\n南野拓实"},
            {"哥斯达黎加", "D", "古斯塔沃·阿尔法罗", "哥斯达黎加国家体育场",
             "凯洛尔·纳瓦斯\n乔尔·坎贝尔\n弗朗西斯科·卡尔沃\n耶尔特辛·特赫达\n安东尼·孔特雷拉斯"},
            // ===== E 组 =====
            {"巴西",       "E", "多里瓦尔·儒尼奥尔", "马拉卡纳体育场",
             "阿利松·贝克尔\n维尼修斯·儒尼奥尔\n罗德里戈\n内马尔\n布鲁诺·吉马良斯"},
            {"瑞士",       "E", "穆拉特·雅金",       "瑞士体育场",
             "扬·索默\n格拉尼特·扎卡\n谢尔丹·沙奇里\n布雷尔·恩博洛\n曼努埃尔·阿坎吉"},
            {"塞尔维亚",   "E", "德拉甘·斯托伊科维奇","红星体育场",
             "瓦尼亚·米林科维奇-萨维奇\n亚历山大·米特罗维奇\n杜尚·弗拉霍维奇\n杜尚·塔迪奇\n谢尔盖·米林科维奇-萨维奇"},
            {"喀麦隆",     "E", "里戈贝尔·宋",       "雅温得奥莱姆贝体育场",
             "安德烈·奥纳纳\n文森特·阿布巴卡尔\n埃里克·舒波-莫廷\n卡尔·托科·埃坎比\n赞博·安古伊萨"},
            // ===== F 组 =====
            {"比利时",     "F", "多梅尼科·特德斯科", "博杜安国王体育场",
             "蒂博·库尔图瓦\n凯文·德布劳内\n罗梅卢·卢卡库\n莱安德罗·特罗萨德\n阿马杜·奥纳纳"},
            {"克罗地亚",   "F", "兹拉特科·达利奇",   "马克西米尔体育场",
             "多米尼克·利瓦科维奇\n卢卡·莫德里奇\n马特奥·科瓦契奇\n约什科·格瓦迪奥尔\n安德烈·克拉马里奇"},
            {"摩洛哥",     "F", "瓦利德·雷格拉吉",   "穆罕默德五世体育场",
             "亚辛·布努\n阿什拉夫·哈基米\n哈基姆·齐耶什\n索菲扬·阿姆拉巴特\n优素福·恩内西里"},
            {"加拿大",     "F", "杰西·马什",         "BMO体育场",
             "米兰·博里扬\n阿方索·戴维斯\n乔纳森·戴维\n斯蒂芬·欧斯塔基奥\n凯尔·拉林"},
            // ===== G 组 =====
            {"葡萄牙",     "G", "罗伯托·马丁内斯",   "光明球场",
             "迪奥戈·科斯塔\n克里斯蒂亚诺·罗纳尔多\n布鲁诺·费尔南德斯\n贝尔纳多·席尔瓦\n拉斐尔·莱昂"},
            {"乌拉圭",     "G", "马塞洛·贝尔萨",     "世纪球场",
             "塞尔吉奥·罗切特\n费德里科·巴尔韦德\n达尔文·努涅斯\n罗纳德·阿劳霍\n吉奥尔吉安·德阿拉斯卡埃塔"},
            {"韩国",       "G", "尤尔根·克林斯曼",   "首尔世界杯体育场",
             "金承奎\n孙兴慜\n黄喜灿\n李刚仁\n金玟哉"},
            {"加纳",       "G", "克里斯·休顿",       "巴巴亚拉体育场",
             "劳伦斯·阿蒂-齐吉\n穆罕默德·库杜斯\n托马斯·帕尔特伊\n伊尼亚基·威廉姆斯\n乔丹·阿尤"},
            // ===== H 组 =====
            {"荷兰",       "H", "罗纳德·科曼",       "约翰·克鲁伊夫竞技场",
             "巴特·维尔布鲁根\n维吉尔·范迪克\n孟菲斯·德佩\n科迪·加克波\n弗兰基·德容"},
            {"塞内加尔",   "H", "阿利乌·西塞",       "迪安尼究奥林匹克体育场",
             "爱德华·门迪\n萨迪奥·马内\n卡利杜·库利巴利\n伊斯梅拉·萨尔\n尼古拉·雅克松"},
            {"厄瓜多尔",   "H", "费利克斯·桑切斯",   "罗德里戈·帕斯体育场",
             "埃尔南·加林德斯\n恩纳·瓦伦西亚\n莫伊塞斯·凯塞多\n佩尔维斯·埃斯图皮尼安\n凯文·罗德里格斯"},
            {"澳大利亚",   "H", "格雷厄姆·阿诺德",   "澳大利亚体育场",
             "马修·瑞安\n马修·莱基\n杰克逊·欧文\n哈里·苏塔\n克雷格·古德温"},
        };

        for (String[] data : preset) {
            Team team = new Team(data[0], data[2], data[3]);
            team.setGroup(data[1]);
            List<String> players = new ArrayList<>();
            for (String p : data[4].split("\n")) {
                players.add(p.trim());
            }
            team.setPlayers(players);

            // 自动生成队徽（先放入 teams map 以获取 team id，再生成 logo）
            teams.put(team.getId(), team);
            String logoPath = LogoGenerator.generate(team.getId(), team.getName(), team.getGroup());
            if (logoPath != null) {
                team.setLogo(logoPath);
            }
        }
        updateStandings();
    }

    /**
     * 获取球队 ID 到名称的映射（供 UI 查询名称用）
     * @return Map<球队ID, 球队名称>
     */
    public Map<String, String> getTeamIdNameMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Team t : teams.values()) {
            map.put(t.getId(), t.getName());
        }
        return map;
    }
}
