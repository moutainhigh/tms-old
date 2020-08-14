@echo off
rem Get the current system time
set x=%TIME%

rem Uncomment the next line to see what the real time is, which includes milliseconds
rem echo %x%

rem Find the token ':' in the value 'x'
rem The system time is returned like this: 10:13:29.40
rem so we have to break it out using the 'cut' function in DOS
for /f "tokens=1,2 delims=:" %%m in ('echo %x%') do set hour=%%m
for /f "tokens=2,3 delims=:" %%m in ('echo %x%') do set min=%%m
for /f "tokens=3,4 delims=:" %%m in ('echo %x%') do set s=%%m
for /f "tokens=1,2 delims=." %%m in ('echo %s%') do set sec=%%m
for /f "tokens=2,3 delims=." %%m in ('echo %s%') do set msec=%%m
echo %DATE:~0,4%%DATE:~5,2%%DATE:~8,2%%hour%%min%%sec%%msec%
