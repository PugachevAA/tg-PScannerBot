package puga_tmsk.puga_bot.service.keyboards;

import lombok.Data;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
public class ReplyKeyboards {

    public ReplyKeyboardMarkup getMainReplyKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        button1.setText("11");
        row1.add(button1);

        KeyboardRow row2 = new KeyboardRow();
        KeyboardButton button2 = new KeyboardButton();
        button2.setText("22");
        row1.add(button2);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(row1);
        keyboardRows.add(row2);

        final ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup();
        mainKeyboard.setKeyboard(keyboardRows);
        mainKeyboard.setOneTimeKeyboard(true);
        mainKeyboard.setSelective(true);
        mainKeyboard.setResizeKeyboard(true);

        return mainKeyboard;
    }

    public ReplyKeyboardMarkup getShoppingListKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        KeyboardButton button1 = new KeyboardButton();
        button1.setText("Добавить");
        row1.add(button1);

        KeyboardRow row2 = new KeyboardRow();
        KeyboardButton button2 = new KeyboardButton();
        button2.setText("Очистить");
        row1.add(button2);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(row1);
        keyboardRows.add(row2);

        final ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup();
        mainKeyboard.setKeyboard(keyboardRows);
        mainKeyboard.setOneTimeKeyboard(true);
        mainKeyboard.setSelective(true);
        mainKeyboard.setResizeKeyboard(true);

        return mainKeyboard;
    }

}
