package de.dfki.app;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * Entry point.
 * We define here packages that are scanned for spring components. e.g. for adding additional rest-controller
 * to the web api
 */
@SpringBootApplication(scanBasePackages = {"de.dfki.pdp.web"})
public class MyApp {
    private static final Logger log = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) throws IOException {
        SpringApplication.run(MyApp.class, args);
    }


}

