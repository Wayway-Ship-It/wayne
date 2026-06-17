@echo off
chcp 65001
echo ========================================
echo 汽车配件仓库管理系统 - 后端启动脚本
echo ========================================
echo.

echo [1/3] 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java，请先安装JDK 1.8或更高版本
    pause
    exit /b 1
)
echo [成功] Java环境检查通过
echo.

echo [2/3] 检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo [错误] 未检测到Maven，请先安装Maven
    pause
    exit /b 1
)
echo [成功] Maven环境检查通过
echo.

echo [3/3] 启动后端服务...
echo 请确保：
echo   1. MySQL服务已启动
echo   2. 数据库已创建并执行了database/init.sql
echo   3. application.yml中的数据库配置已修改正确
echo.
echo 后端服务将在 http://localhost:8080 启动
echo 按 Ctrl+C 停止服务
echo.
pause

cd auto-parts-warehouse-backend
mvn spring-boot:run

pause
