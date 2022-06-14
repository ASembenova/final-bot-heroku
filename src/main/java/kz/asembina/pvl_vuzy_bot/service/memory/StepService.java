package kz.asembina.pvl_vuzy_bot.service.memory;

import kz.asembina.pvl_vuzy_bot.model.Step;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

@Component
public interface StepService {
    void addUser(long chatId);
    void addStep(long chatId, Step step);
    Step deleteLastStep(long chatId);
    Step getLastStep(long chatId);
    boolean containsUser(long chatId);
    Deque<Step> getStack(long chatId);
}
