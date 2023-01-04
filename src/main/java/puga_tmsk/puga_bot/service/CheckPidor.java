package puga_tmsk.puga_bot.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.model.UserData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Getter
public class CheckPidor {

    private Thread thread;
    private TelegramBot telegramBot;
    private boolean checkPidorStatus = false;

    public CheckPidor(TelegramBot tgb) {
        this.telegramBot = tgb;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar checkDate = Calendar.getInstance();
                checkDate.set(Calendar.HOUR_OF_DAY, 0);
                checkDate.set(Calendar.MINUTE, 0);
                checkDate.set(Calendar.SECOND, 0);
                checkDate.set(Calendar.MILLISECOND, 0);
                checkDate.add(Calendar.DATE, - 1);

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Запускается блок проверки пидора, дата запуска ");

                try {
                    while (true) {
                        log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Цикл проверки запущен");

                        List<UserData> pidorsData = new ArrayList<>();
                        for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                            if (ud.getDate().getTime().equals(checkDate.getTime())) {
                                log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Нашли запись за вчера");
                                //if (ud.getMessageCount() < 3 && !ud.isPidor()) {
                                if (ud.getMessageCount() < 3) {
                                    log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Меньше 3 сообщений и без пометки - пидор найден");

                                    pidorsData.add(ud);
                                }
                            }
                        }

                        setNewPidors(pidorsData, checkDate, today);

                        checkDate.add(Calendar.DATE,1);
                        today.add(Calendar.DATE,1);
                        log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Время checkDate установлено на " + checkDate.getTime());
                        log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Время today установлено на " + today.getTime());

                        long timeout = today.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                        log.info(Calendar.getInstance().getTime().toString() + " [PIDOR SCANNER] Усыпляем блок проверки на пидора на " + timeout + "ms");
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

    private void setNewPidors(List<UserData> pidorsData, Calendar checkDate, Calendar today) {

        User user;

        if (pidorsData.size() > 0) {
            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                if (ud.getDate().getTime().equals(today.getTime())) {
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
                telegramBot.sendMessage(telegramBot.config.getChatId(), "А вот и новый пидарок нарисовался! Встречайте, @" +
                        telegramBot.getUserRepository().findById(ud.getUserId()).get().getUserName() +
                        " перехватывает знамя. Это, кстати, уже его " + user.getPidorCount() + " раз.", "");
            }
        } else {
            List<String> lastPidors = new ArrayList<>();
            for (UserData ud : telegramBot.getUserDataRepository().findAll()) {
                if (ud.getDate().getTime().equals(checkDate.getTime())) {
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
