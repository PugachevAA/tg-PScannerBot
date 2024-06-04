package puga_tmsk.puga_bot.service.apps;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.FirebirdDialect;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.service.TelegramBot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class RusRoulette {
    TelegramBot telegramBot;
    Random random;
    boolean isRun;
    List<User> gamers;
    int gamer;
    Integer pollId;
    Integer messageId;

    public RusRoulette(TelegramBot tgb) {
        telegramBot = tgb;
        isRun = false;
        random = new Random();
            }

    public void startGame(Message msg) {

        if (!isRun) {
            isRun = true;
            log.info("Игра не запущена, запускаю");
            gamers = telegramBot.getUserRepository().findAll();
            log.info("Игроков: " + gamers.size());
            sendStartPoll();

        } else {
            log.info("Игра уже запущена");
        }
    }

    private void sendStartPoll() {
        List<String> options = new ArrayList<>();

        String option1 = "Да";
        String option2 = "Нет";
        options.add(option1);
        options.add(option2);

        telegramBot.sendPoll("Ну че, ссыкуны, готовы сыграть в пидрулетку?))",options);
    }

    public void checkPoll(Poll poll) {
        if (isRun) {
            //messageId = message.getMessageId();
            if (poll.getQuestion().equals("Ну че, ссыкуны, готовы сыграть в пидрулетку?))")) {
                log.info("Есть голос");
                if (poll.getTotalVoterCount() == gamers.size() && poll.getOptions().get(0).getVoterCount() == gamers.size()) {
                    //telegramBot.deleteMessage(messageId);
                    firstShot();
                } else {
                    if (poll.getOptions().get(1).getVoterCount() > 0) {
                        telegramBot.sendMessage("Ну и пошли вы в пизду. Кто-то из вас зассал)", null, null);
                        stopGame();
                    }
                }
            }
        }
    }

    private void firstShot() {

        gamer = random.nextInt(gamers.size());
        log.info("Сейчас стреляет: " + gamers.get(gamer).getUserName());

        String text = "Красатоны! Погнали епта. Первым стреляет @" + gamers.get(gamer).getUserName();

        telegramBot.sendMessage( text, null, telegramBot.getInLineKeyboards().rouletteShot(gamers.get(gamer).getUserId()));

    }

    private void nextShot(String text) {

        telegramBot.sendMessage( text, null, telegramBot.getInLineKeyboards().rouletteShot(gamers.get(gamer).getUserId()));
    }

    private void stopGame() {
        isRun = false;
        gamers.clear();
    }

    public void callbackHandler(CallbackQuery callbackQuery) {
        messageId = callbackQuery.getMessage().getMessageId();
        String[] callback = callbackQuery.getData().split("_");
        if (callback.length > 1) {
            if (callback[1].equals("shot")) {
                if (callback[2].equals(String.valueOf(callbackQuery.getFrom().getId()))) {
                    shot();
                } else {
                    telegramBot.sendMessage("Кто, блять, читать не умеет? Стреляет @" + gamers.get(gamer).getUserName(), null, null);
                }
            }
        }
    }

    private void shot() {
        int kill = random.nextInt(2);
        if (kill == 1) {
            telegramBot.editMessage(messageId, "Патрон был боевой. @" + gamers.get(gamer).getUserName() + ", Чувак, ты - пидор)) ", null);
            stopGame();
        } else {
            String thisGamer = gamers.get(gamer).getUserName();
            gamers.remove(gamer);
            if (gamers.size()>0) {
                gamer = random.nextInt(gamers.size());
                nextShot("Фух, " + thisGamer + ", пронесло тебя. Следующим стреляет " + gamers.get(gamer).getUserName());
            } else {
                telegramBot.editMessage(messageId, "Холостой! Охуеть вы счастливчики, ничего не изменилось, получается)", null);
                stopGame();
            }
        }
    }
}
