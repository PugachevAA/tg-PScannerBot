package puga_tmsk.puga_bot.service.apps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.model.UserData;
import puga_tmsk.puga_bot.model.UserRepository;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public void getMyData(long chatId, long userId, LocalDate nowDate) {
        String answer = "";
        String preAnswer = "";
        String prePreAnswer = "";
        log.info("[MAIN] check /mydata");

        long msgCountToday = 0;
        long getDataCountToday = 0;
        long msgCountAll = 0;
        long getDataCountAll = 0;

        User user = telegramBot.getUserRepository().findById(userId).get();
        UserData udToday = telegramBot.getUserDataRepository().findByUserIdAndDate(userId, nowDate);
        if (udToday == null) {
            udToday = createAndGetEmptyTodayNote(userId, nowDate);
        }

        List<UserData> udAll = new ArrayList<>();

        for (UserData usd : telegramBot.getUserDataRepository().findAll()) {
            if (userId == usd.getUserId()) {
                udAll.add(usd);
            }
        }

        if (udToday.getGetDataCount() == 1) {
            preAnswer = " Опять чтоли?)";
        } else if (udToday.getGetDataCount() == 2) {
            preAnswer = ", ты издеваешься?";
            prePreAnswer = "Да бляяяять...\n";
        }

        getDataCountToday = udToday.getGetDataCount();
        msgCountToday = udToday.getMessageCount();

        for (UserData usd : udAll) {
            msgCountAll += usd.getMessageCount();
            getDataCountAll += usd.getGetDataCount();
        }

        String isPidorStr = "";
        if (user.isPidorNow()) {
            isPidorStr = "Пидор";
        } else {

            isPidorStr = "Не пидор";
        }


        answer = prePreAnswer +
                        "@" + user.getUserName() + preAnswer + "\n\n" +
                        "Сообщений с даты регистрации: " + msgCountAll + "\n" +
                        "Сообщений сегодня: " + msgCountToday + "\n" +
//                        "Запросил свою стату сегодня: " + getDataCountToday + " раз" + "\n" +
//                        "Запросил свою стату всего: " + msgCountToday  + " раз" + "\n" +
                        "Был пидором " + user.getPidorCount() + " раз" + "\n" +
                        "Был пидором " + telegramBot.getUserDataRepository().countAllByUserIdAndIsPidor(userId, true) + " дней" + "\n" +
                        "На данный момент в статусе: " + isPidorStr + "\n";

        if (udToday.getGetDataCount() >2) {
            answer = "@" + user.getUserName() + ", да пошел ты, заманал уже сегодня)) Завтра пробуй)";
        }
        telegramBot.sendMessage(chatId, answer,"");

        udToday.setGetDataCount(udToday.getGetDataCount() + 1);
        telegramBot.getUserDataRepository().save(udToday);
    }

    public void addUserMessageCount(long userId, LocalDate nowDate, Message msg) {
        log.info("[MAIN] Adding message count");
        UserData ud = new UserData();
        for (UserData udAll : telegramBot.getUserDataRepository().findAll()) {
            log.info(udAll.getDate() + "   " + nowDate);
            if (udAll.getUserId() == userId && udAll.getDate().equals(nowDate)) {
                ud = udAll;
                log.info("[MAIN/addUserMessageCount] user finded");
            }
        }
        if (ud.getId() == 0) {
            boolean isPidor = false;
            LocalDate tomorrow = nowDate.minusDays(1);
            if (telegramBot.getUserDataRepository().findFirstByUserIdOrderByDateDesc(userId).isPidor()) {
                isPidor = true;
            }
            ud.setId(telegramBot.getUserDataRepository().count() + 1);
            ud.setUserId(userId);
            ud.setDate(nowDate);
            ud.setMessageCount(1);
            ud.setPidor(isPidor);
        } else {
            ud.setMessageCount(ud.getMessageCount() + 1);
        }
        telegramBot.getUserDataRepository().save(ud);
    }

    public void getAllStata(long chatId) {
        log.info("[UserActions] call stata method");

    }

    public UserData createAndGetEmptyTodayNote(long userId, LocalDate today) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        userData.setDate(today);
        userData.setGetDataCount(0);
        userData.setMessageCount(0);
        userData.setId(telegramBot.getUserDataRepository().findFirstByOrderByIdDesc().getId() + 1);
        userData.setPidor(telegramBot.getUserRepository().findById(userId).get().isPidorNow());
        telegramBot.getUserDataRepository().save(userData);
        return userData;
    }


}
