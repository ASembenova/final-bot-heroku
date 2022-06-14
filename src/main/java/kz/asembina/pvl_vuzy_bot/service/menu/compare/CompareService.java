package kz.asembina.pvl_vuzy_bot.service.menu.compare;

import com.vdurmont.emoji.EmojiParser;
import kz.asembina.pvl_vuzy_bot.db.domain.GeneratedDocument;
import kz.asembina.pvl_vuzy_bot.egovapi.CombinationService;
import kz.asembina.pvl_vuzy_bot.egovapi.DataObjectService;
import kz.asembina.pvl_vuzy_bot.egovapi.Vuz;
import kz.asembina.pvl_vuzy_bot.model.Speciality;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.PDFService;
import kz.asembina.pvl_vuzy_bot.service.SplitterService;
import kz.asembina.pvl_vuzy_bot.service.db.GeneratedDocumentService;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import kz.asembina.pvl_vuzy_bot.service.menu.speciality.SpecialityService;
import kz.asembina.pvl_vuzy_bot.service.report.AllParamsService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CompareService {

    private final SpecialityService specialityService;
    private final PDFService pdfService;
    private final MessageSender messageSender;
    private final LocaleService localeService;
    private final CombinationService combinationService;
    private final SplitterService splitterService;
    private final AllParamsService allParamsService;
    private final GeneratedDocumentService documentService;
    private final SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Map <Long, int[]> selectedButtons;

    public CompareService(SpecialityService specialityService, PDFService pdfService, MessageSender messageSender, LocaleService localeService, DataObjectService dataObjectService, CombinationService combinationService, SplitterService splitterService, AllParamsService allParamsService, GeneratedDocumentService documentService) {
        this.specialityService = specialityService;
        this.pdfService = pdfService;
        this.messageSender = messageSender;
        this.localeService = localeService;
        this.combinationService = combinationService;
        this.splitterService = splitterService;
        this.allParamsService = allParamsService;
        this.documentService = documentService;
        selectedButtons = new HashMap<>();
    }

    public SendMessage getCompareModesMessage(long chatId, String lang) {
        return messageSender.createMessageWithKeyboardByTags(chatId, "compare_spec.instr", getKeyboard(lang), lang);
    }

    public ReplyKeyboardMarkup getKeyboard(String lang){
        return messageSender.getMenuKeyboard(Arrays.asList("menu.compare_byname", "menu.compare_bycode", "menu.compare_byname_and_code", "back"), lang);
    }

    public SendMessage getCompareMessage(long chatId, String compareMode, String lang) {
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineMessageButtons(chatId, compareMode, new int[2], lang);
        String text = getComparingMessageDecription(lang, compareMode);
        return messageSender.createMessageWithInlineKeyboard(chatId, text, inlineKeyboardMarkup);
    }

    public String getComparingMessageDecription(String lang, String compareMode){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(localeService.getMessage(compareMode+".instr", lang));
        List<String> listOfVuz = combinationService.getVuzList(lang);
        stringBuilder.append("\n\n<b>");
        for (int i = 0; i < listOfVuz.size(); i++) {
            stringBuilder.append(i+1).append(" - ").append(listOfVuz.get(i)).append("\n");
        }
        stringBuilder.append("</b>");
        return stringBuilder.toString();
    }


    public EditMessageText getEditedComparingMessage(long chatId, long messageId, String compareMode, int[] selected, String lang){
        EditMessageText newMessage = new EditMessageText();
        newMessage.setMessageId((int) messageId);
        newMessage.setText(getComparingMessageDecription(lang, compareMode));
        newMessage.setChatId(String.valueOf(chatId));
        newMessage.setReplyMarkup(getInlineMessageButtons(chatId, compareMode, selected, lang));
        newMessage.setParseMode("html");
        return newMessage;
    }

    private InlineKeyboardMarkup getInlineMessageButtons(long chatId, String compareMode, int[] selected, String lang) {
        if(!selectedButtons.containsKey(chatId)){
            selectedButtons.put(chatId, selected);
        }
        int first = selectedButtons.get(chatId)[0];
        int second = selectedButtons.get(chatId)[1];
        if(selected[0]==0){
            second = selected[1];
        }
        if (selected[1]==0){
            first = selected[0];
        }
        selectedButtons.put(chatId, new int[]{first, second});
        int numOfVuz = combinationService.getVuzList(lang).size();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i=0; i<numOfVuz; i++) {
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            if(first==i+1){
                button1.setText((i+1)+" \u2714");
            } else{
                button1.setText(String.valueOf(i+1));
            }
            button1.setCallbackData(compareMode+ " first "+(i+1));
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            if(second==i+1){
                button2.setText((i+1)+"\u2714");
            } else{
                button2.setText(String.valueOf(i+1));
            }
            button2.setCallbackData(compareMode + " second "+(i+1));
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(button1);
            keyboardButtonsRow.add(button2);
            rowList.add(keyboardButtonsRow);
        }
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode(localeService.getMessage("btn.generate_pdf", lang)));
        button.setCallbackData("generate_pdf"+" "+compareMode+" "+first+" "+second);
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(button);
        rowList.add(keyboardButtonsRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }


    public void getComparingResults(long chatId, Vuz v1, Vuz v2, String compareMode, String firstname, String lastname, Date dateOrder){
        Set<Speciality> different1; Set<Speciality> different2; Set<Speciality> common1; Set<Speciality> common2;
        String lang = localeService.getLocaleTag(chatId);
        List<Set<Speciality>> differences;
        List<Set<Speciality>> intersections;
        switch (compareMode){
            case "compare_byname":
                differences = specialityService.getDifferenceByName(v1,v2, lang);
                intersections = specialityService.getIntersectionByName(v1,v2, lang);
                break;
            case "compare_bycode":
                differences = specialityService.getDifferenceByCode(v1,v2, lang);
                intersections = specialityService.getIntersectionByCode(v1,v2, lang);
                break;
            default:
                differences = specialityService.getDifferenceByNameAndCode(v1,v2, lang);
                intersections = specialityService.getIntersectionByNameAndCode(v1,v2, lang);
                break;
        }
        different1 = differences.get(0);
        different2 = differences.get(1);
        common1 = intersections.get(0);
        common2 = intersections.get(1);
        List<Speciality> diff1List = new ArrayList<>(); diff1List.addAll(different1);
        List<Speciality> diff2List = new ArrayList<>(); diff2List.addAll(different2);
        List<Speciality> common1List = new ArrayList<>(); common1List.addAll(common1);
        List<Speciality> common2List = new ArrayList<>(); common2List.addAll(common2);
        int[] sizes = {different1.size(), common1.size(), common2.size(), different2.size()};
        int maxSize = Arrays.stream(sizes).max().getAsInt();
        Speciality[][] array = new Speciality[maxSize][4];
        for (int i = 0; i < array.length; i++) {
            if(i<sizes[0]){
                array[i][0] = diff1List.get(i);
            }
            if(i<sizes[1]){
                array[i][1] = common1List.get(i);
            }
            if(i<sizes[2]){
                array[i][2] = common2List.get(i);
            }
            if(i<sizes[3]){
                array[i][3] = diff2List.get(i);
            }
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("counter", String.format("%06d", documentService.getCounter()));

        variables.put("compare_param", localeService.getMessage(compareMode+".param", lang));
        String name1; String name2;
        if (localeService.getLocaleTag(chatId).equals("kz")){
            name1 = splitterService.splitFullname(v1.name1);
            name2 = splitterService.splitFullname(v2.name1);
        } else {
            name1 = splitterService.splitFullname(v1.name2);
            name2 = splitterService.splitFullname(v2.name2);
        }
        long current = System.currentTimeMillis();
        Date date = new Date(current);
        Time time = new Time(current);
        String datetime = formater.format(date);
        variables.put("vuz1_name", name1);
        variables.put("vuz2_name", name2);
        variables.put("firstname", firstname);
        variables.put("lastname", lastname);
        variables.put("vuz1_total", (sizes[0]+sizes[1]));
        variables.put("vuz2_total", (sizes[2]+sizes[3]));
        variables.put("common_total", sizes[1]);
        variables.put("array", array);
        variables.put("datetime", datetime);
        StringBuilder title = new StringBuilder();
        title.append(localeService.getMessage("compare", lang)).append(" ");
        title.append(localeService.getMessage(compareMode+".param", lang)).append(" : ");
        title.append(name1).append(", ").append(name2);
        try {
            File file = pdfService.generatePDF(variables, "compare_"+ localeService.getLocaleTag(chatId)).get();
            GeneratedDocument document = new GeneratedDocument();
            document.setChatId(chatId);
            document.setTitle(title.toString());
            document.setDate(date);
            document.setTime(time);
            document.setCustomer(firstname+" "+lastname);
            String fileId = messageSender.sendPdf(chatId, file, firstname, lastname, date);
            document.setFileId(fileId);
            documentService.save(document);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<i>").append(localeService.getMessage("preparing_doc_take", lang)).append(" ");
            stringBuilder.append((new Date(System.currentTimeMillis()).getTime()-dateOrder.getTime())/1000.0).append(" секунд").append("</i>");
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(stringBuilder.toString());
            sendMessage.setParseMode("html");
            messageSender.execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

