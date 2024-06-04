package puga_tmsk.puga_bot.service.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.util.ArrayList;
import java.util.List;

public class InLineKeyboards {

    TelegramBot telegramBot;
    public InLineKeyboards(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public InlineKeyboardMarkup rouletteShot(long userId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();

        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("Выстрелить");
        btn1.setCallbackData("/pidroulette_shot_" + userId);
        keyboardRow1.add(btn1);



        keyboardRows.add(keyboardRow1);

        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
        mainInLineKeyboard.setKeyboard(keyboardRows);

        return mainInLineKeyboard;
    }

//    public InlineKeyboardMarkup getShoppingListKeyboard(Long chatId, String message) {

//        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
//
//        for (List<InlineKeyboardButton> row : generateShoppingListButtons(chatId, message, shoppingListRepository)) {
//            keyboardRows.add(row);
//        }
//
//        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();
//
//        InlineKeyboardButton additems = new InlineKeyboardButton();
//        additems.setText("Заполнить");
//        additems.setCallbackData("/shoplistadditems");
//        keyboardRow1.add(additems);
//
//        InlineKeyboardButton clearall = new InlineKeyboardButton();
//        clearall.setText("Очистить все");
//        clearall.setCallbackData("/shoplistclear");
//        keyboardRow1.add(clearall);
//
//
//        keyboardRows.add(keyboardRow1);
//
//        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
//        mainInLineKeyboard.setKeyboard(keyboardRows);
//
//        return mainInLineKeyboard;
//    }

//    public InlineKeyboardMarkup getShoppingListAddKeyboard(Long chatId, String message, ShoppingListRepository shoppingListRepository) {
//
//        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
//
//        for (List<InlineKeyboardButton> row : generateShoppingListButtons(chatId, message, shoppingListRepository)) {
//            keyboardRows.add(row);
//        }
//
//        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();
//
//        InlineKeyboardButton additems = new InlineKeyboardButton();
//        additems.setText("Закончить");
//        additems.setCallbackData("/shoplistendadd");
//        keyboardRow1.add(additems);
//
//        keyboardRows.add(keyboardRow1);
//
//        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
//        mainInLineKeyboard.setKeyboard(keyboardRows);
//
//        return mainInLineKeyboard;
//    }
//    private List<List<InlineKeyboardButton>> generateShoppingListButtons(Long chatId, String message, ShoppingListRepository shoppingListRepository) {
//        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
//
//        if (shoppingListRepository.count() > 0) {
//            for (ShoppingList shoppingList1 : shoppingListRepository.findAll()) {
//                if (shoppingList1.getChatId().equals(chatId)) {
//
//                    List<InlineKeyboardButton> keyboardRowTemp = new ArrayList<>();
//
//                    InlineKeyboardButton itemButton = new InlineKeyboardButton();
//                    itemButton.setText(shoppingList1.getProduct());
//                    itemButton.setCallbackData("/shoppinglist_" + shoppingList1.getId());
//                    keyboardRowTemp.add(itemButton);
//                    keyboardRows.add(keyboardRowTemp);
//                }
//            }
//        }
//        return keyboardRows;
//    }


}
