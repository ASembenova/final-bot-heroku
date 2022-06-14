package kz.asembina.pvl_vuzy_bot.service.menu;

import com.vdurmont.emoji.EmojiParser;
import kz.asembina.pvl_vuzy_bot.egovapi.CombinationService;
import kz.asembina.pvl_vuzy_bot.egovapi.DataObjectService;
import kz.asembina.pvl_vuzy_bot.egovapi.Vuz;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.SplitterService;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import kz.asembina.pvl_vuzy_bot.service.menu.speciality.SpecialityService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendContact;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SelectOneService {

    private final DataObjectService dataObjectService;
    private final MessageSender messageSender;
    private final LocaleService localeService;
    private final CombinationService combinationService;
    private final SpecialityService specialityService;
    private final SplitterService splitterService;
    private final List<List<String>> list = new ArrayList<>();

    public SelectOneService(DataObjectService dataObjectService, MessageSender messageSender, LocaleService localeService, CombinationService combinationService, SpecialityService specialityService, SplitterService splitterService) {
        this.dataObjectService = dataObjectService;
        this.messageSender = messageSender;
        this.localeService = localeService;
        this.combinationService = combinationService;
        this.specialityService = specialityService;
        this.splitterService = splitterService;
        list.add(Arrays.asList("btn.previous", "btn.next"));
        list.add(Arrays.asList("btn.speclist"));
        list.add(Arrays.asList("btn.geo"));
        list.add(Arrays.asList("btn.call"));
        list.add(Arrays.asList("btn.site"));
    }

    public BotApiMethod<?> getSelectOneMsg(long chatId, String lang) {
        List<String> namesOfButtons = getKeyboardList(lang);
        return messageSender.createMessageWithKeyboard(chatId, localeService.getMessage(
                "select_one.msg", lang), namesOfButtons);
    }

    public List<String> getKeyboardList(String lang){
        List<String> namesOfButtons = new ArrayList<>();
        namesOfButtons.addAll(combinationService.getVuzList(lang));
        namesOfButtons.addAll(messageSender.getButtonList(Arrays.asList("help", "back"), lang));
        return namesOfButtons;
    }


    public SendMessage getOneVuzInfo(long chatId, String vuzName, String lang){
        int index = combinationService.getIndexByVuzName(vuzName, chatId, lang);
        return getOneVuzInfo(chatId, index, lang);
    }

    private SendMessage getOneVuzInfo(long chatId, int index, String lang){
        String textMessage = combinationService.oneVuzInfo(index, chatId);
        final InlineKeyboardMarkup inlineKeyboardMarkup = getInlineMessageButtons(index, chatId, lang);
        return messageSender.createMessageWithInlineKeyboard(chatId, textMessage, inlineKeyboardMarkup);
    }

    private InlineKeyboardMarkup getInlineMessageButtons(int index, long chatId, String lang) {
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (List<String> listTag:list) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (String tag:listTag) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(localeService.getMessage(tag, lang));
                btn.setCallbackData(tag+index);
                if(tag.equals("btn.site")){
                    btn.setUrl(combinationService.getSiteUrl(index));
                }
                row.add(btn);
            }
            rowList.add(row);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
    public EditMessageText getEditedMessageAboutVuz(long chatId, long messageId, int newIndex, String lang){
        EditMessageText newMessage = new EditMessageText();
        newMessage.setMessageId((int) messageId);
        newMessage.setText(EmojiParser.parseToUnicode(combinationService.oneVuzInfo(newIndex, chatId)));
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setReplyMarkup((InlineKeyboardMarkup) getOneVuzInfo(chatId, newIndex, lang).getReplyMarkup());
        newMessage.setParseMode("html");
        return newMessage;
    }

    public EditMessageText getMessageWithNextVuz(long chatId, long messageId, int index, String lang){
        int newIndex = index+1;
        if(newIndex>=dataObjectService.getVuzy().length){
            newIndex=0;
        }
        return getEditedMessageAboutVuz(chatId, messageId, newIndex, lang);
    }

    public EditMessageText getMessageWithPreviousVuz(long chatId, long messageId, int index, String lang){
        int newIndex = index-1;
        if(newIndex<0){
            newIndex = dataObjectService.getVuzy().length-1;
        }
        return getEditedMessageAboutVuz(chatId, messageId, newIndex, lang);
    }

    public SendMessage getMessageWithSpecList(long chatId, int index, String lang) {
        Vuz vuz = dataObjectService.getVuzy()[index];
        int count = specialityService.getSimpleListByRegex(vuz, chatId).size();
        String data;
        String vuzName;
        if (localeService.getLocaleTag(chatId).equals("kz")) {
            data = vuz.name6;
            vuzName = splitterService.splitFullname(vuz.name1);
        } else {
            data = vuz.name7;
            vuzName = splitterService.splitFullname(vuz.name2);
        }
        data = data.replaceAll(", 6", ",\n6");
        data = data.replaceAll(",6", ",\n6");
        data = data.replaceAll(",  6", ",\n6");
        data = data.replaceAll(", 5", ",\n5");
        data = data.replaceAll(", 7", ",\n7");
        data = data.replaceAll(", 8", ",\n8");
        StringBuilder result = new StringBuilder();
        result.append("<b>").append(vuzName).append("</b>").append("\n\n");
        result.append("<i>").append(localeService.getMessage("spec_list", lang)).append("</i>").append("\n\n");
        result.append(data).append("\n\n");
        result.append("<b>").append(localeService.getMessage("total", lang)).append(": "+count).append("</b>");
        return messageSender.createMessageWithKeyboard(chatId, result.toString(), getKeyboardList(lang));
    }

    public SendVenue getMessageWithLocation(long chatId, int index){
        Vuz vuz = dataObjectService.getVuzy()[index];
        String locationStr = vuz.name18;
        String address;
        String title;
        if (localeService.getLocaleTag(chatId).equals("kz")){
            address = vuz.name9 + ", " + vuz.name16;
            title = splitterService.splitFullname(vuz.name1);
        } else {
            address = vuz.name10 +", " + vuz.name17;
            title = splitterService.splitFullname(vuz.name2);
        }
        double[] location = splitterService.splitGeo(locationStr);
        return new SendVenue(String.valueOf(chatId), location[0], location[1], title, address); //return new SendLocation(String.valueOf(chatId), latitude, longtitude);
    }

    public SendContact getMessageWithContact(long chatId, int index) {
        String phone = splitterService.splitPhone(dataObjectService.getVuzy()[index].name11);
        String name = splitterService.splitFullname(dataObjectService.getVuzy()[index].name2);
        return new SendContact(String.valueOf(chatId),phone,name);
    }
}
