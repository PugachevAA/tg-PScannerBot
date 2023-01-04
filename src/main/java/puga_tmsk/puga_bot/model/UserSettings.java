package puga_tmsk.puga_bot.model;

import lombok.Data;
import puga_tmsk.puga_bot.config.BotStatus;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "userSettings")
public class UserSettings {

    @Id
    private Long chatId;
    private int botStatus;

}

