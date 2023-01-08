package puga_tmsk.puga_bot.service;

import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import puga_tmsk.puga_bot.config.BotConfig;
import puga_tmsk.puga_bot.model.*;
import puga_tmsk.puga_bot.service.apps.CheckPidor;
import puga_tmsk.puga_bot.service.apps.UserActions;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;
import puga_tmsk.puga_bot.service.keyboards.ReplyKeyboards;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@Component
@Data
@Getter
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private long chatId;
    private CheckPidor checkPidor = new CheckPidor(this);
    private UserActions userActions = new UserActions(this);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDataRepository userDataRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;


    private static final String HELP_TEXT = "Pidor scanner v1.0";

    ReplyKeyboards replyKeyboards = new ReplyKeyboards();
    InLineKeyboards inLineKeyboards = new InLineKeyboards();

    public TelegramBot(BotConfig config) {

        this.config = config;
        List<BotCommand> menu = new ArrayList<>();
        //menu.add(new BotCommand("/start", "Запустить бота"));
        menu.add(new BotCommand("/mydata", "Данные обо мне"));
        //menu.add(new BotCommand("/help", "Помощь"));

        try {
            this.execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: " + e.getMessage());
        }

        checkPidor.startCheckPidor();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        long userId;
        String messageText;
        String userFirstName;
        String userName;

        Calendar nowDate = Calendar.getInstance();
        log.info(new Timestamp(System.currentTimeMillis()) + " [MAIN] Update recieved: " + nowDate.getTime());
        nowDate.setTimeZone(TimeZone.getTimeZone(config.getTimeZone()));
        log.info("[MAIN] Update recieved: " + nowDate.getTime());
        nowDate.set(Calendar.HOUR_OF_DAY, 0);
        nowDate.set(Calendar.MINUTE, 0);
        nowDate.set(Calendar.SECOND, 0);
        nowDate.set(Calendar.MILLISECOND, 0);
        log.info("[MAIN] Update recieved: " + nowDate.getTime());


        if (update.hasMessage()) {

            userActions.registerUser(update.getMessage());

            Message msg = update.getMessage();

            userId = msg.getFrom().getId();
            userName = msg.getFrom().getUserName();
            userFirstName = msg.getFrom().getFirstName();
            messageText = msg.getText();
            chatId = msg.getChatId();


            log.info("[MAIN] It is message from: " + userFirstName);

            if (msg.hasText()) {
                log.info("[MAIN] Message has text");
                switch (messageText) {
                    //case "/start":
                    //case "/start@pidor_scanner_bot":
                    //    startCommandRecieved();
                    //    break;
                    case "/mydata":
                    case "/mydata@pidor_scanner_bot":
                        userActions.getMyData(chatId, userId, nowDate);
                        break;
                    default:
                        sendMessage(update.getMessage().getChatId(), userName + ", нарываешься! Только кружки ;)", userName);
                }

            } else if (msg.hasVideoNote()) {

                log.info("[MAIN] Message has videonote");

                userActions.addUserMessageCount(userId, nowDate, msg);
            }
        }
    }



    //отключено чтобы не заебывать пацанов при перезагрузке
    private void startCommandRecieved(){
        String answer;
        if (!checkPidor.isCheckPidorStatus()) {
            checkPidor.startCheckPidor();
            answer = "Здарова, бедолаги :) \n\n"
                    + "Теперь я буду считать ваши кружки, сообщать кто объебался, и какой это по счету раз. Погнали)";

        } else {
            answer = "Функция ПидорСканнер уже запущена.";
        }

        sendMessage(chatId, answer, "");
    }

    public void sendMessage(long chatId, String textToSend, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

//        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
            log.info("[MAIN] ANSWER: User " + userName + ", text: " + textToSend);
        }
        catch (TelegramApiException e) {
            log.error("send error:" + e);
        }
    }


}
