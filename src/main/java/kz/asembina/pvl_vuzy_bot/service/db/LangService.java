package kz.asembina.pvl_vuzy_bot.service.db;

import kz.asembina.pvl_vuzy_bot.db.domain.Lang;
import kz.asembina.pvl_vuzy_bot.db.repo.LangRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class LangService {

    private final LangRepo langRepo;

    public LangService(LangRepo langRepo) {
        this.langRepo = langRepo;
    }

    public void saveNewUser(long chatId, String firstname, String lastname){
        Lang lang = new Lang();
        lang.setChatId(chatId);
        lang.setFirstname(firstname);
        lang.setLastname(lastname);
        langRepo.save(lang);
    }

    public List<Lang> findAll(){
        return langRepo.findAll();
    }

    public void changeLang(long chatId, String langTag){
        Lang lang = langRepo.findByChatId(chatId);
        lang.setLocale(Locale.forLanguageTag(langTag));
        langRepo.save(lang);
    }

}
