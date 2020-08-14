%~d0
cd %~dp0
call mvn compile
call mvn exec:java -Dexec.mainClass="org.nw.deploy.SQLExporter" -Dexec.args="true" -Dexec.classpathScope=runtime
pause
