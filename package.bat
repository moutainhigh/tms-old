%~d0
cd %~dp0
call mvn clean package -Dmaven.test.skip=true
cd target
copy ..\ear\*.ear .\
jar uf tms-webapp.ear tms-webapp.war
echo �����ɣ��������ݿ�ű�...
cd ..
call exportSQL.bat
pause
