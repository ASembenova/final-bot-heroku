package kz.asembina.pvl_vuzy_bot.config;

import kz.asembina.pvl_vuzy_bot.telegram.PvlVuzyBot;
import kz.asembina.pvl_vuzy_bot.telegram.handler.UpdateHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
public class SpringConfig {
    private final BotConfig botConfig;
    public SpringConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(botConfig.getWebHookPath()).build();
    }

    @Bean
    public PvlVuzyBot springWebhookBot(SetWebhook setWebhook, UpdateHandler updateHandler) {
        PvlVuzyBot bot = new PvlVuzyBot(setWebhook, updateHandler);
        bot.setBotToken(botConfig.getBotToken());
        bot.setBotUsername(botConfig.getUserName());
        bot.setBotPath(botConfig.getWebHookPath());
        return bot;
    }
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}