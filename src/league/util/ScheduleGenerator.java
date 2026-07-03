package league.util;

import league.model.Match;
import league.model.Team;

import java.util.*;

/**
 * 赛程生成器 — 使用"圆桌旋转算法"生成主客场双循环赛程
 *
 * 算法原理:
 *   1. 将球队编号放入列表，固定第1队不动
 *   2. 其余球队围成一个环，每轮顺时针旋转一位
 *   3. 前半程 N-1 轮：每轮由对阵双方便是 "第1队 vs 环上对应队" + "其他队两两对阵"
 *   4. 后半程 N-1 轮：交换主客场
 *
 * 示例（4队: A B C D）:
 *   前半程:
 *     Round 1: A vs D, B vs C
 *     Round 2: A vs C, D vs B
 *     Round 3: A vs B, C vs D
 *   后半程（主客交换）:
 *     Round 4: D vs A, C vs B
 *     Round 5: C vs A, B vs D
 *     Round 6: B vs A, D vs C
 */
public class ScheduleGenerator {

    /**
     * 为给定的球队列表生成主客场双循环赛程（全部轮次）
     * @param teams 球队列表
     * @return 所有比赛的列表，按轮次排序
     */
    public static List<Match> generate(List<Team> teams) {
        return generate(teams, Integer.MAX_VALUE);
    }

    /**
     * 为给定的球队列表生成主客场双循环赛程（限制最大轮次）
     * @param teams     球队列表
     * @param maxRounds 最大轮次数（超过则不生成后续轮次）
     * @return 比赛列表，不超过 maxRounds 轮
     */
    public static List<Match> generate(List<Team> teams, int maxRounds) {
        if (teams == null || teams.size() < 2) {
            throw new IllegalArgumentException("至少需要2支球队才能生成赛程");
        }

        List<Match> allMatches = new ArrayList<>();
        List<String> teamIds = new ArrayList<>();
        for (Team t : teams) {
            teamIds.add(t.getId());
        }

        int n = teamIds.size();
        boolean isOdd = (n % 2 != 0);

        // 奇数队时添加轮空标记
        List<String> circle;
        if (isOdd) {
            circle = new ArrayList<>(teamIds);
            circle.add(null); // null 表示轮空
            n++;
        } else {
            circle = new ArrayList<>(teamIds);
        }

        int totalRounds = n - 1;          // 前半程轮次
        int roundCounter = 1;
        Map<String, String> stadiumMap = buildStadiumMap(teams);

        // ===== 前半程：正常对阵 =====
        for (int round = 0; round < totalRounds && roundCounter <= maxRounds; round++) {
            List<Match> roundMatches = generateRoundMatches(
                    roundCounter++, circle, teams, stadiumMap, false);
            allMatches.addAll(roundMatches);
            rotateCircle(circle);
        }

        // 重置 circle 列表用于后半程
        circle.clear();
        for (Team t : teams) {
            circle.add(t.getId());
        }
        if (isOdd) circle.add(null);

        // ===== 后半程：交换主客场 =====
        for (int round = 0; round < totalRounds && roundCounter <= maxRounds; round++) {
            List<Match> roundMatches = generateRoundMatches(
                    roundCounter++, circle, teams, stadiumMap, true);
            allMatches.addAll(roundMatches);
            rotateCircle(circle);
        }

        return allMatches;
    }

    /**
     * 生成一轮比赛
     * @param swapHomeAway true=后半程（交换主客场）
     */
    private static List<Match> generateRoundMatches(
            int round, List<String> circle, List<Team> teams,
            Map<String, String> stadiumMap, boolean swapHomeAway) {

        List<Match> roundMatches = new ArrayList<>();
        int n = circle.size();
        int half = n / 2;

        for (int i = 0; i < half; i++) {
            String left = circle.get(i);
            String right = circle.get(n - 1 - i);

            // 跳过轮空
            if (left == null || right == null) continue;

            String homeId, awayId;
            if (swapHomeAway) {
                homeId = right;
                awayId = left;
            } else {
                homeId = left;
                awayId = right;
            }

            String stadium = stadiumMap.getOrDefault(homeId, "中立场地");
            Match match = new Match(round, homeId, awayId, stadium);
            // 设置一个占位日期（后续可在 UI 中修改）
            match.setDate(String.format("第%d轮", round));
            roundMatches.add(match);
        }

        return roundMatches;
    }

    /** 环形旋转：固定第0位，其余元素顺时针移动一位 */
    private static void rotateCircle(List<String> circle) {
        // 移除并保留最后一个元素
        String last = circle.remove(circle.size() - 1);
        // 插入到第1位（第0位固定不动）
        circle.add(1, last);
    }

    /** 构建球队ID到主场名称的映射 */
    private static Map<String, String> buildStadiumMap(List<Team> teams) {
        Map<String, String> map = new HashMap<>();
        for (Team t : teams) {
            String stadium = t.getHomeStadium();
            if (stadium == null || stadium.isEmpty()) {
                stadium = t.getName() + "主场";
            }
            map.put(t.getId(), stadium);
        }
        return map;
    }
}
