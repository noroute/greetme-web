package poc.openshift.greetme.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import poc.openshift.greetme.web.client.GreetMeServerClient;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/")
public class GreetingsController {

    private static final List<Locale> ALL_LOCALES = Stream.of(Locale.getISOLanguages())
            .map(Locale::forLanguageTag)
            .sorted(Comparator.comparing(o -> o.getDisplayLanguage(Locale.ENGLISH)))
            .collect(Collectors.toList());

    private GreetMeServerClient greetMeServerClient;

    @Autowired
    public GreetingsController(GreetMeServerClient greetMeServerClient) {
        this.greetMeServerClient = greetMeServerClient;
    }

    @PostMapping
    public String addGreeting(@ModelAttribute Person person, Model model) {
        greetMeServerClient.postPersonToGreet(person);
        addGreetingsAndLocalesToModel(model);
        return "greetings";
    }

    private void addGreetingsAndLocalesToModel(Model model) {
        model.addAttribute("greetings", greetMeServerClient.getGreetings());
        model.addAttribute("locales", ALL_LOCALES);
    }

    @GetMapping
    public String getGreetings(Model model) {
        addGreetingsAndLocalesToModel(model);
        model.addAttribute("person", new Person("Bob", Locale.ENGLISH.getLanguage()));
        return "greetings";
    }
}