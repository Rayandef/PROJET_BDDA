@echo off
if not exist bin mkdir bin
javac -d bin -cp src src\bdda\Test.java
javac -d bin src\bdda\config\DBConfig.java
javac -d bin src\bdda\test\TestDBConfig.java 
java -cp bin;config bdda.Test