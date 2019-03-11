package de.dfki.app;


import de.dfki.dialog.Dialog;
import de.dfki.dialog.MetaFactory;
import de.dfki.dialog.TimeBehavior;
import de.dfki.web.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 */
//@Component
@SpringBootApplication(scanBasePackages = {"de.dfki.web"})
public class MyApp {
    //    @Autowired
//    private AppConfig config;
    private static final Logger log = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) throws IOException {
        SpringApplication.run(MyApp.class, args);
    }

//    @Override
//    public void run(ApplicationArguments args) throws Exception {
////        Dialog dialog = config.getDialog();
////        dialog.run();
//
//        System.out.println("HURR");
//        synchronized (this) {
//            this.wait();
//        }
//    }

}

