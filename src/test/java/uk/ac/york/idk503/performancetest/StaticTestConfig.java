package uk.ac.york.idk503.performancetest;

import java.util.Random;

public class StaticTestConfig {
    public static final int TARGET = new Random().nextInt(90, 100);
    public static final int REST_TARGET = new Random().nextInt(90, 100);

    static {
        final int SIZE = new Random().nextInt(900, 1_000);
        System.setProperty("size", String.valueOf(SIZE));
        System.setProperty("target", String.valueOf(TARGET));
        System.setProperty("restTarget", String.valueOf(REST_TARGET));
    }
}
