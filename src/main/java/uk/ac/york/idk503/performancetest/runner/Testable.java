package uk.ac.york.idk503.performancetest.runner;

import java.util.Objects;

public interface Testable {
    static final String BASE_URL = "http://localhost:8080/api/metadata/%s/%d";
    static final short RUNTIME_IN_SECONDS =
        Objects.nonNull(System.getProperty("runtimeInSeconds")) ?
                Short.valueOf(System.getProperty("runtimeInSeconds")) : 60;
    static final int TARGET =
            Objects.nonNull(System.getProperty("target")) ?
                    Integer.valueOf(System.getProperty("target")) : 100000;
    static final int REST_TARGET =
            Objects.nonNull(System.getProperty("restTarget")) ?
                    Integer.valueOf(System.getProperty("restTarget")) : 50000;
    public boolean test();
}
