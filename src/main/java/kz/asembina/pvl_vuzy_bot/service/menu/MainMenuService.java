package kz.asembina.pvl_vuzy_bot.service.menu;

import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.Arrays;

@Service
public class MainMenuService {

    private final MessageSender messageSender;

    public MainMenuService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public BotApiMethod<?> getWelcomeMsg(long chatId, String lang) {
        return messageSender.createMessageWithKeyboardByTags(chatId, "welcome.msg",
                Arrays.asList("lang.kz", "lang.ru"), lang);
    }

    public BotApiMethod<?> getMainMenuMsg(long chatId, String lang) {
        return messageSender.createMessageWithKeyboardByTags(chatId, "main_menu.msg", Arrays.asList("select_all", "select_one", "change_lang", "help"), lang);
    }

    public BotApiMethod<?> getSelectLangMsg(long chatId, String lang) {
        return messageSender.createMessageWithKeyboardByTags(chatId, "change_lang.message", Arrays.asList("lang.kz", "lang.ru"), lang);
    }
}
