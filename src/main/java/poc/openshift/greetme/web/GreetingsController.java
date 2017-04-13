package poc.openshift.greetme.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Controller
@RequestMapping("/")
public class GreetingsController {

    @Value("${greetme.server.baseurl}")
    private String greetMeServerBaseUrl;

    private RestTemplate greetMeServer;

    @PostConstruct
    public void init() {
        this.greetMeServer = new RestTemplateBuilder().rootUri(greetMeServerBaseUrl).build();
    }

    @PostMapping
    public String addGreeting(@ModelAttribute Person person, Model model) {
        greetMeServer.postForObject("/greetings", person, Greeting.class);
        model.addAttribute("greetings", getGreetingsFromServer());
        return "greeting";
    }

    @GetMapping
    public String getGreetings(Model model) {
        model.addAttribute("person", new Person(""));
        model.addAttribute("greetings", getGreetingsFromServer());
        return "greeting";
    }

    private Collection<Greeting> getGreetingsFromServer() {
        @SuppressWarnings("unchecked")
        Collection greetings = greetMeServer.getForObject("/greetings", Collection.class);
        return greetings;
    }
}