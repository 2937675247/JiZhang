@echo off
echo 正在以离线模式构建项目...

REM 设置环境变量
set ANDROID_HOME=C:\Android
set PATH=%PATH%;%ANDROID_HOME%\platform-tools

REM 清理之前的构建
echo 清理之前的构建...
call gradlew clean --offline

REM 使用离线模式构建
echo 正在构建项目（离线模式）...
call gradlew build --offline --stacktrace --info

echo 构建完成！如果成功，APK应该位于app/build/outputs/apk/目录
pause 