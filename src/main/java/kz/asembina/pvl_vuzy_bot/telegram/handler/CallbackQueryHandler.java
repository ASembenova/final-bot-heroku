package kz.asembina.pvl_vuzy_bot.telegram.handler;

import kz.asembina.pvl_vuzy_bot.egovapi.DataObjectService;
import kz.asembina.pvl_vuzy_bot.egovapi.Vuz;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import kz.asembina.pvl_vuzy_bot.service.menu.SelectOneService;
import kz.asembina.pvl_vuzy_bot.service.menu.compare.CompareService;
import kz.asembina.pvl_vuzy_bot.service.report.CustomParamsService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.sql.Date;
import java.util.concurrent.CompletableFuture;

@Component
public class CallbackQueryHandler {

    private final SelectOneService selectOneService;
    private final MessageSender messageSender;
    private final DataObjectService dataObjectService;
    private final CompareService compareService;
    private final CustomParamsService customParamsService;
    private final LocaleService localeService;

    public CallbackQueryHandler(SelectOneService selectOneService, MessageSender messageSender, DataObjectService dataObjectService, CompareService compareService, CustomParamsService customParamsService, LocaleService localeService) {
        this.selectOneService = selectOneService;
        this.messageSender = messageSender;
        this.dataObjectService = dataObjectService;
        this.compareService = compareService;
        this.customParamsService = customParamsService;
        this.localeService = localeService;
    }

    public BotApiMethod<?> handle(CallbackQuery callbackQuery) {
        final long chatId = callbackQuery.getMessage().getChatId();
        final String callbackId = callbackQuery.getId();
        final long messageId = callbackQuery.getMessage().getMessageId();
        final String lang = localeService.getLocaleTag(chatId);
        String data = callbackQuery.getData();
        if (data.contains("btn")){
            return processCallbackBtn(chatId, data, messageId, lang);
        } else if(data.contains("generate_pdf")){
            Date date = new Date(System.currentTimeMillis());
            return generatePdfComparingSpec(chatId, data, callbackId, callbackQuery, date, lang);
        } else if (data.contains("compare")){
            return addCheckMarkVuz(chatId, data, messageId, lang);
        } else if (data.contains("name")){
            return addCheckMarkParams(chatId, data, messageId);
        } else if (data.contains("custom_params_msg")){
            return generateByParams(chatId, callbackId, lang);
        }
        System.out.println("process call back fall");
        return null;
    }

    private BotApiMethod<?> generateByParams(long chatId, String callbackId, String lang) {
        return customParamsService.generateReportByParams(chatId, callbackId, lang);
    }

    private BotApiMethod<?> addCheckMarkVuz(long chatId, String data, long messageId, String lang) {
        String[] params = data.split(" "); //example: "compare_byname first 1"
        int[] selected = new int[2];
        if(params[1].equals("first")){
            selected[0] = Integer.parseInt(params[2]);
        } else if(params[1].equals("second")){
            selected[1] = Integer.parseInt(params[2]);
        }
        return compareService.getEditedComparingMessage(chatId, messageId, data.split(" ")[0], selected, lang);
    }

    private BotApiMethod<?> addCheckMarkParams(long chatId, String data, long messageId) {
        int num = Integer.parseInt(data.substring(4)); // "name11"
        return customParamsService.getEditedCustomParametersMessage(chatId, messageId, num-1);
    }

    private BotApiMethod<?> generatePdfComparingSpec(long chatId, String data, String callbackId, CallbackQuery callbackQuery, Date dateOrder, String lang) {
        String[] params = data.split(" ");
        String compareMode = params[1];
        int first = Integer.parseInt(params[2]);
        int second = Integer.parseInt(params[3]);
        if(first!=0&&second!=0){
            if(first==second){
                return messageSender.getAnswerCallbackQuery(chatId, "compare.choose_other", callbackId, lang);
            }
            messageSender.execute(messageSender.getAnswerCallbackQuery(chatId,
                            "compare.wait", callbackId, lang));
            Vuz vuz1 = dataObjectService.getVuzy()[first-1];
            Vuz vuz2 = dataObjectService.getVuzy()[second-1];
            compareService.getComparingResults(chatId, vuz1, vuz2, compareMode, callbackQuery.getFrom().getFirstName(), callbackQuery.getFrom().getLastName(), dateOrder);
        } else{
            return messageSender.getAnswerCallbackQuery(chatId, "compare.not_selected", callbackId, lang);
        }
        return null;
    }

    private BotApiMethod<?> processCallbackBtn(long chatId, String data, long messageId, String lang) {
        int index = Integer.valueOf(String.valueOf(data.charAt(data.length()-1)));
        data = data.substring(0, data.length()-1);
        switch (data){
            case ("btn.previous"):
                return selectOneService.getMessageWithNextVuz(chatId, messageId, index, lang);
            case ("btn.next"):
                return selectOneService.getMessageWithPreviousVuz(chatId, messageId, index, lang);
            case ("btn.speclist"):
                return selectOneService.getMessageWithSpecList(chatId, index, lang);
            case ("btn.geo"):
                return selectOneService.getMessageWithLocation(chatId, index);
            case ("btn.call"):
                return selectOneService.getMessageWithContact(chatId, index);
            case ("btn.site"):
                return selectOneService.getMessageWithContact(chatId, index);
        }
        return null;
    }
}

