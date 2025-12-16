package bdda;

import bdda.config.DBConfig;
import bdda.config.SGBD;

public class Main{
    public static void main(String[] args){
        SGBD sgbd = new SGBD(new DBConfig());
        sgbd.Run();
    }
}