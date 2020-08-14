@echo off
%~d0
cd %~dp0
call java -Dfile.encoding=UTF-8 -jar build/JSBuilder2.jar --projectFile %~dp0ext.jsb2 --homeDir deploy
rem �˴��ע�ͣ������Ҫ�����ļ�������ȥ����һ�е�rem
xcopy .\deploy\ext-3.4+\* . /s /h /r /y /exclude:uncopy.txt
call java -Dfile.encoding=UTF-8 -jar build/CleanJavascript.jar %~dp0ext-all-debug.js %~dp0ext-all-debug.js 
pause