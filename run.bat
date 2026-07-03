@echo off
chcp 65001 >nul
echo ========================================
echo   足球联赛积分管理系统 - 运行脚本
echo ========================================
echo.

:: 检查是否已编译
if not exist out\league\Main.class (
    echo [提示] 尚未编译，请先运行 compile.bat 编译
    echo.
    pause
    exit /b
)

echo [启动] 正在启动应用程序...
echo.

java -cp "out;lib\gson-2.10.1.jar" league.Main

pause
