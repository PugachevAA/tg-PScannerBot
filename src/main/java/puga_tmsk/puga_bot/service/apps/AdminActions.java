package puga_tmsk.puga_bot.service.apps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import puga_tmsk.puga_bot.service.TelegramBot;

@Slf4j
@Getter
public class AdminActions {
    private TelegramBot telegramBot;

    public AdminActions(TelegramBot tgb) {
        this.telegramBot = tgb;
    }

    public void helloAdmin() {
        telegramBot.sendMessage( "Hello, Admin ", null, null);
    }

    public void getChatId() {
        telegramBot.sendMessage( telegramBot.getChatId() + " ", null, null);
    }
}
