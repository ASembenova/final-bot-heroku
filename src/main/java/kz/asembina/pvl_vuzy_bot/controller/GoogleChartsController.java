package kz.asembina.pvl_vuzy_bot.controller;

import kz.asembina.pvl_vuzy_bot.service.db.GeneratedDocumentService;
import kz.asembina.pvl_vuzy_bot.service.db.LangService;
import kz.asembina.pvl_vuzy_bot.service.memory.UpdateCounterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GoogleChartsController {

    private final UpdateCounterService updateCounterService;
    private final GeneratedDocumentService generatedDocumentService;
    private final LangService langService;

    public GoogleChartsController(UpdateCounterService updateCounterService, GeneratedDocumentService generatedDocumentService, LangService langService) {
        this.updateCounterService = updateCounterService;
        this.generatedDocumentService = generatedDocumentService;
        this.langService = langService;
    }


    @GetMapping("/chart")
    public String getPieChart(Model model) {
        Map<String, Integer> graphData = new HashMap<>();
        Map<Long, Integer> map = updateCounterService.findAll();

        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            Long chatId = entry.getKey();
            int counter = entry.getValue();
            graphData.put(String.valueOf(chatId), counter);
        }

        model.addAttribute("chartData", graphData);
        return "chart";
    }

    @GetMapping("/doc")
    public String getDocs(Model model) {
        model.addAttribute("generatedDocuments", generatedDocumentService.findAll());
        return "docs";
    }

    @GetMapping("/lang")
    public String getLangs(Model model) {
        model.addAttribute("langs", langService.findAll());
        return "lang";
    }



}
