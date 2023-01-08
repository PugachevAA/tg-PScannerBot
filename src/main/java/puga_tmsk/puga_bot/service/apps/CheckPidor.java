package puga_tmsk.puga_bot.service.apps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.model.UserData;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@Getter
public class CheckPidor {

    private Thread thread;
    private TelegramBot telegramBot;
    private boolean checkPidorStatus = false;
    private boolean isFirstStart = true;

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




                log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Запускается блок проверки пидора ");

                try {
                    while (true) {

                        log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Цикл проверки запущен");

                        if (isFirstStart) {
                            isFirstStart = false;

                            log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Первый запуск, пропускаю проверку");
                        } else {
                            log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Не первый запуск, работаем");

                            List<UserData> pidorsData = new ArrayList<>();
                            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                                if (ud.getDate().equals(checkDate)) {
                                    log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Нашли запись за вчера");
                                    //if (ud.getMessageCount() < 3 && !ud.isPidor()) {
                                    if (ud.getMessageCount() < 3) {
                                        log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Меньше 3 сообщений и без пометки - пидор найден");

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
                            log.info("[PIDOR SCANNER] timeout func: " + today.atStartOfDay().atZone(ZoneId.of(tgb.getConfig().getTimeZone())) + " - " + LocalDateTime.now().atZone(ZoneId.of(tgb.getConfig().getTimeZone())));
                        long timeout = todayMillis - nowMillis;
                            log.info("[PIDOR SCANNER] Усыпляем блок проверки на пидора на " + new Time(timeout).toString());
                            log.info("[PIDOR SCANNER] Усыпляем блок проверки на пидора на " + timeout);
                        Thread.sleep(timeout);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    public void startCheckPidor() {
        checkPidorStatus = true;
        thread.start();
    }

    private void setNewPidors(List<UserData> pidorsData, LocalDate checkDate, LocalDate today) {

        User user;

        if (pidorsData.size() > 0) {
            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                if (ud.getDate().equals(today)) {
                    ud.setPidor(false);
                    telegramBot.getUserDataRepository().save(ud);
                }

            }
            for (UserData ud : pidorsData) {
                ud.setPidor(true);
                UserData newUd = new UserData();
                newUd.setId(telegramBot.getUserDataRepository().count() + 1);
                newUd.setUserId(ud.getUserId());
                newUd.setDate(today);
                newUd.setMessageCount(0);
                newUd.setPidor(true);
                user = telegramBot.getUserRepository().findById(ud.getUserId()).get();
                user.setPidorCount(user.getPidorCount()+1);
                user.setPidorNow(true);
                telegramBot.getUserRepository().save(user);
                telegramBot.getUserDataRepository().save(newUd);
                telegramBot.sendMessage(telegramBot.getChatId(), "А вот и новый пидарок нарисовался! Встречайте, @" + ", " + user.getFirstName() +
                        telegramBot.getUserRepository().findById(ud.getUserId()).get().getUserName() +
                        " перехватывает знамя. Это, кстати, уже его " + user.getPidorCount() + " раз.", "");
            }
        } else {
            List<String> lastPidors = new ArrayList<>();
            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                if (ud.getDate().equals(checkDate) && ud.isPidor()) {
                    lastPidors.add("@" + telegramBot.getUserRepository().findById(ud.getUserId()).get().getUserName());
                }
            }
            if (lastPidors.size() > 0) {
                telegramBot.sendMessage(telegramBot.getChatId(), "Сегодня новых пидоров не обнаружено, знамя по прежнему в руках: "
                        + lastPidors.toString(), "");
            } else {
                telegramBot.sendMessage(telegramBot.getChatId(), "Сегодня новых пидоров не обнаружено", "");
            }
        }

    }

}
