package de.dfki.step.web;

import de.dfki.step.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
//@Component
//@ConfigurationProperties("dialog")
@Configuration
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    public static Dialog dialog;

    @Value("${dialog.name}")
    public String name;

    @Autowired
    private ApplicationContext appContext;

    @Bean
    public Dialog getDialog() {
        return dialog;
    }

    public void initDialog() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class dialogClass = Class.forName(name);
        AppConfig.dialog = (Dialog) dialogClass.getConstructor().newInstance();

        Thread dialogThread = new Thread(dialog);
        dialogThread.setDaemon(true);
        dialogThread.start();
    }

    @PostConstruct
    public void init() {
        try {
            initDialog();
        } catch (Exception e) {
            log.error("Could not load dialog class with name '{}'. Cause: {}", name, e.getMessage(), e);
            int ec = SpringApplication.exit(appContext, () -> 1);
            System.exit(ec);
        }
    }


}
