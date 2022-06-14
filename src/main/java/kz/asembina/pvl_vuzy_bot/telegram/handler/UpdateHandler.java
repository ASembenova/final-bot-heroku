package kz.asembina.pvl_vuzy_bot.telegram.handler;

import kz.asembina.pvl_vuzy_bot.service.memory.UpdateCounterService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateHandler {
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final UpdateCounterService updateCounterService;


    public UpdateHandler(CallbackQueryHandler callbackQueryHandler, MessageHandler messageHandler, UpdateCounterService updateCounterService) {
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.updateCounterService = updateCounterService;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()){
            Message message = update.getMessage();
            if(message.hasText()){
                updateCounterService.save(message.getChatId());
                return messageHandler.handle(message);
            }
        } else if(update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return callbackQueryHandler.handle(callbackQuery);
        }
        return null;
    }

}


