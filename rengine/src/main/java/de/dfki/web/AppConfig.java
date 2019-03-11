package de.dfki.web;

import de.dfki.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }


}
