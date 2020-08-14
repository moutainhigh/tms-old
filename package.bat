%~d0
cd %~dp0
call mvn clean package -Dmaven.test.skip=true
cd target
copy ..\ear\*.ear .\
jar uf tms-webapp.ear tms-webapp.war
echo 打包完成，导出数据库脚本...
cd ..
call exportSQL.bat
pause
