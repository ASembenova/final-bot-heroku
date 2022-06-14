package kz.asembina.pvl_vuzy_bot.telegram;

import kz.asembina.pvl_vuzy_bot.telegram.handler.UpdateHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;


public class PvlVuzyBot extends SpringWebhookBot {
    private String botPath;
    private String botUsername;
    private String botToken;

    private final UpdateHandler updateHandler;

    public PvlVuzyBot(SetWebhook setWebhook, UpdateHandler updateHandler) {
        super(setWebhook);
        this.updateHandler = updateHandler;
    }

    public PvlVuzyBot(DefaultBotOptions options, SetWebhook setWebhook, UpdateHandler updateHandler) {
        super(options, setWebhook);
        this.updateHandler = updateHandler;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        BotApiMethod<?> botApiMethod = updateHandler.handleUpdate(update);
        if (botApiMethod!=null && botApiMethod.getClass() == SendMessage.class) {
            return sendLongMsgByParts(botApiMethod);
        }
        return botApiMethod;
    }

    public BotApiMethod<?> sendLongMsgByParts(BotApiMethod<?> botApiMethod){
        SendMessage sendMessage = (SendMessage) botApiMethod;
        int size = sendMessage.getText().length();
        if (size > 4096) {
            String text = sendMessage.getText();
            String chatId = sendMessage.getChatId();
            for (int i = 0; i < size; i += 4096) {
                SendMessage part = new SendMessage();
                if (i < size - 4096) {
                    part.setText(text.substring(i, i + 4096));
                } else {
                    part.setText(text.substring(i, size - 1));
                }
                part.setChatId(chatId);
                part.setParseMode("html");
                try {
                    this.execute(part);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return botApiMethod;
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    public void setBotPath(String botPath) {
        this.botPath = botPath;
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
}
