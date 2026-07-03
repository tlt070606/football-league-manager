package league.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 球队模型类
 * 存储球队的基本信息：名称、队徽、教练、球员名单、主场
 */
public class Team {
    private String id;              // 球队唯一编号（UUID 前 8 位）
    private String name;            // 球队名称
    private String group;           // 所属小组 (A-H)，世界杯分组标签
    private String logo;            // 队徽图片路径，可为空
    private String coach;           // 主教练姓名
    private List<String> players;   // 球员名单
    private String homeStadium;     // 主场名称

    /** 无参构造器（Gson 反序列化需要） */
    public Team() {
        // 取 UUID 前 8 位作为短 ID（课程项目数据量小，碰撞概率可忽略）
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.players = new ArrayList<>();
    }

    /**
     * 完整构造器
     * @param name        球队名称
     * @param coach       主教练姓名
     * @param homeStadium 主场名称
     */
    public Team(String name, String coach, String homeStadium) {
        this();
        this.name = name;
        this.coach = coach;
        this.homeStadium = homeStadium;
    }

    // ========== Getter / Setter ==========

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }

    public List<String> getPlayers() { return players; }
    public void setPlayers(List<String> players) { this.players = players; }

    public String getHomeStadium() { return homeStadium; }
    public void setHomeStadium(String homeStadium) { this.homeStadium = homeStadium; }

    /** 获取球员人数 */
    public int getPlayerCount() {
        return players == null ? 0 : players.size();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id != null && id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
