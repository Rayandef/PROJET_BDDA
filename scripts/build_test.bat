@echo off
if not exist bin mkdir bin
javac -d bin -cp src src\bdda\config\* src\bdda\test\*
java -cp bin bdda/test/BufferManagerTests
java -cp bin bdda/test/DiskManagerTests
java -cp bin bdda/test/RelationTests
java -cp bin bdda/test/TestDBConfig
java -cp bin bdda/test/SGBDTests