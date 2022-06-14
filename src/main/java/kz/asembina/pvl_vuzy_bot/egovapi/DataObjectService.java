package kz.asembina.pvl_vuzy_bot.egovapi;

import kz.asembina.pvl_vuzy_bot.service.SplitterService;
import kz.asembina.pvl_vuzy_bot.service.memory.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataObjectService {
    private Vuz[] vuzy;
    private Metadata metadata;
    private Field[] vuzFields;
    private Method[] metaMethods;
    private boolean[] flagsRu;
    private boolean[] flagsKz;
    private boolean[] allSelected;
    private String[][] datasetPassport;
    private final LocaleService localeService;
    private final SplitterService splitterService;
    private Map<Integer, String> metaRu;
    private Map<Integer, String> metaKz;
    @Autowired
    public DataObjectService(EgovApiConnection egovApiConnection, LocaleService localeService, SplitterService splitterService){
        vuzy = egovApiConnection.createVuzObjects();
        datasetPassport = egovApiConnection.getPassport();
        metadata = egovApiConnection.createMetadataObject();
        initFlags();
        this.localeService = localeService;
        this.splitterService = splitterService;
    }

    private void initFlags(){
        metaRu = new TreeMap<>();
        metaKz = new TreeMap<>();
        vuzFields = vuzy[0].getClass().getDeclaredFields();
        metaMethods = getMetadata().getClass().getDeclaredMethods();
        flagsRu = new boolean[metaMethods.length];
        flagsKz = new boolean[metaMethods.length];
        allSelected = new boolean[metaMethods.length];
        try {
            for (int i = 0; i < metaMethods.length; i++) {
                allSelected[i] = true;
                String s = metaMethods[i].getName();
                Pattern p = Pattern.compile("(\\D+)(\\d{1,2})(\\D+)"); // example: getName1Kk
                Matcher m = p.matcher(s);
                while (m.find()){
                    int num = Integer.parseInt(m.group(2));
                    if(m.group(3).equals("Kk")){
                        metaKz.put(num, metaMethods[i].invoke(metadata).toString()); //metaKz: электрондық мекенжайы
                        //metaMethods[i]=getName1Kk()
                        flagsKz[i]=true;
                    } else if (m.group(3).equals("Ru")){
                        metaRu.put(num, metaMethods[i].invoke(metadata).toString());
                        flagsRu[i]=true;
                    } else{
                        flagsKz[i]=true;
                        flagsRu[i]=true;
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Vuz[] getVuzy() {
        return vuzy;
    }

    public Metadata getMetadata(){
        return metadata;
    }

    public String[][] getDatasetPassport() { return datasetPassport; }

    public Map<Integer, String> getMetaRu() {
        return metaRu;
    }

    public Map<Integer, String> getMetaKz() {
        return metaKz;
    }

    public Field[] getVuzFields() {
        return vuzFields;
    }

    public Method[] getMetaMethods() {
        return metaMethods;
    }

    public boolean[] getFlagsRu() {
        return flagsRu;
    }

    public boolean[] getFlagsKz() {
        return flagsKz;
    }

    public boolean[] getAllSelected() {
        return allSelected;
    }
}


