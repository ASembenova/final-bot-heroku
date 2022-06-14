package kz.asembina.pvl_vuzy_bot.service;
import com.vdurmont.emoji.EmojiParser;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MessageSender {
    @Autowired
    @Lazy
    private AbsSender bot;
    private final LocaleService localeService;

    public MessageSender(LocaleService localeService) {
        this.localeService = localeService;
    }

    public SendMessage createMessageWithKeyboard(long chatId, String text, List<String> list) {
        ReplyKeyboardMarkup replyKeyboardMarkup = null;
        if (list!=null){
            replyKeyboardMarkup = getMenuKeyboard(list);
        }
        return createMessageWithKeyboard(chatId, text, replyKeyboardMarkup);
    }

    public SendMessage createMessageWithKeyboard(long chatId, String replyText, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(replyText);
        sendMessage.setParseMode("html");
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    public ReplyKeyboardMarkup getMenuKeyboard(List<String> namesOfButtons) {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (String name: namesOfButtons) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(EmojiParser.parseToUnicode(name)));
            keyboard.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public SendMessage createMessageWithKeyboardByTags(long chatId, String textTag, List<String> tags, String lang) {
        ReplyKeyboardMarkup replyKeyboardMarkup = null;
        String text = EmojiParser.parseToUnicode(localeService.getMessage(textTag, lang));
        if (tags!=null){
            replyKeyboardMarkup = getMenuKeyboard(tags, lang);
        }
        return createMessageWithKeyboard(chatId, text, replyKeyboardMarkup);
    }

    public SendMessage createMessageWithKeyboardByTags(long chatId, String textTag, ReplyKeyboardMarkup replyKeyboardMarkup, String lang) {
        String text = EmojiParser.parseToUnicode(localeService.getMessage(textTag, lang));
        return createMessageWithKeyboard(chatId, text, replyKeyboardMarkup);
    }

    public ReplyKeyboardMarkup getMenuKeyboard(List<String> tags, String lang) {
        List<String> namesOfButtons = getButtonList(tags, lang);
        return getMenuKeyboard(namesOfButtons);
    }

    public SendMessage createMessageWithInlineKeyboard(final long chatId, String textMessage, final InlineKeyboardMarkup inlineKeyboardMarkup) {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        sendMessage.setParseMode("html");
        if (inlineKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        return sendMessage;
    }

    public String sendPdf(long chatId, File file, String firstname, String lastname, Date date){
        String fileId = null;
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(new InputFile(file));
        sendDocument.setChatId(String.valueOf(chatId));
        try {
            fileId = bot.execute(sendDocument).getDocument().getFileId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return fileId;
    }

    public List<String> getButtonList(List<String> tags, String lang){
        List<String> list = new ArrayList<>();
        for(String tag:tags){
            list.add(EmojiParser.parseToUnicode(localeService.getMessage(tag, lang)));
        }
        return list;
    }

    public AnswerCallbackQuery getAnswerCallbackQuery(long chatId, String messageTag, String callbackQueryId, String lang){
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setText(localeService.getMessage(messageTag, lang));
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        return answerCallbackQuery;
    }

    public SendMessage createMessageWithBackButton(long chatId, String text, String lang){
        return createMessageWithKeyboard(chatId, text, getButtonList(Arrays.asList("back"), lang));
    }


    public void execute(BotApiMethod botApiMethod){
        try {
            this.bot.execute(botApiMethod);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsgToDeveloper(Message message, String lang){
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId("5244146363");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(localeService.getMessage("help.write.title", lang));
        stringBuilder.append(" ").append(message.getFrom().getFirstName());
        if(message.getFrom().getLastName()!=null){
            stringBuilder.append(" ").append(message.getFrom().getLastName());
        }
        stringBuilder.append("\n").append(message.getText());
        sendMessage.setText(stringBuilder.toString());
        try {
            if(sendMessage.getText()!=null && sendMessage.getChatId()!=null){
                this.bot.execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
