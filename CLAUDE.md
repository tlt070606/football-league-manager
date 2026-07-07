# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建与运行

```batch
:: 编译（Windows）
compile.bat

:: 或手动
javac -encoding UTF-8 -cp "lib/gson-2.10.1.jar" -d out src/league/Main.java src/league/model/*.java src/league/service/*.java src/league/ui/*.java src/league/util/*.java

:: 运行
run.bat
:: 或手动
java -cp "out;lib/gson-2.10.1.jar" league.Main
```

- JDK 8+，唯一外部依赖 `lib/gson-2.10.1.jar`
- 源码编码 UTF-8（中文注释和字符串）
- 编译输出到 `out/`，运行数据写入 `data/`（JSON），队徽图片存 `assets/logos/`

## 架构概览

Java Swing 桌面应用，世界杯小组赛积分管理系统。三个标签页：球队管理 → 比赛管理 → 积分榜。

**分层结构：**

```
src/league/
├── Main.java                   # 入口，SwingUtilities.invokeLater 启动 MainFrame
├── model/                      # 数据对象（POJO，Gson 序列化）
│   ├── Team.java               # id(UUID前8位), name, group(A-H), logo路径, coach, players, homeStadium
│   ├── Match.java              # round, homeTeamId/awayTeamId(存ID非引用), 比分, played标志；-1=未赛
│   └── Standing.java           # 统计: 场次/胜平负/进球/失球/净胜球/积分
├── service/
│   ├── LeagueManager.java      # 单例核心控制器，球队CRUD、赛程生成、比分录入、积分计算、随机模拟
│   └── FileManager.java        # Gson JSON 持久化，读写 data/teams.json 和 data/matches.json
├── ui/
│   ├── MainFrame.java          # JFrame + JTabbedPane + 菜单栏（重置数据/退出）
│   ├── TeamPanel.java          # 球队列表表格 + 搜索/添加/编辑/删除，双击行编辑
│   ├── MatchPanel.java         # 按轮次查看比赛、生成赛程、录入比分、一键随机模拟、重置
│   ├── StandingPanel.java      # 积分表格（按小组筛选） + BarChartPanel 柱状图
│   ├── BarChartPanel.java      # Graphics2D 手绘柱状图，前3金银铜色，渐变柱体
│   ├── TeamDialog.java         # 球队编辑对话框
│   └── ScoreDialog.java        # 比分录入对话框
└── util/
    ├── ScheduleGenerator.java  # 圆桌旋转算法生成主客场双循环赛程
    └── LogoGenerator.java      # 自动生成 128×128 圆形队徽 PNG
```

**核心设计决策：**

- **LeagueManager 单例** — 所有 UI 面板通过 `LeagueManager.getInstance()` 获取同一实例；Swing 单线程无需同步
- **Match 存 teamId 不存 Team 引用** — 避免 Gson 循环引用和冗余序列化
- **积分榜全量重算** — `updateStandings()` 每次遍历所有已完赛比赛，非增量更新（数据量小，足够）
- **赛程算法** — 圆桌旋转(Round-Robin)，固定首队、其余环形旋转，前半程N-1轮 + 后半程主客交换N-1轮；奇数队自动补 null 轮空
- **数据持久化** — 每次增删改后自动调用 `saveData()`；启动时检查 `data/teams.json` 是否存在，有则加载，无则初始化 32 队预设数据
- **32 支世界杯国家队** — 硬编码在 `LeagueManager.initPresetData()` 中，8 组 × 4 队，含真实教练、主场、球员名单

**排名规则**（`LeagueManager.updateStandings()` 中的 Comparator 链）：
1. 积分高 → 2. 净胜球多 → 3. 进球数多 → 4. 队名首字母序

## 数据流

```
用户操作 → LeagueManager 方法 → 更新内存(Map/List) → FileManager.saveXxx() → data/*.json
                                                      → UI 面板 refreshData() 重新读取
```

`MainFrame` 的标签页切换监听器自动触发各面板 `refreshData()`，确保跨面板数据一致。
