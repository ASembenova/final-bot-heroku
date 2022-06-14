package kz.asembina.pvl_vuzy_bot.service.menu;

import kz.asembina.pvl_vuzy_bot.egovapi.CombinationService;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Arrays;

@Service
public class SelectAllService {

    private final MessageSender messageSender;
    private final CombinationService combinationService;

    public SelectAllService(MessageSender messageSender, CombinationService combinationService) {
        this.messageSender = messageSender;
        this.combinationService = combinationService;
    }

    public SendMessage getSelectAllMsg(long chatId, String lang) {
        return messageSender.createMessageWithKeyboardByTags(chatId, "select_all.msg", Arrays.asList("all_params", "select_params",
                "compare_spec", "search", "back"), lang);
    }

}
