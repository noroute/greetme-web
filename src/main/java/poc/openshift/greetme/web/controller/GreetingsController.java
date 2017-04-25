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
import java.util.Locale;

@Controller
@RequestMapping("/")
public class GreetingsController {

    private RestTemplate greetMeServer;

    @Autowired
    public GreetingsController(RestTemplate greetMeServer) {
        this.greetMeServer = greetMeServer;
    }

    @PostMapping
    public String addGreeting(@ModelAttribute Person person, Model model) {
        greetMeServer.postForObject("/greetings", person, Greeting.class);
        model.addAttribute("greetings", getGreetingsFromServer());
        return "greetings";
    }

    @GetMapping
    public String getGreetings(Model model) {
        model.addAttribute("person", new Person("", Locale.ENGLISH.getLanguage()));
        model.addAttribute("greetings", getGreetingsFromServer());
        return "greetings";
    }

    private Collection<Greeting> getGreetingsFromServer() {
        @SuppressWarnings("unchecked")
        Collection<Greeting> greetings = greetMeServer.getForObject("/greetings", Collection.class);
        return greetings;
    }
}