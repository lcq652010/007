@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  JSON 工具 - 编译脚本
echo  Java 8 兼容版本
echo ========================================
echo.

set JAVA_HOME=
set JAVAC=javac
set JAR=jar

%JAVAC% -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 javac 命令，请确保已安装 JDK 并配置好 PATH 环境变量
    echo.
    echo 请确保:
    echo   1. 已安装 JDK 8 或更高版本
    echo   2. 将 JAVA_HOME\bin 添加到 PATH 环境变量
    echo   3. 或者手动设置 JAVA_HOME 在此脚本中
    pause
    exit /b 1
)

echo [信息] 编译器版本:
%JAVAC% -version
echo.

set SRC_DIR=src
set OUT_DIR=out
set CLASS_DIR=%OUT_DIR%\classes
set JAR_DIR=%OUT_DIR%\jar
set JAR_NAME=json-tool.jar

if exist "%OUT_DIR%" (
    echo [信息] 清理旧的输出目录...
    rmdir /s /q "%OUT_DIR%"
)

echo [信息] 创建输出目录...
mkdir "%CLASS_DIR%"
mkdir "%JAR_DIR%"
echo.

echo [信息] 编译 Java 源代码...
echo 源目录: %SRC_DIR%
echo 输出目录: %CLASS_DIR%
echo.

set SOURCE_FILES=
for /r "%SRC_DIR%" %%f in (*.java) do (
    set SOURCE_FILES=!SOURCE_FILES! "%%f"
)

%JAVAC% -encoding UTF-8 -d "%CLASS_DIR%" -sourcepath "%SRC_DIR%" %SOURCE_FILES%

if errorlevel 1 (
    echo.
    echo [错误] 编译失败！
    pause
    exit /b 1
)

echo.
echo [信息] 编译成功！
echo.

echo [信息] 创建 JAR 包...
echo 主类: com.jsontool.JsonToolMain
echo.

echo Manifest-Version: 1.0 > "%JAR_DIR%\manifest.txt"
echo Main-Class: com.jsontool.JsonToolMain >> "%JAR_DIR%\manifest.txt"
echo Created-By: Java JSON Tool Builder >> "%JAR_DIR%\manifest.txt"

%JAR% cvfm "%JAR_DIR%\%JAR_NAME%" "%JAR_DIR%\manifest.txt" -C "%CLASS_DIR%" .

if errorlevel 1 (
    echo.
    echo [错误] 创建 JAR 包失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo  编译完成！
echo ========================================
echo.
echo 输出文件:
echo   %CD%\%JAR_DIR%\%JAR_NAME%
echo.
echo 使用方法:
echo   1. 显示帮助:
echo      java -jar "%JAR_DIR%\%JAR_NAME%" --help
echo.
echo   2. 校验 JSON 文件:
echo      java -jar "%JAR_DIR%\%JAR_NAME%" -v test\valid.json
echo.
echo   3. 格式化 JSON 文件:
echo      java -jar "%JAR_DIR%\%JAR_NAME%" -f -w test\valid.json
echo.
echo   4. 压缩 JSON 文件:
echo      java -jar "%JAR_DIR%\%JAR_NAME%" -f -m -w test\valid.json
echo.
pause
