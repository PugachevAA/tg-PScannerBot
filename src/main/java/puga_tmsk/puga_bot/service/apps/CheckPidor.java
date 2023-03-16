package puga_tmsk.puga_bot.service.apps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.model.UserData;
import puga_tmsk.puga_bot.service.TelegramBot;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Getter
public class CheckPidor {

    private Thread thread;
    private TelegramBot telegramBot;
    private boolean checkPidorStatus = false;
    private boolean isFirstStart = true;
//    private boolean isFirstStart = false; //для тестов при запуске

    public CheckPidor(TelegramBot tgb) {
        this.telegramBot = tgb;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LocalDate today = LocalDate.ofInstant(Instant.now(), ZoneId.of(tgb.getConfig().getTimeZone()));
                LocalDate checkDate = today.minusDays(1);

                log.info("[PIDOR SCANNER] Запускается блок проверки пидора ");

                try {
                    while (true) {

                        log.info("[PIDOR SCANNER] Цикл проверки запущен");

                        checkForAllUsersInCheckDate(checkDate);

                        if (isFirstStart) {
                            log.info("[PIDOR SCANNER] Первый запуск, пропускаю проверку");

                            isFirstStart = false;
                        } else {
                            log.info("[PIDOR SCANNER] Не первый запуск, работаем");

                            List<UserData> pidorsData = new ArrayList<>();
                            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                                if (ud.getDate().equals(checkDate)) {
                                    log.info("[PIDOR SCANNER] Нашли запись за вчера");
                                    if (ud.getMessageCount() < 3) {
                                        log.info("[PIDOR SCANNER] Меньше 3 сообщений и без пометки - пидор найден");

                                        pidorsData.add(ud);
                                    }
                                }
                            }
                            setNewPidors(pidorsData, checkDate, today);
                        }

                        checkDate=checkDate.plusDays(1);
                        today=today.plusDays(1);
                            log.info("[PIDOR SCANNER] Время checkDate установлено на " + checkDate);
                            log.info("[PIDOR SCANNER] Время today установлено на " + today);
                        long todayMillis = today.atStartOfDay().atZone(ZoneId.of(tgb.getConfig().getTimeZone())).toInstant().toEpochMilli();
                        long nowMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            log.info("[PIDOR SCANNER] timeout func: " + today.atStartOfDay().atZone(ZoneId.of(tgb.getConfig().getTimeZone())) + " - " + LocalDateTime.now().atZone(ZoneId.systemDefault()));
                        long timeout = todayMillis - nowMillis;
                            log.info("[PIDOR SCANNER] Усыпляем блок проверки на пидора на " + timeout + "ms");
                        Thread.sleep(timeout);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkForAllUsersInCheckDate(LocalDate checkDate) {
        for (User u : telegramBot.getUserRepository().findAll()) {
            UserData ud = telegramBot.getUserDataRepository().findByUserIdAndDate(u.getUserId(), checkDate);
            if (ud == null) {
                Long count;
                count = telegramBot.getUserDataRepository().findFirstByDateOrderByIdDesc(checkDate).getId();
                if (count == null) {
                    count = telegramBot.getUserDataRepository().count();
                }
                ud = new UserData();
                ud.setId(count + 1);
                ud.setUserId(u.getUserId());
                ud.setMessageCount(0);
                ud.setDate(checkDate);
                ud.setPidor(false);
                telegramBot.getUserDataRepository().save(ud);
            }
        }
    }

    public void startCheckPidor() {
        checkPidorStatus = true;
        thread.start();
    }

    private void setNewPidors(List<UserData> pidorsData, LocalDate checkDate, LocalDate today) {

        //User user;
        List<User> users = new ArrayList<>(telegramBot.getUserRepository().findAll());

        if (pidorsData.size() > 0) {

            //Обнуляем пидорстатус всем за checkDate в табл UsersData
            for (UserData ud : telegramBot.getUserDataRepository().findAllByDate(checkDate)) {
                 ud.setPidor(false);
                 telegramBot.getUserDataRepository().save(ud);
            }

            //Обнуляем пидорстатус всем в табл Users
            for (User u : users) {
                u.setPidorNow(false);
            }

            for (UserData ud : pidorsData) {

                ud.setPidor(true);

                for (User u : users) {
                    if (u.getUserId() == ud.getUserId()) {
                        u.setPidorNow(true);
                        u.setPidorCount(u.getPidorCount() + 1);
                    }
                }

                telegramBot.getUserDataRepository().save(ud);

                String answer = "Встречайте нового пидорка! @" + telegramBot.getUserRepository().findById(ud.getUserId()).get().getFirstName() +
                        telegramBot.getUserRepository().findById(ud.getUserId()).get().getUserName() +
                        " перехватывает знамя. Это, кстати, уже его " + telegramBot.getUserRepository().findById(ud.getUserId()).get().getPidorCount() + " раз.";

                telegramBot.sendMessage(telegramBot.getChatId(), answer, "");
            }

            telegramBot.getUserRepository().saveAll(users);

        } else {
            List<String> lastPidors = new ArrayList<>();
            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                if (ud.getDate().equals(checkDate) && ud.isPidor()) {
                    lastPidors.add("@" + telegramBot.getUserRepository().findById(ud.getUserId()).get().getUserName());
                }
            }
            if (lastPidors.size() > 0) {
                telegramBot.sendMessage(telegramBot.getChatId(), "Сегодня новых пидоров не обнаружено, знамя по прежнему в цепких ягодицах: "
                        + lastPidors.toString(), "");
            } else {
                telegramBot.sendMessage(telegramBot.getChatId(), "Сегодня новых пидоров не обнаружено", "");
            }
        }

    }

}
