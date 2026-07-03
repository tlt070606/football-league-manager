@echo off
chcp 65001 >nul
echo ========================================
echo   足球联赛积分管理系统 - 编译脚本
echo ========================================
echo.

:: 清理旧编译文件
if exist out (
    echo [清理] 删除旧的编译输出...
    rmdir /s /q out
)

:: 创建输出目录
mkdir out

:: 收集所有 Java 源文件
echo [编译] 正在收集源文件...
dir /s /b src\*.java > sources.txt

:: 编译（-encoding UTF-8 确保中文注释正确处理）
echo [编译] 正在编译...
javac -encoding UTF-8 -cp "lib\gson-2.10.1.jar" -d out @sources.txt

:: 清理临时文件
del sources.txt

:: 检查编译结果
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   编译成功！运行 run.bat 启动程序
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   编译失败！请检查上方错误信息
    echo ========================================
)

pause
