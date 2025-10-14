package bdda;

import bdda.test.TestDBConfig;
import bdda.test.DiskManagerTests;
import bdda.test.BufferManagerTests;

public class Test {
    public static void main(String[] args){
        TestDBConfig.runTests();
        DiskManagerTests.main(args);
        BufferManagerTests.main(args);
        System.out.println("FIN");
    }
}
