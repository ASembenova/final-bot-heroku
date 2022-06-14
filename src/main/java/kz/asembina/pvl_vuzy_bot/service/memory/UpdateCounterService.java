package kz.asembina.pvl_vuzy_bot.service.memory;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateCounterService {

    private final Map<Long, Integer> counterMap;

    public UpdateCounterService() {
        counterMap = new HashMap<>();
    }

    public void save(long chatId){
        if(counterMap.containsKey(chatId)){
            int counter = counterMap.get(chatId);
            counterMap.put(chatId, counter+1);
        } else{
            counterMap.put(chatId, 1);
        }
    }

    public Map<Long, Integer> findAll(){
        return counterMap;
    }

}
