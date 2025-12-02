package bdda.test;

import bdda.config.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class SGBDTests {

    public static void testSGBD(){
        SGBD sgbd = new SGBD(new DBConfig());
        final InputStream is = System.in;

        String cmd = "CREATE TABLE Tab1 (C1:FLOAT,C2:INT)\n";
        ByteArrayInputStream bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        cmd = "CREATE TABLE Tab2 (C7:CHAR(5),AA:VARCHAR(2))\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);
    
        cmd = "CREATE TABLE Tab3 (Toto:CHAR(120))\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        cmd = "DESCRIBE TABLE Tab1\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        cmd = "DESCRIBE TABLES\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        cmd = "DROP TABLE Tab1\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        cmd = "DESCRIBE TABLES\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        cmd = "EXIT\n";
        bis = new ByteArrayInputStream(cmd.getBytes());
        System.setIn(bis);
        System.setIn(is);

        sgbd.Run();
    }
    public static void main(String[] args) {
        testSGBD();
    }
}
