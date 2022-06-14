package kz.asembina.pvl_vuzy_bot.service.memory;

import kz.asembina.pvl_vuzy_bot.model.Step;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StepServiceImpl implements StepService {
    Map<Long, Deque<Step>> stepMap;
    Deque<Step> defaultStack;

    public StepServiceImpl() {
        stepMap = new HashMap<>();
        defaultStack = new ArrayDeque<Step>();
        defaultStack.push(Step.START);
    }

    @Override
    public void addUser(long chatId) {
        Deque<Step> defaultStack = new ArrayDeque<Step>();
        defaultStack.push(Step.START);
        stepMap.put(chatId, defaultStack);
    }

    @Override
    public void addStep(long chatId, Step step) {
        if(!stepMap.containsKey(chatId)){
            addUser(chatId);
        }
        Step current = stepMap.get(chatId).peek();
        if(!current.equals(step)){
            stepMap.get(chatId).push(step);
        }
    }

    @Override
    public Step deleteLastStep(long chatId) {
        if (stepMap.get(chatId).size()!=0){
            Step current = getLastStep(chatId);
            if(current!=null || !current.equals(Step.START)){
                stepMap.get(chatId).pop();
            }
            return current;
        }
        addUser(chatId);
        return Step.START;
    }

    @Override
    public Step getLastStep(long chatId) {
        return stepMap.get(chatId).peek();
    }

    @Override
    public boolean containsUser(long chatId) {
        return stepMap.containsKey(chatId);
    }

    @Override
    public Deque<Step> getStack(long chatId){
        if(containsUser(chatId)){
            return stepMap.get(chatId);
        }
        return defaultStack;
    }

    public Map getMap(){
        return stepMap;
    }
}
