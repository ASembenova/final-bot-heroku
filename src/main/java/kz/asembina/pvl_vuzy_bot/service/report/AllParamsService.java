package kz.asembina.pvl_vuzy_bot.service.report;

import kz.asembina.pvl_vuzy_bot.db.domain.GeneratedDocument;
import kz.asembina.pvl_vuzy_bot.egovapi.CombinationService;
import kz.asembina.pvl_vuzy_bot.egovapi.DataObjectService;
import kz.asembina.pvl_vuzy_bot.egovapi.Metadata;
import kz.asembina.pvl_vuzy_bot.egovapi.Vuz;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.PDFService;
import kz.asembina.pvl_vuzy_bot.service.db.GeneratedDocumentService;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AllParamsService {
    private final PDFService pdfService;
    private final MessageSender messageSender;
    private final LocaleService localeService;
    private final DataObjectService dataObjectService;
    private final CombinationService combinationService;
    private final GeneratedDocumentService documentService;
    private final Metadata metadata;
    SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AllParamsService(PDFService pdfService, MessageSender messageSender, LocaleService localeService, DataObjectService dataObjectService, CombinationService combinationService, GeneratedDocumentService documentService, Metadata metadata) {
        this.pdfService = pdfService;
        this.messageSender = messageSender;
        this.localeService = localeService;
        this.dataObjectService = dataObjectService;
        this.combinationService = combinationService;
        this.documentService = documentService;
        this.metadata = metadata;
    }

    public BotApiMethod<?> getAllParamsMsg(long chatId, String lang) {
        String text = combinationService.getAllParamsInfo(chatId);
        return messageSender.createMessageWithKeyboard(chatId, text, getKeyboard(lang));
    }

    public ReplyKeyboardMarkup getKeyboard(String lang){
        return messageSender.getMenuKeyboard(Arrays.asList("btn.generate_pdf", "back"), lang);
    }

    public String getAllParamsPassport(long chatId, String firstname, String lastname, Date dateOrder, String lang){
        boolean[] b = new boolean[18];
        for (int i = 0; i < b.length; i++) {
            b[i] = true;
        }
        Map<String, Object> variables = this.getPassportVariables(dataObjectService.getVuzy(), chatId, b);
        variables.put("firstname", firstname);
        variables.put("lastname", lastname);
        long current = System.currentTimeMillis();
        java.sql.Date date = new java.sql.Date(current);
        Time time = new Time(current);
        variables.put("datetime", formater.format(date));
        try {
            File file = pdfService.generatePDF(variables, "params_"+ localeService.getLocaleTag(chatId)).get();
            GeneratedDocument document = new GeneratedDocument();
            document.setChatId(chatId);
            document.setDate(date);
            document.setTime(time);
            document.setCustomer(firstname+" "+lastname);
            String fileId = messageSender.sendPdf(chatId, file, firstname, lastname, date);
            document.setFileId(fileId);
            documentService.save(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<i>").append(localeService.getMessage("preparing_doc_take", lang)).append(" ");
        stringBuilder.append((new java.sql.Date(System.currentTimeMillis()).getTime()-dateOrder.getTime())/1000.0).append(" секунд").append("</i>");
        return stringBuilder.toString();
    }

    private Map<String, Object> getPassportVariables(Vuz[] vuzy, long chatId, boolean[] paramsFlag){
        int paramsCount = 0;
        for (boolean flag: paramsFlag) {
            if(flag==true){
                paramsCount++;
            }
        }
        String[][] array = new String[paramsFlag.length][vuzy.length+1];
        Field[] fields = dataObjectService.getVuzFields();
        Map<Integer, String> meta;
        if(localeService.getLocaleTag(chatId).equals("kz")){
            meta = dataObjectService.getMetaKz();
        } else{
            meta = dataObjectService.getMetaRu();
        }
        for (int i = 0; i < paramsFlag.length; i++) {
            for (int j = 0; j < vuzy.length; j++) {
                array[i][0] = meta.get(i+1);
                if(paramsFlag[i]==false){
                    break;
                } else {
                    try {
                        array[i][j+1] = fields[i].get(vuzy[j]).toString();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("array", array);
        //variables.put("counter", String.format("%06d", documentDBService.getCounter()));
        variables.put("numOfVuz", vuzy.length);
        return variables;
    }


}


