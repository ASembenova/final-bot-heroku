package kz.asembina.pvl_vuzy_bot.service.menu;

import kz.asembina.pvl_vuzy_bot.egovapi.DataObjectService;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.Arrays;
import java.util.List;

@Service
public class AboutService {

    private final MessageSender messageSender;
    private final DataObjectService dataObjectService;
    private final LocaleService localeService;

    public AboutService(MessageSender messageSender, DataObjectService dataObjectService, LocaleService localeService) {
        this.messageSender = messageSender;
        this.dataObjectService = dataObjectService;
        this.localeService = localeService;
    }

    public BotApiMethod<?> getHelpMessage(long chatId, String lang) {
        return messageSender.createMessageWithKeyboardByTags(chatId, "help.msg",
                messageSender.getMenuKeyboard(getKeyboardList(lang)), lang);
    }

    public List<String> getKeyboardList(String lang) {
        return messageSender.getButtonList(Arrays.asList("help.dataset_info", "help.opendata", "help.dataegov","help.write", "back"), lang);
    }

    private ReplyKeyboardMarkup getKeyboard(String lang) {
        return messageSender.getMenuKeyboard(messageSender.getButtonList(Arrays.asList("help.dataset_info", "help.opendata", "help.dataegov","help.write", "back"), lang));
    }

    public BotApiMethod<?> getHelpDatasetPassportMessage(long chatId, String lang){
        String[][] array = dataObjectService.getDatasetPassport();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String[] row = array[i];
            if(!(row[1]==null) && !(row[1].equals(""))){
                stringBuilder.append("<b>").append(row[0]).append(": </b>").append(" ").append(row[1]).append("\n");
            }
        }
        return messageSender.createMessageWithKeyboard(chatId, stringBuilder.toString(), getKeyboard(lang));
    }

    public BotApiMethod<?> getHelpOpenDataMessage(long chatId, String lang){
        return messageSender.createMessageWithKeyboard(chatId, localeService.getMessage("help.opendata.msg", lang), getKeyboard(lang));
    }

    public BotApiMethod<?> getHelpDataEgovMessage(long chatId, String lang){
        return messageSender.createMessageWithKeyboard(chatId, localeService.getMessage("help.dataegov.msg", lang), getKeyboard(lang));
    }

    public BotApiMethod<?> getHelpWriteMessage(long chatId, String lang) {
        return messageSender.createMessageWithKeyboard(chatId, localeService.getMessage("help.write.msg", lang), getKeyboard(lang));
    }
}
