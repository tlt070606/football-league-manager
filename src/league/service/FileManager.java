package league.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import league.model.Match;
import league.model.Team;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * 文件管理类 — 负责 JSON 数据的持久化读写
 *
 * 使用 Gson 库将 Java 对象序列化为 JSON 文件，以及反序列化读取。
 * 数据文件存放在项目根目录的 data/ 文件夹下。
 */
public class FileManager {
    /** 数据存储目录（相对于 JVM 工作目录，即项目根目录） */
    private static final String DATA_DIR = "data";
    /** 球队数据文件名 */
    private static final String TEAMS_FILE = "teams.json";
    /** 比赛数据文件名 */
    private static final String MATCHES_FILE = "matches.json";

    /** 配置了 prettyPrinting 的 Gson 实例，JSON 输出格式化便于人工查看 */
    private final Gson gson;

    public FileManager() {
        // prettyPrinting: 让 JSON 文件格式化输出，方便人工查看和调试
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDataDir();
    }

    /** 确保 data 目录存在 */
    private void ensureDataDir() {
        Path dir = Paths.get(DATA_DIR);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                System.err.println("无法创建数据目录: " + e.getMessage());
            }
        }
    }

    // ==================== 球队数据 ====================

    /**
     * 保存球队数据到 JSON 文件
     * @param teams 球队 Map（key=ID, value=Team 对象）
     */
    public void saveTeams(Map<String, Team> teams) {
        List<Team> teamList = new ArrayList<>(teams.values());
        writeJson(teamList, TEAMS_FILE);
    }

    /**
     * 从 JSON 文件加载球队数据
     * @return Map<球队ID, Team>，若无数据则返回空 Map
     */
    public Map<String, Team> loadTeams() {
        List<Team> teamList = readJsonList(TEAMS_FILE, Team.class);
        Map<String, Team> map = new LinkedHashMap<>();
        if (teamList != null) {
            for (Team t : teamList) {
                map.put(t.getId(), t);
            }
        }
        return map;
    }

    // ==================== 比赛数据 ====================

    /**
     * 保存比赛数据到 JSON 文件
     * @param matches 待保存的比赛列表
     */
    public void saveMatches(List<Match> matches) {
        writeJson(matches, MATCHES_FILE);
    }

    /**
     * 从 JSON 文件加载比赛数据
     * @return 比赛列表，若无数据则返回空列表
     */
    public List<Match> loadMatches() {
        List<Match> list = readJsonList(MATCHES_FILE, Match.class);
        return list != null ? list : new ArrayList<>();
    }

    // ==================== 内部工具方法 ====================

    /** 将对象序列化为 JSON 写入文件 */
    private void writeJson(Object obj, String filename) {
        Path filePath = Paths.get(DATA_DIR, filename);
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            gson.toJson(obj, writer);
        } catch (IOException e) {
            System.err.println("写入文件失败 " + filename + ": " + e.getMessage());
        }
    }

    /** 从 JSON 文件读取并反序列化为 List */
    private <T> List<T> readJsonList(String filename, Class<T> clazz) {
        Path filePath = Paths.get(DATA_DIR, filename);
        if (!Files.exists(filePath)) {
            return null;
        }
        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            Type listType = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("读取文件失败 " + filename + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查数据文件是否存在（只检查 teams.json）
     * @return true 表示已有球队数据文件
     */
    public boolean hasExistingData() {
        return Files.exists(Paths.get(DATA_DIR, TEAMS_FILE));
    }
}
