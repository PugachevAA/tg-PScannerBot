package puga_tmsk.puga_bot.service;

import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.ManagedEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import puga_tmsk.puga_bot.config.BotConfig;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.*;
import puga_tmsk.puga_bot.service.apps.AdminActions;
import puga_tmsk.puga_bot.service.apps.CheckPidor;
import puga_tmsk.puga_bot.service.apps.RusRoulette;
import puga_tmsk.puga_bot.service.apps.UserActions;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;
import puga_tmsk.puga_bot.service.keyboards.ReplyKeyboards;
import java.sql.Timestamp;
import java.time.*;
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
    private AdminActions adminActions = new AdminActions(this);
    private RusRoulette rusRoulette = new RusRoulette(this);
    private final ReplyKeyboards replyKeyboards = new ReplyKeyboards();;
    private final InLineKeyboards inLineKeyboards = new InLineKeyboards(this);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDataRepository userDataRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;


    private static final String HELP_TEXT = "Pidor scanner v1.0";

    public TelegramBot(BotConfig config) {

        this.config = config;
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/mydata", "Данные обо мне"));
        menu.add(new BotCommand("/pidroulette", "Рулетка для сильных духом))"));

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
        log.info("[MAIN] Update recieved: " + new Timestamp(System.currentTimeMillis()));

        long userId;
        String messageText;
        String userFirstName;
        String userName;
        LocalDate nowDate = LocalDate.ofInstant(Instant.now(), ZoneId.of(config.getTimeZone()));
        log.info("[MAIN] nowDate: " + nowDate.toString());
        if (update.hasMessage()) {

            Message msg = update.getMessage();

            userId = msg.getFrom().getId();
            userName = msg.getFrom().getUserName();
            userFirstName = msg.getFrom().getFirstName();
            messageText = msg.getText();
            chatId = config.getOurCaId();

            if (chatId == config.getOurCaId()) {

                log.info("[MAIN] It is message from: " + userFirstName + ", chatid: " + chatId);

                userActions.registerUser(update.getMessage());
                if (msg.hasText()) {
                    log.info("[MAIN] Message has text");
                    log.info(userId + " = " + config.getAdminId());
                    switch (messageText) {
                        case "/mydata":
                        case "/mydata@pidor_scanner_bot":
                            log.info("/mydata " + userName);
                            userActions.getMyData(chatId, userId, nowDate);
                            break;

                        case "/stata":
                            userActions.getAllStata(chatId);
                            break;

                        case "/chatid":
                            log.info("/chatid" + userName);
                            adminActions.getChatId();
                            break;
                        case "/pidroulette":
                            log.info("/pidroulette" + userName);
                            rusRoulette.startGame(msg);
                            break;

                        default:
                            String defMessage = userName + ", нарываешься! Только кружки ;)";
                            if (userId == config.getAdminId()) {
                                defMessage = "Как скажешь, господин";
                            }
                            sendMessage( defMessage, userName, null);
                    }

                } else if (msg.hasVideoNote()) {

                    log.info("[MAIN] Message has videonote");

                    userActions.addUserMessageCount(userId, nowDate, msg);
                }
            }
        }else if (update.hasPoll()) {
            rusRoulette.checkPoll(update.getPoll());
        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().contains("/pidroulette")) {
                rusRoulette.callbackHandler(update.getCallbackQuery());
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

        sendMessage(answer, "", null);
    }

    public void sendMessage(String textToSend, String userName, InlineKeyboardMarkup inlineKeyboardMarkup ) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        if(inlineKeyboardMarkup != null) {
            message.setReplyMarkup(inlineKeyboardMarkup);
        }

//        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
            log.info("[MAIN] ANSWER: User " + userName + ", text: " + textToSend);
        }
        catch (TelegramApiException e) {
            log.error("send error:" + e);
        }
    }


    public void editMessage(Integer messageId, String newText, InlineKeyboardMarkup mainInLineKeyboard) {

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setReplyMarkup(mainInLineKeyboard);
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);

        try {
            if (!newText.equals("")) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId(messageId);
                editMessageText.setText(newText);
                execute(editMessageText);
                log.info("Edit message");

            }
            execute(editMessageReplyMarkup);
            log.info("Edit message");
        }
        catch (TelegramApiException e) {
            log.error("edit error:" + e);
        }
    }

    public void sendPoll(String question, List< String > options) {
        SendPoll sendPoll = new SendPoll();
        sendPoll.setChatId(String.valueOf(chatId));
        sendPoll.setQuestion(question);
        sendPoll.setOptions(options);

        try {
            execute(sendPoll);
            log.info("[MAIN] send poll");
        }
        catch (TelegramApiException e) {
            log.error("send poll error:" + e);
        }
    }

    public void deleteMessage(Integer msgId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(msgId);
        deleteMessage.setChatId(String.valueOf(chatId));

        try {
            execute(deleteMessage);
            log.info("[MAIN] delete message");
        }
        catch (TelegramApiException e) {
            log.error("delete message error:" + e);
        }
    }


}
