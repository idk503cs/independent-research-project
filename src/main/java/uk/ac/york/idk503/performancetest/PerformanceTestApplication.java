package uk.ac.york.idk503.performancetest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

@SpringBootApplication
public class PerformanceTestApplication {
	private static final Logger LOG = LoggerFactory.getLogger(PerformanceTestApplication.class);
	private static ConfigurableApplicationContext configurableApplicationContext;

	public static void main(String[] args) {
		configurableApplicationContext = SpringApplication.run(PerformanceTestApplication.class, args);
	}

	public static void shutdown() {
		try {
			configurableApplicationContext.stop();
			configurableApplicationContext.close();
		} catch(Exception e) {
			LOG.error(e.getMessage());
			System.exit(0);
		}
	}

	public static void terminate() {
		try {
			if(new File("STOP.file").createNewFile()){
				configurableApplicationContext.stop();
				configurableApplicationContext.close();
			} else {
				LOG.error("STOP.file failed to create");
			}
		} catch(Exception e) {
			LOG.error(e.getMessage());
			System.exit(0);
		}
	}
}
