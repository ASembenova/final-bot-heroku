package kz.asembina.pvl_vuzy_bot.telegram.handler;

import kz.asembina.pvl_vuzy_bot.egovapi.CombinationService;
import kz.asembina.pvl_vuzy_bot.service.MessageSender;
import kz.asembina.pvl_vuzy_bot.service.db.LangService;
import kz.asembina.pvl_vuzy_bot.service.menu.*;
import kz.asembina.pvl_vuzy_bot.model.Step;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import kz.asembina.pvl_vuzy_bot.service.memory.StepServiceImpl;
import kz.asembina.pvl_vuzy_bot.service.menu.compare.CompareService;
import kz.asembina.pvl_vuzy_bot.service.report.AllParamsService;
import kz.asembina.pvl_vuzy_bot.service.report.CustomParamsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Date;

@Component
public class MessageHandler {
    private final LocaleService localeService;
    private final LangService langService;
    private final MessageSender messageSender;
    private final MainMenuService menuService;
    private final SelectOneService selectOneService;
    private final SelectAllService selectAllService;
    private final SearchService searchService;
    private final AboutService aboutService;
    private final CompareService compareService;
    private final AllParamsService allParamsService;
    private final StepServiceImpl stepService;
    private final CustomParamsService customParamsService;
    private final CombinationService combinationService;
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);


    public MessageHandler(LocaleService localeService, LangService langService, MessageSender messageSender, MainMenuService menuService, SelectOneService selectOneService, SelectAllService selectAllService, SearchService searchService, AboutService aboutService, CompareService compareService, AllParamsService allParamsService, StepServiceImpl stepService, CustomParamsService customParamsService, CombinationService combinationService) {
        this.localeService = localeService;
        this.langService = langService;
        this.messageSender = messageSender;
        this.menuService = menuService;
        this.selectOneService = selectOneService;
        this.selectAllService = selectAllService;
        this.searchService = searchService;
        this.aboutService = aboutService;
        this.compareService = compareService;
        this.allParamsService = allParamsService;
        this.stepService = stepService;
        this.customParamsService = customParamsService;
        this.combinationService = combinationService;
    }

    public BotApiMethod<?> handle(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();
        String lang = localeService.getLocaleTag(chatId);
        Step newStep = null;
        Step currentStep = null;
        if(!stepService.containsUser(chatId) || messageText.equals("/start")){
            localeService.addNewUser(chatId);
            langService.saveNewUser(chatId, message.getFrom().getFirstName(), message.getFrom().getLastName());
            currentStep = Step.START;
            stepService.addUser(chatId);
            newStep = nextStep(currentStep, messageText, chatId, lang);
        } else{
            if(messageText.equals(localeService.getMessage("back", lang))) {
                currentStep = stepService.deleteLastStep(chatId);
                newStep = stepService.getLastStep(chatId);
            } else {
                currentStep = stepService.getLastStep(chatId);
                if(messageText.equals(localeService.getMessage("help", lang))){
                    newStep = Step.HELP;
                } else{
                    newStep = nextStep(currentStep, messageText, chatId, lang);
                }
            }
            logger.info("Current: "+stepService.getStack(chatId).peek().name());
        }
        stepService.addStep(chatId, newStep);
        logger.info("New: "+stepService.getStack(chatId).peek().name());
        return processNewStep(newStep, chatId, message, lang, currentStep);
    }

    private BotApiMethod<?> processNewStep(Step step, long chatId, Message message, String lang, Step currentStep) {
        String messageText = message.getText();
        String stepName = step.name();
        if(currentStep.name().equals(Step.LANG_MENU) || currentStep.name().equals(Step.WELCOME)){
            lang = localeService.getLocaleTag(chatId);
        }
        switch (stepName) {
            case "WELCOME":
                return menuService.getWelcomeMsg(chatId, lang);
            case "MAIN_MENU":
                return menuService.getMainMenuMsg(chatId, lang);
            case "SELECT_ALL":
                return selectAllService.getSelectAllMsg(chatId, lang);
            case "SELECT_ONE":
                if (combinationService.getVuzList(lang).toString().contains(messageText)) {
                    return selectOneService.getOneVuzInfo(chatId, messageText, lang);
                } else {
                    return selectOneService.getSelectOneMsg(chatId, lang);
                }
            case "LANG_MENU":
                if (messageText.equals(localeService.getMessage("change_lang", lang))) {
                    return menuService.getSelectLangMsg(chatId, lang);
                }
                if (messageText.equals(localeService.getMessage("lang.kz", lang))) {
                    localeService.changeLang("kz", chatId);
                    langService.changeLang(chatId, "kz");
                    lang="kz";
                } else if (messageText.equals(localeService.getMessage("lang.ru", lang))) {
                    localeService.changeLang("ru", chatId);
                    langService.changeLang(chatId, "ru");
                    lang="ru";
                }
                stepService.deleteLastStep(chatId);
                return menuService.getMainMenuMsg(chatId, lang);
            case "PARAMS_ALL":
                if (messageText.equals(localeService.getMessage("all_params", lang))) {
                    return allParamsService.getAllParamsMsg(chatId, lang);
                } else if (messageText.equals(localeService.getMessage("btn.generate_pdf", lang))) {
                    Date date = new Date(System.currentTimeMillis());
                    String text = allParamsService.getAllParamsPassport(chatId, message.getFrom().getFirstName(), message.getFrom().getLastName(), date, lang);
                    return messageSender.createMessageWithKeyboard(chatId, text, allParamsService.getKeyboard(lang));
                }
                return null;
            case "PARAMS_CUSTOM":
                return customParamsService.getChooseParamsMsg(chatId, lang);
            case "COMPARE":
                if(messageText.equals(localeService.getMessage("compare_spec", lang))){
                    return compareService.getCompareModesMessage(chatId, lang);
                }
                String compareMode = "";
                if(messageText.equals(localeService.getMessage("menu.compare_byname", lang))){
                    compareMode = "compare_byname";
                } else if(messageText.equals(localeService.getMessage("menu.compare_bycode", lang))){
                    compareMode = "compare_bycode";
                } else if(messageText.equals(localeService.getMessage("menu.compare_byname_and_code", lang))){
                    compareMode = "compare_byname_and_code";
                }
                return compareService.getCompareMessage(chatId, compareMode, lang);
            case "SEARCH":
                if (messageText.equals(localeService.getMessage("search", lang))) {
                    return searchService.getSearchMsg(chatId, lang);
                } else {
                    return searchService.getSearchResults(chatId, messageText, lang);
                }
            case "HELP":
                if (messageText.equals(localeService.getMessage("help", lang))) {
                    return aboutService.getHelpMessage(chatId, lang);
                } else if (messageText.equals(localeService.getMessage("help.dataset_info", lang))) {
                    return aboutService.getHelpDatasetPassportMessage(chatId, lang);
                } else if (messageText.equals(localeService.getMessage("help.opendata", lang))) {
                    return aboutService.getHelpOpenDataMessage(chatId, lang);
                } else if (messageText.equals(localeService.getMessage("help.dataegov", lang))) {
                    return aboutService.getHelpDataEgovMessage(chatId, lang);
                } else if (messageText.equals(localeService.getMessage("help.write", lang))) {
                    return aboutService.getHelpWriteMessage(chatId, lang);
                } else messageSender.sendMsgToDeveloper(message, lang);
        }
        return null;
    }

    private Step nextStep(Step currentStep, String messageText, long chatId, String lang) {
        String name = currentStep.name();
        switch (name){
            case "START":
                return Step.WELCOME;
            case "WELCOME":
                if(messageText.equals(localeService.getMessage("lang.kz", lang))){
                    localeService.changeLang("kz", chatId);
                    langService.changeLang(chatId, "kz");
                } else {
                    localeService.changeLang("ru", chatId);
                    langService.changeLang(chatId, "ru");
                }
                return Step.MAIN_MENU;
            case "MAIN_MENU":
                if(messageText.equals(localeService.getMessage("select_all", lang))){
                    return Step.SELECT_ALL;
                } else if(messageText.equals(localeService.getMessage("select_one", lang))){
                    return Step.SELECT_ONE;
                } else if(messageText.equals(localeService.getMessage("change_lang", lang))){
                    return Step.LANG_MENU;
                } else {
                    return Step.MAIN_MENU;
                }
            case "PARAMS_CUSTOM":
                stepService.deleteLastStep(chatId);
            case "SELECT_ALL":
                if(messageText.equals(localeService.getMessage("all_params", lang))){
                    return Step.PARAMS_ALL;
                } else if(messageText.equals(localeService.getMessage("select_params", lang))){
                    return Step.PARAMS_CUSTOM;
                } else if(messageText.equals(localeService.getMessage("compare_spec", lang))){
                    return Step.COMPARE;
                } else if(messageText.equals(localeService.getMessage("search", lang))){
                    return Step.SEARCH;
                } else {
                    return Step.SELECT_ALL;
                }
        }
        return currentStep;
    }


}
