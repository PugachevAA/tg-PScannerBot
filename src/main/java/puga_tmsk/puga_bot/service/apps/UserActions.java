package puga_tmsk.puga_bot.service.apps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.model.UserData;
import puga_tmsk.puga_bot.model.UserRepository;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@Getter
public class UserActions {

    private TelegramBot telegramBot;

    public UserActions(TelegramBot tgb) {
        this.telegramBot = tgb;
    }

    UserRepository ur;
    public void registerUser(Message message) {
        if(telegramBot.getUserRepository().findById(message.getFrom().getId()).isEmpty()) {

            User user = new User();

            user.setUserId(message.getFrom().getId());
            user.setFirstName(message.getFrom().getFirstName());
            user.setLastName(message.getFrom().getLastName());
            user.setUserName(message.getFrom().getUserName());
            user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            user.setPidorCount(0);
            user.setPidorNow(false);


            telegramBot.getUserRepository().save(user);

            log.info("User saved: " + user);
        }
    }

    public void getMyData(long chatId, long userId, Calendar nowDate) {
        log.info("[MAIN] check /mydata");
        User user = telegramBot.getUserRepository().findById(userId).get();
        List<UserData> ud = new ArrayList<>();
        for (UserData usd : telegramBot.getUserDataRepository().findAll()) {
            if (userId == usd.getUserId()) {
                ud.add(usd);
            }
        }
        long msgCountAll = 0;
        for (UserData usd : ud) {
            msgCountAll += usd.getMessageCount();
        }

        long msgCountToday = 0;
        for (UserData usd : ud) {
            if (usd.getDate().getTime() == nowDate.getTimeInMillis())
                msgCountToday = usd.getMessageCount();
        }
        String isPidorStr = "";
        if (user.isPidorNow()) {
            isPidorStr = "пидор";
        } else {

            isPidorStr = "не пидор";
        }
        String answer = user.getUserName() + "\n" +
                        "Имя:" + user.getFirstName() + "\n" +
                        "Фамилия:" + user.getLastName() + "\n" +
                        "Дата регистрации " + user.getRegisterTime().toString() + "\n" +
                        "Был пидором " + user.getPidorCount() + " раз" + "\n" +
                        "Сообщений с даты регистрации: " + msgCountAll + "\n" +
                        "Сообщений сегодня: " + msgCountToday+ "\n" +
                        "На данный момент в статусе: " + isPidorStr + "\n";
        telegramBot.sendMessage(chatId, answer,"");
    }

    public void addUserMessageCount(long userId, Calendar nowDate, Message msg) {
        nowDate.setTimeZone(TimeZone.getTimeZone(telegramBot.getConfig().getTimeZone()));
        log.info("[MAIN] Adding message count");
        UserData ud = new UserData();
        for (UserData udAll : telegramBot.getUserDataRepository().findAll()) {
            log.info(udAll.getDate().getTime() + "   " + nowDate.getTimeInMillis());
            Calendar cld = Calendar.getInstance();
            cld.setTimeZone(TimeZone.getTimeZone(telegramBot.getConfig().getTimeZone()));
            cld.setTime(udAll.getDate());
            if (udAll.getUserId() == userId && udAll.getDate().getTime() == nowDate.getTimeInMillis()) {
                ud = udAll;
                log.info("[MAIN/addUserMessageCount] user finded");
            }
        }
        if (ud.getId() == 0) {
            ud.setId(telegramBot.getUserDataRepository().count() + 1);
            ud.setUserId(userId);
            ud.setDate(new Timestamp(nowDate.getTimeInMillis()));
            ud.setMessageCount(1);
            ud.setPidor(false);
        } else {
            ud.setMessageCount(ud.getMessageCount() + 1);
        }
        telegramBot.getUserDataRepository().save(ud);
    }
}
