package de.dfki.tocalog.telegram;

import de.dfki.tocalog.input.TextInput;
import de.dfki.tocalog.framework.Event;
import de.dfki.tocalog.framework.EventEngine;
import de.dfki.tocalog.framework.InputComponent;
import de.dfki.tocalog.kb.EKnowledgeMap;
import de.dfki.tocalog.model.Service;
import de.dfki.tocalog.output.Output;
import de.dfki.tocalog.output.OutputComponent;
import de.dfki.tocalog.output.TextOutput;
import de.dfki.tocalog.output.impp.AllocationState;
import de.dfki.tocalog.rasa.RasaHelper;
import de.dfki.tocalog.rasa.RasaResponse;
import de.dfki.tocalog.wiki.WikiMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 */
public class TelegramBot extends TelegramLongPollingBot implements InputComponent, OutputComponent {
    private static Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private Pattern commandPattern = Pattern.compile("/([a-zA-Z0-9_]+)\\s*([a-zA-Z0-9\\s]*)");
    private WikiMedia wikiMedia;// = new WikiMedia();
    private Context context;
    private TelegramBotsApi botsApi;
    private long lastChatId;

    static {
        ApiContextInitializer.init();
    }

    public void start() {

        botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(lastChatId)
                .setText(msg);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        // check if we received a text message and forward it as TextInput
        if (!update.hasMessage()) {
            return;
        }

        Message msg = update.getMessage();
        if (!msg.hasText()) {
            return;
        }
        String text = msg.getText();

        Long userId = msg.getChatId();
        EKnowledgeMap<Service> km = context.getKnowledgeBase().getKnowledgeMap(Service.class);
        Service service = Service.create().setType("telegram").setId(userId.toString());
        km.add(service);

        TextInput ti = new TextInput(text);
        ti.setSource(msg.getFrom().getFirstName());
        context.getEventEngine().submit(Event.build(ti).setSource(TelegramBot.class.getSimpleName()).build());
//        Matcher matcher = commandPattern.matcher(text);
//        if (!matcher.matches()) {
//            return;
//        }
//        String command = matcher.group(1);
//        String arg = matcher.group(2);
//        log.info("received command {} {}", command, arg);
//
//
//        User user = msg.getFrom();
//        if (user == null) {
//            return;
//        }
//
//        if (command.equals("gender")) {
//            handleGenderCommand(update);
//        }
//
//
//        if (command.equals("rasa")) {
//            handleRasaCommand(update, arg);
//        }

    }

    private void sendText(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText(text);
        execute(message);
    }

    private void sendTextAsync(long chatId, String text, SentCallback<Message> callback) throws TelegramApiException {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText(text);
        executeAsync(message, callback);
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
        RasaHelper rsh = new RasaHelper();
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

    @Override
    public void init(Context context) {
        this.context = context;
//        EKnowledgeMap<Service> ks = context.getKnowledgeBase().getKnowledgeMap(Service.class);
//        Service s = Service.create();
//        s.setType("telegram");
//        s.setId(UUID.randomUUID().toString());
//        ks.add(s);
    }

    @Override
    public void onEvent(EventEngine engine, Event event) {

    }


    private Map<String, AllocationState> allocationStates = new HashMap<>();

    private synchronized void setAllocationState(String id, AllocationState allocationState) {
        this.allocationStates.put(id, allocationState);
    }

    @Override
    public String allocate(Output output, Service service) {
        String id = UUID.randomUUID().toString();
        if (!handles(output, service)) {
            String errMsg = String.format("Can't output {} on {}", output, service);
            log.warn(errMsg);
            setAllocationState(id, new AllocationState(AllocationState.State.ERROR, new IllegalArgumentException(errMsg)));
            return id;
        }

        TextOutput to = (TextOutput) output;
        try {
            setAllocationState(id, new AllocationState(AllocationState.State.INIT));
            //TODO is this blocking?

            sendTextAsync(Long.parseLong(service.getId().get()), to.getText(), new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> method, Message response) {
                    setAllocationState(id, new AllocationState(AllocationState.State.SUCCESS));
                }

                @Override
                public void onError(BotApiMethod<Message> method, TelegramApiRequestException apiException) {
                    setAllocationState(id, new AllocationState(AllocationState.State.ERROR, apiException));
                }

                @Override
                public void onException(BotApiMethod<Message> method, Exception exception) {
                    setAllocationState(id, new AllocationState(AllocationState.State.ERROR, exception));
                }
            });
        } catch (TelegramApiException e) {
            e.printStackTrace();
            setAllocationState(id, new AllocationState(AllocationState.State.ERROR, e));
        }
        return id;
    }

    @Override
    public synchronized AllocationState getState(String id) {
        if(!allocationStates.containsKey(id)) {
            return new AllocationState(AllocationState.State.NONE);
        }
        return allocationStates.get(id);
    }

    @Override
    public boolean handles(Output output, Service service) {
        if (!(output instanceof TextOutput)) {
            return false;
        }
        if (!service.getType().isPresent()) {
            return false;
        }
        if (!service.getId().isPresent()) {
            return false;
        }
        return service.getType().get().equals("telegram");
    }
}
