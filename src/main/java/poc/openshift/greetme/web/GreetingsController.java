package poc.openshift.greetme.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Controller
@RequestMapping("/")
public class GreetingsController {

    private RestTemplate greetMeServer;

    public GreetingsController(@Value("${greetme.server.baseurl}") String greetMeServerBaseUrl) {
        this.greetMeServer = new RestTemplateBuilder().rootUri(greetMeServerBaseUrl).build();
    }

    @PostMapping
    public String addGreeting(@ModelAttribute Person person, Model model) {
        greetMeServer.postForObject("/greetings", person, Greeting.class);
        model.addAttribute("greetings", getGreetingsFromServer());
        return "greetings";
    }

    @GetMapping
    public String getGreetings(Model model) {
        model.addAttribute("person", new Person(""));
        model.addAttribute("greetings", getGreetingsFromServer());
        return "greetings";
    }

    private Collection<Greeting> getGreetingsFromServer() {
        @SuppressWarnings("unchecked")
        Collection greetings = greetMeServer.getForObject("/greetings", Collection.class);
        return greetings;
    }
}