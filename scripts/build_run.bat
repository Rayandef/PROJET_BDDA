@echo off
if not exist bin mkdir bin
javac -d bin src\bdda\config\*
java -cp bin bdda/config/SGBD