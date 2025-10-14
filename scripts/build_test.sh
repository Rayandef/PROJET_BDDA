mkdir -p bin
javac -d bin src/bdda/config/DBConfig.java src/bdda/config/PageID.java src/bdda/test/TestDBConfig.java src/bdda/config/DiskManager.java src/bdda/test/DiskManagerTests.java src/bdda/config/BufferManager.java src/bdda/test/BufferManagerTests.java src/bdda/Test.java
javac -d bin -cp src/bdda/Test.java
java -cp bin bdda.Test