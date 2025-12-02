package bdda.test;

import bdda.config.*;

public class SGBDTests {

    public static void main(String[] args) {
        SGBD sgbd = new SGBD(new DBConfig());
        sgbd.Run();
    }
}
