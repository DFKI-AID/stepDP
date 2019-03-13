package de.dfki.app;


import de.dfki.step.dialog.MyDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 * Entry point.
 * We define here packages that are scanned for spring components. e.g. for adding additional rest-controller
 * to the web api
 */
@SpringBootApplication(scanBasePackages = {"de.dfki.step.web"})
public class MyApp {
    private static final Logger log = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext ctx = SpringApplication.run(MyApp.class, args);
    }
}

