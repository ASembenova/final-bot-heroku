package kz.asembina.pvl_vuzy_bot.egovapi;

import com.vdurmont.emoji.EmojiParser;
import kz.asembina.pvl_vuzy_bot.service.SplitterService;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CombinationService {
    private final DataObjectService dataObjectService;
    private final SplitterService splitterService;
    private final Vuz[] vuzy;
    private final Metadata metadata;
    private final LocaleService localeService;
    private final List<String> vuzListKz;
    private final List<String> vuzListRu;

    public CombinationService(DataObjectService dataObjectService, SplitterService splitterService, LocaleService localeService) {
        this.dataObjectService = dataObjectService;
        this.splitterService = splitterService;
        this.localeService = localeService;
        vuzy = dataObjectService.getVuzy();
        metadata = dataObjectService.getMetadata();
        vuzListKz = initVuzList("kz");
        vuzListRu = initVuzList("ru");
    }

    public int getIndexByVuzName(String vuzname, long chatId, String lang){
        int index = 0;
        String[] vuzFullnames = new String[vuzy.length];
        if(lang.equals("ru")){
            for (int i = 0; i < vuzy.length; i++) {
                vuzFullnames[i] = vuzy[i].name2;
            }
        } else if(lang.equals("kz")){
            for (int i = 0; i < vuzy.length; i++) {
                vuzFullnames[i] = vuzy[i].name1;
            }
        }
        for (int i = 0; i < vuzy.length; i++) {
            if(vuzFullnames[i].contains(vuzname)){
                index = i;
                break;
            }
        }
        return index;
    }

    public String oneVuzInfo(int index, long chatId){
        if(localeService.getLocaleTag(chatId).equals("kz")){
            return oneVuzInfoKz(index);
        } else {
            return oneVuzInfoRu(index);
        }
    }

    private String oneVuzInfoRu(int index) {
        StringBuilder message = new StringBuilder();
        message.append("<b>").append(metadata.fields.name2.labelRu).append("</b>:\n"); //name rus
        message.append(vuzy[index].name2).append("\n\n");
        message.append(metadata.fields.name5.labelRu).append(":\n"); //direction
        message.append(vuzy[index].name5).append("\n\n");
        message.append(metadata.fields.name3.labelRu).append(" :male_office_worker: :\n"); //fio ruk
        message.append(vuzy[index].name3).append("\n\n");
        message.append(metadata.fields.name8.labelRu).append(" :busts_in_silhouette: :\n"); //number
        message.append(vuzy[index].name8).append("\n\n");
        message.append(metadata.fields.name15.labelRu).append(" :globe_with_meridians: :\n"); //web-site
        message.append(vuzy[index].name15).append("\n\n");
        message.append(metadata.fields.name17.labelRu).append(" :office: :\n"); //address
        message.append(vuzy[index].name10).append(", ").append(vuzy[index].name17).append("\n\n");
        message.append(metadata.fields.name11.labelRu).append(" :telephone_receiver::\n"); //phone
        message.append(vuzy[index].name11).append("\n\n");
        message.append(metadata.fields.name12.labelRu).append(" :e-mail: :\n"); //email
        message.append(vuzy[index].name12).append("\n\n");
        message.append(metadata.fields.name14.labelRu).append(" :hourglass_flowing_sand: :\n"); //working mode
        message.append(vuzy[index].name14).append("\n\n");
        return EmojiParser.parseToUnicode(message.toString());
    }

    private String oneVuzInfoKz(int index) {
        StringBuilder message = new StringBuilder();
        message.append("<b>").append(metadata.fields.name1.labelKk).append("</b>:\n"); //name rus
        message.append(vuzy[index].name1).append("\n\n");
        message.append(metadata.fields.name4.labelKk).append(":\n"); //direction
        message.append(vuzy[index].name4).append("\n\n");
        message.append(metadata.fields.name3.labelKk).append(" :male_office_worker: :\n"); //fio ruk
        message.append(vuzy[index].name3).append("\n\n");
        message.append(metadata.fields.name8.labelKk).append(" :busts_in_silhouette: :\n"); //number
        message.append(vuzy[index].name8).append("\n\n");
        message.append(metadata.fields.name15.labelKk).append(":globe_with_meridians::\n"); //web-site
        message.append(vuzy[index].name15).append("\n\n");
        message.append(metadata.fields.name16.labelKk).append(":office::\n"); //address
        message.append(vuzy[index].name9).append(", ").append(vuzy[index].name16).append("\n\n");
        message.append(metadata.fields.name11.labelKk).append(" :telephone_receiver::\n"); //phone
        message.append(vuzy[index].name11).append("\n\n");
        message.append(metadata.fields.name12.labelKk).append(":e-mail::\n"); //email
        message.append(vuzy[index].name12).append("\n\n");
        message.append(metadata.fields.name13.labelKk).append(":hourglass_flowing_sand::\n"); //working mode
        message.append(vuzy[index].name13).append("\n\n");
        return EmojiParser.parseToUnicode(message.toString());
    }

    private List<String> initVuzList(String lang){
        List<String> list = new ArrayList<>();
        if(lang.equals("kz")){
            for (Vuz v:vuzy) {
                list.add(splitterService.splitFullname(v.name1));
            }
        } else {
            for (Vuz v:vuzy) {
                list.add(splitterService.splitFullname(v.name2));
            }
        }
        return list;
    }

    public List<String> getVuzList(String lang){
        if(lang.equals("kz")){
            return vuzListKz;
        }
        return vuzListRu;
    }

    public String getSiteUrl(int index){
        return vuzy[index].name15;
    }

    public String getAllParamsInfo(long chatId){
        return getCustomParamsInfo(chatId, dataObjectService.getAllSelected());
    }

    public String getCustomParamsInfo(long chatId, boolean[] selectedButtons){
        boolean[] flags;
        Map<Integer, String> meta = new HashMap<>();
        if(localeService.getLocaleTag(chatId).equals("ru")){
            flags = dataObjectService.getFlagsRu();
            meta.putAll(dataObjectService.getMetaRu());
        } else{
            flags = dataObjectService.getFlagsKz();
            meta.putAll(dataObjectService.getMetaKz());
        }
        int x = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < flags.length; i++) {
            Field field;
            if(flags[i]==true){
                if(selectedButtons[x]==true){
                    if(x!=5 && x!=6){
                        field = dataObjectService.getVuzFields()[x];
                        stringBuilder.append("<b>").append(meta.get(x+1)).append("</b>\n");
                        for (int j = 0; j < vuzy.length; j++) {
                            String s = null;
                            try {
                                s = field.get(vuzy[j]).toString();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            stringBuilder.append((j+1)+" - "+s+"\n");
                        }
                        stringBuilder.append("\n");
                    }
                }
                x++;
            }
        }
        return stringBuilder.toString();
    }
}
