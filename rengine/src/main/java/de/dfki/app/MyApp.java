package de.dfki.app;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 *
 */
//@Component
@SpringBootApplication(scanBasePackages = {"de.dfki.pdp.web"})
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

