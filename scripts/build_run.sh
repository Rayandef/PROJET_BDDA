mkdir -p bin
javac -d bin src/bdda/config/*
java -cp bin bdda/config/SGBD
