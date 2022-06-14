package kz.asembina.pvl_vuzy_bot.service.menu;

import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import kz.asembina.pvl_vuzy_bot.service.menu.speciality.SpecialityService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.Arrays;

@Service
public class SearchService {

    private final MessageSender messageSender;
    private final SpecialityService specialityService;
    private final LocaleService localeService;

    public SearchService(MessageSender messageSender, SpecialityService specialityService, LocaleService localeService) {
        this.messageSender = messageSender;
        this.specialityService = specialityService;
        this.localeService = localeService;
    }

    public BotApiMethod<?> getSearchMsg(long chatId, String lang) {
        return messageSender.createMessageWithBackButton(chatId, localeService.getMessage("search.instr", lang), lang);
    }

    public BotApiMethod<?> getSearchWarningMsg(long chatId, String lang) {
        return messageSender.createMessageWithBackButton(chatId, localeService.getMessage("search.warning", lang), lang);
    }

    public BotApiMethod<?> getSearchResults(long chatId, String messageText, String lang) {
        String result = specialityService.searchByKeyword(messageText, chatId, lang);
        if(result.length()>=4096){
            return getSearchWarningMsg(chatId, lang);
        }
        return messageSender.createMessageWithBackButton(chatId, result, lang);
    }
}
