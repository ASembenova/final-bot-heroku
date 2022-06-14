package kz.asembina.pvl_vuzy_bot.db.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Locale;

@Entity
public class Lang {

    @Id
    private Long chatId;

    private Locale locale;

    private String firstname;

    private String lastname;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public String toString() {
        return "Lang{" +
                "chatId=" + chatId +
                ", locale=" + locale +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}
