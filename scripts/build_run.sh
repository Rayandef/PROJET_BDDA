mkdir -p bin
javac -d bin src/bdda/Main.java src/bdda/config/DBConfig.java src/bdda/test/TestDBConfig.java
java -cp bin bdda.Main
