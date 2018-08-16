package de.dfki.tocalog.telegram;

import de.dfki.tocalog.input.rasa.RasaInputHandler;
import de.dfki.tocalog.input.rasa.RasaResponse;
import de.dfki.tocalog.wiki.WikiMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class TelegramBot extends TelegramLongPollingBot {
    private static Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private Pattern commandPattern = Pattern.compile("/([a-zA-Z0-9_]+)\\s*([a-zA-Z0-9\\s]*)");
    private WikiMedia wikiMedia = new WikiMedia();

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (!update.hasMessage()) {
            return;
        }


        Message msg = update.getMessage();
        if (!msg.hasText()) {
            return;
        }
        String text = msg.getText();
        Matcher matcher = commandPattern.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        String command = matcher.group(1);
        String arg = matcher.group(2);
        log.info("received command {} {}", command, arg);


        User user = msg.getFrom();
        if (user == null) {
            return;
        }

        if(command.equals("gender")) {
            handleGenderCommand(update);
        }


        if(command.equals("rasa")) {
            handleRasaCommand(update, arg);
        }

    }

    private void handleGenderCommand(Update update) {
        User user = update.getMessage().getFrom();
        String rsp = String.format("%s male=%s female=%s", user.getFirstName(),
                wikiMedia.isMaleName(user.getFirstName()), wikiMedia.isFemaleName(user.getFirstName()));
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(update.getMessage().getChatId())
                .setText(rsp);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleRasaCommand(Update update, String arg) {
        RasaInputHandler rsh = new RasaInputHandler() {
            @Override
            public void handleIntent(RasaResponse response) {

            }
        };
        try {
            String rasaJson = rsh.nlu(arg);
            RasaResponse rasaRsp = rsh.parseJson(rasaJson);

            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText(rasaRsp.toString());
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public String getBotUsername() {
        return "tocalog_bot";
    }

    @Override
    public String getBotToken() {
        return "620685796:AAGaZtrIThApilcLvNBNyqnhJEMNDyDpZl4";
    }
}
