package kz.asembina.pvl_vuzy_bot.service.report;

import com.vdurmont.emoji.EmojiParser;
import kz.asembina.pvl_vuzy_bot.egovapi.CombinationService;
import kz.asembina.pvl_vuzy_bot.egovapi.DataObjectService;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.lang.reflect.Field;
import java.util.*;

@Service
public class CustomParamsService {

    private final LocaleService localeService;
    private final DataObjectService dataObjectService;
    private final CombinationService combinationService;
    private final MessageSender messageSender;
    private Map<Long, boolean[]> selectedParamsButtons;
    private final boolean[] emptyArray;

    public CustomParamsService(LocaleService localeService, DataObjectService dataObjectService, CombinationService combinationService, MessageSender messageSender) {
        this.localeService = localeService;
        this.dataObjectService = dataObjectService;
        this.combinationService = combinationService;
        this.messageSender = messageSender;
        selectedParamsButtons = new HashMap<>();
        emptyArray = new boolean[dataObjectService.getMetaRu().size()];
    }

    public BotApiMethod<?> getEditedCustomParametersMessage(long chatId, long messageId, int i) { //на callback: name11
        if(!selectedParamsButtons.containsKey(chatId)){
            selectedParamsButtons.put(chatId, new boolean[dataObjectService.getMetaRu().size()]);
        }
        boolean[] selected = selectedParamsButtons.get(chatId);
        if(!selected[i]) {
            selected[i] = true;
        } else {
            selected[i] = false;
        }
        selectedParamsButtons.put(chatId, selected);
        String lang = localeService.getLocaleTag(chatId);
        SendMessage sendMessage = getChooseParamsMsg(chatId, selected, lang);
        EditMessageText newMessage = new EditMessageText();
        newMessage.setMessageId((int) messageId);
        newMessage.setText("test");
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setReplyMarkup((InlineKeyboardMarkup) sendMessage.getReplyMarkup());
        newMessage.setParseMode("html");
        return newMessage;
    }

    public BotApiMethod<?> getChooseParamsMsg(long chatId, String lang) { //case PARAMS_CUSTOM
        boolean[] empty = new boolean[dataObjectService.getMetaRu().size()];
        selectedParamsButtons.put(chatId, empty);
        return getChooseParamsMsg(chatId, empty, lang);
    }


    private SendMessage getChooseParamsMsg(long chatId, boolean[] selected, String lang) { //inside Edited
        Map<Integer, String> map;
        if(lang.equals("kz")){
            map = dataObjectService.getMetaKz();
        }else {
            map = dataObjectService.getMetaRu();
        }
        List<String> list = new ArrayList<>();
        int i = 0;
        for (String value : map.values()) {
            StringBuilder stringBuilder = new StringBuilder()
                    .append(String.valueOf(value.charAt(0)).toUpperCase())
                    .append(value.substring(1));
            if(selected[i]){
                stringBuilder.append("\u2714");
            }
            list.add(stringBuilder.toString());
            i++;
        }
        List<String> fieldsList = new ArrayList<>();
        for (Field field:dataObjectService.getVuzFields()) {
            fieldsList.add(field.getName());
        }
        list.add(localeService.getMessage("btn.get_by_msg", lang));
        fieldsList.add("custom_params_msg");
        return createMessageWithInlineKeyboard(chatId, "Test", list, fieldsList);
    }

    public SendMessage createMessageWithInlineKeyboard(final long chatId, String textMessage, List<String> namesOfButtons, List<String> fieldsList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = null;
        if (namesOfButtons != null && fieldsList!=null) {
            inlineKeyboardMarkup = getInlineKeyboard(namesOfButtons, fieldsList);
        }
        return messageSender.createMessageWithInlineKeyboard(chatId, textMessage, inlineKeyboardMarkup);
    }


    public InlineKeyboardMarkup getInlineKeyboard(List<String> list, List<String> fieldsList){ //inside CreateMsg
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(EmojiParser.parseToUnicode(list.get(i)));
            if(i!=list.size()-1){
                btn.setCallbackData(fieldsList.get(i));
            } else {
                btn.setCallbackData(fieldsList.get(i+1));
            }
            row.add(btn);
            rowList.add(row);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public BotApiMethod<?> generateReportByParams(long chatId, String callbackId, String lang) {
        boolean[] selected = selectedParamsButtons.get(chatId);
        if(Arrays.equals(selected, emptyArray)){
            return messageSender.getAnswerCallbackQuery(chatId, "nothing", callbackId, lang);
        }
        String text = combinationService.getCustomParamsInfo(chatId, selected);
        return messageSender.createMessageWithBackButton(chatId, text, lang);
    }
}
