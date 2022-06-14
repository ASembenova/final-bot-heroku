package kz.asembina.pvl_vuzy_bot.service.memory;


import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class LocaleService {
    private Map<Long, Locale> localeMap;
    private Locale defaultLocale;
    private MessageSource messageSource;

    public LocaleService(@Value("${localeTag}") String localeTag, MessageSource messageSource) {
        this.messageSource = messageSource;
        this.defaultLocale = Locale.forLanguageTag(localeTag);
        localeMap = new HashMap<>();
    }

    public void changeLang(String localeTag, long chatId){
        Locale locale = Locale.forLanguageTag(localeTag);
        localeMap.put(chatId, locale);
    }


    public String getMessage(String tag, String lang) {
        return EmojiParser.parseToUnicode(messageSource.getMessage(tag, null, Locale.forLanguageTag(lang)));
    }


    public String getLocaleTag(long chatId){
        return getLocale(chatId).getLanguage();
    }

    private Locale getLocale(long chatId){
        if(isEmpty() || !containsUser(chatId)){
            localeMap.put(chatId, defaultLocale);
        }
        return localeMap.get(chatId);
    }
    public void addNewUser(long chatId){
        localeMap.put(chatId, defaultLocale);
    }

    public boolean isEmpty(){
        return localeMap.isEmpty();
    }

    public boolean containsUser(long chatId){
        return localeMap.containsKey(chatId);
    }
}
