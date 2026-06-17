@echo off
chcp 65001
echo ========================================
echo 汽车配件仓库管理系统 - 前端启动脚本
echo ========================================
echo.

echo [1/3] 检查Node.js环境...
node -v
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js，请先安装Node.js 14或更高版本
    pause
    exit /b 1
)
echo [成功] Node.js环境检查通过
echo.

echo [2/3] 检查npm环境...
npm -v
if %errorlevel% neq 0 (
    echo [错误] npm异常
    pause
    exit /b 1
)
echo [成功] npm环境检查通过
echo.

echo [3/3] 启动前端服务...
echo 请确保后端服务已先启动
echo 前端服务将在 http://localhost:8081 启动
echo 按 Ctrl+C 停止服务
echo.
pause

cd auto-parts-warehouse-frontend

if not exist "node_modules" (
    echo 首次运行，正在安装依赖...
    echo 使用淘宝镜像加速下载...
    npm install --registry=https://registry.npmmirror.com
    if %errorlevel% neq 0 (
        echo [错误] 依赖安装失败
        pause
        exit /b 1
    )
)

npm run serve

pause
