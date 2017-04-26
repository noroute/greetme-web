package poc.openshift.greetme.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
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

    private RestTemplate greetMeServer;

    @Autowired
    public GreetingsController(RestTemplate greetMeServer) {
        this.greetMeServer = greetMeServer;
    }

    @PostMapping
    public String addGreeting(@ModelAttribute Person person, Model model) {
        greetMeServer.postForObject("/greetings", person, Greeting.class);
        addGreetingsAndLocalesToModel(model);
        return "greetings";
    }

    private void addGreetingsAndLocalesToModel(Model model) {
        model.addAttribute("greetings", getGreetingsFromServer());
        model.addAttribute("locales", ALL_LOCALES);
    }

    private Collection<Greeting> getGreetingsFromServer() {
        @SuppressWarnings("unchecked")
        Collection<Greeting> greetings = greetMeServer.getForObject("/greetings", Collection.class);
        return greetings;
    }

    @GetMapping
    public String getGreetings(Model model) {
        addGreetingsAndLocalesToModel(model);
        model.addAttribute("person", new Person("Bob", Locale.ENGLISH.getLanguage()));
        return "greetings";
    }
}