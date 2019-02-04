package de.dfki.rs.web;

import de.dfki.rs.Dialog;
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
        public final Dialog app = new Dialog();

        public Settings() {
            Thread appThread = new Thread(app);
            appThread.setDaemon(true);
            appThread.start();
        }
    }
}
