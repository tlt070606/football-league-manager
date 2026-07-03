# 足球联赛积分管理系统

面向对象课程设计 — Java + Swing 实现

## 环境要求

- JDK 8 或更高版本
- Windows / macOS / Linux

## 快速开始

```
1. 双击 compile.bat 编译（或在终端运行 javac 命令）
2. 双击 run.bat 运行
```

或手动命令：

```batch
:: 编译
javac -encoding UTF-8 -cp "lib/gson-2.10.1.jar" -d out src/league/Main.java src/league/model/*.java src/league/service/*.java src/league/ui/*.java src/league/util/*.java

:: 运行
java -cp "out;lib/gson-2.10.1.jar" league.Main
```

## 功能说明

| 模块 | 功能 |
|---|---|
| 球队管理 | 添加、编辑、删除、搜索球队（名称/教练/主场/球员名单） |
| 比赛管理 | 自动生成主客场双循环赛程、按轮次查看、录入比分 |
| 积分榜 | 实时排名表格 + 柱状图可视化（积分/净胜球/进球排名） |

## 排名规则

1. 积分高者在前（胜3分、平1分、负0分）
2. 积分相同 → 净胜球多者在前
3. 净胜球相同 → 进球数多者在前
4. 仍相同 → 并列排名

## 项目结构

```
├── src/league/
│   ├── Main.java              # 程序入口
│   ├── model/
│   │   ├── Team.java          # 球队模型
│   │   ├── Match.java         # 比赛模型
│   │   └── Standing.java      # 积分记录模型
│   ├── service/
│   │   ├── LeagueManager.java # 核心业务逻辑（单例）
│   │   └── FileManager.java   # JSON 数据读写
│   ├── ui/
│   │   ├── MainFrame.java     # 主窗口
│   │   ├── TeamPanel.java     # 球队管理面板
│   │   ├── MatchPanel.java    # 比赛管理面板
│   │   ├── StandingPanel.java # 积分榜面板
│   │   ├── BarChartPanel.java # 柱状图组件
│   │   ├── TeamDialog.java    # 球队编辑对话框
│   │   └── ScoreDialog.java   # 比分录入对话框
│   └── util/
│       └── ScheduleGenerator.java # 赛程生成算法
├── lib/
│   └── gson-2.10.1.jar       # JSON 处理库
├── data/                      # 运行时数据（自动生成）
├── compile.bat
├── run.bat
└── README.md
```

## 测试数据

首次运行自动加载 8 支中超球队：上海海港、山东泰山、北京国安、上海申花、成都蓉城、武汉三镇、天津津门虎、浙江队。

进入"比赛管理" → 点击"生成赛程" → 自动生成 14 轮 56 场双循环赛程。
