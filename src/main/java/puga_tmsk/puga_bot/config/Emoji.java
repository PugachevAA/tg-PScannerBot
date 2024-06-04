package puga_tmsk.puga_bot.config;

import com.vdurmont.emoji.EmojiParser;
import lombok.Data;

@Data
public class Emoji {
    private final String GUN = EmojiParser.parseToUnicode(":gun:");
    private final String FIRE = EmojiParser.parseToUnicode(":fire:");
    private final String DASH = EmojiParser.parseToUnicode(":dash:");
}
