@echo off
if not exist bin mkdir bin
javac -d bin -cp src src\bdda\Main.java 
javac -d bin src\bdda\config\DBConfig.java 
javac -d bin src\bdda\util\IO.java
java -cp bin;config bdda.Main