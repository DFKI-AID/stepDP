package de.dfki.rengine.web;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 */
@SpringBootApplication
public class App implements ApplicationRunner {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        synchronized (this) {
            this.wait();
        }
    }
}
