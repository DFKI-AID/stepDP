package de.dfki.rengine.web;

import de.dfki.app.DialogApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 */
@Configuration
public class AppConfig {
    @Bean
    public Settings getSettings() {
        return new Settings();
    }

    public static class Settings {
        public final DialogApp app = new DialogApp();

        public Settings() {
            Thread appThread = new Thread(app);
            appThread.setDaemon(true);
            appThread.start();
        }
    }
}
