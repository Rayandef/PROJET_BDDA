echo off
if not exist bin mkdir bin
javac -d bin src\bdda\*.java
javac -d bin src\bdda\**\*.java
java -cp bin dbba.Main