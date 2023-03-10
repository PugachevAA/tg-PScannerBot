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
            if (usd.getDate().equals(nowDate))
                msgCountToday = usd.getMessageCount();
        }
        String isPidorStr = "";
        if (user.isPidorNow()) {
            isPidorStr = "??????????";
        } else {

            isPidorStr = "???? ??????????";
        }
        String answer = user.getUserName() + "\n" +
                        "??????:" + user.getFirstName() + "\n" +
                        "??????????????:" + user.getLastName() + "\n" +
                        "???????? ?????????????????????? " + user.getRegisterTime().toString() + "\n" +
                        "?????? ?????????????? " + user.getPidorCount() + " ??????" + "\n" +
                        "?????????????????? ?? ???????? ??????????????????????: " + msgCountAll + "\n" +
                        "?????????????????? ??????????????: " + msgCountToday+ "\n" +
                        "???? ???????????? ???????????? ?? ??????????????: " + isPidorStr + "\n";
        telegramBot.sendMessage(chatId, answer,"");
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
}
