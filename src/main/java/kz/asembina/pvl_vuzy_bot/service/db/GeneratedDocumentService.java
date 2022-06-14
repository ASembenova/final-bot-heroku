package kz.asembina.pvl_vuzy_bot.service.db;

import com.google.gson.Gson;
import kz.asembina.pvl_vuzy_bot.db.domain.GeneratedDocument;
import kz.asembina.pvl_vuzy_bot.db.repo.DocumentRepo;
import kz.asembina.pvl_vuzy_bot.service.report.TelegramResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;

@Service
public class GeneratedDocumentService {
    @Value("https://api.telegram.org/bot${telegrambot.botToken}/getFile?file_id=")
    private String linkRequest;
    @Value("https://api.telegram.org/file/bot${telegrambot.botToken}/")
    private String linkFileBase;
    private final DocumentRepo documentRepo;

    private int counter;

    public GeneratedDocumentService(DocumentRepo documentRepo) {
        this.documentRepo = documentRepo;
    }

    public Set<GeneratedDocument> getDocs(long chatId){
        return documentRepo.findAllByChatId(chatId);
    }

    public List<GeneratedDocument> findAll(){
        return documentRepo.findAll();
    }

    @Async
    public void save(GeneratedDocument document){
        try {
            URL url = new URL(linkRequest+document.getFileId());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String result = response.toString();
            Gson gson = new Gson();
            TelegramResponse telegramResponse = gson.fromJson(result, TelegramResponse.class);
            document.setDownloadLink(linkFileBase+telegramResponse.result.file_path);
        }catch (IOException e){
            e.printStackTrace();
        }
        counter++;
        documentRepo.save(document);
    }

    public int getCounter() {
        if(counter==0){
            counter = documentRepo.findAll().size();
        }
        return counter;
    }
}
