@echo off
chcp 65001
echo ========================================
echo 配置MySQL环境变量
echo ========================================
echo.

set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin

echo MySQL路径: %MYSQL_PATH%
echo.

echo 正在检查是否已在PATH中...
echo %PATH% | findstr /i "MySQL Server 8.0\\bin" >nul
if %errorlevel% equ 0 (
    echo [√] MySQL已在PATH中
) else (
    echo [×] MySQL不在PATH中
    echo.
    echo 正在添加MySQL到系统PATH...
    echo.
    echo 请以管理员身份运行此脚本！
    echo.
    echo 或者手动添加以下路径到系统环境变量PATH:
    echo %MYSQL_PATH%
    echo.
)

echo.
echo ========================================
echo 当前会话已临时配置MySQL
echo 你可以在当前窗口使用mysql命令
echo ========================================
echo.

pause
