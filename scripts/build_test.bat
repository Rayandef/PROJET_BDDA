@echo off
if not exist bin mkdir bin
javac src\bdda\config\DBConfig.java src\bdda\config\PageID.java src\bdda\test\TestDBConfig.java src\bdda\config\DiskManager.java src\bdda\test\DiskManagerTests.java src\bdda\config\BufferManager.java src\bdda\test\BufferManagerTests.java src\bdda\Test.java
java -cp bin;config bdda.Test