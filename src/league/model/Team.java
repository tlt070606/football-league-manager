package league.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 球队模型类
 * 存储球队的基本信息：名称、队徽、教练、球员名单、主场
 */
public class Team {
    private String id;
    private String name;
    private String logo;        // 队徽图片路径，可为空
    private String coach;
    private List<String> players;
    private String homeStadium;

    /** 无参构造器（Gson 反序列化需要） */
    public Team() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.players = new ArrayList<>();
    }

    /** 完整构造器 */
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
