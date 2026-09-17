@echo off
REM Compile and run the Smart Student Management System (Windows)
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
java -cp out com.sms.app.Main
