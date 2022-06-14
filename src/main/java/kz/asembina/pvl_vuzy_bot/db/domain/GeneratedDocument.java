package kz.asembina.pvl_vuzy_bot.db.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Time;

@Entity
public class GeneratedDocument {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    public GeneratedDocument() {
    }

    private long chatId;

    private String customer;

    private String title;

    private String fileId;

    private String downloadLink;

    private Date date;

    private Time time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }


    @Override
    public String toString() {
        return "GeneratedDocument{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", customer='" + customer + '\'' +
                ", title='" + title + '\'' +
                ", fileId='" + fileId + '\'' +
                ", downloadLink='" + downloadLink + '\'' +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
