package poc.openshift.greetme.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poc.openshift.greetme.web.client.ErrorObject;
import poc.openshift.greetme.web.client.GreetMeServerClient;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/")
@SessionAttributes("greetings")
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

    @GetMapping
    public String getGreetings(Model model) {
        model.addAttribute("allLocales", ALL_LOCALES);

        Optional<List<Greeting>> greetings = requestGreetingsAndHandleErrors(model);
        if (greetings.isPresent()) {
            model.addAttribute("greetings", greetings.get());
        }

        if (!model.containsAttribute("person")) {
            model.addAttribute(new Person("Bob", Locale.ENGLISH.getLanguage()));
        }

        return "greetings";
    }

    private Optional<List<Greeting>> requestGreetingsAndHandleErrors(Model model) {
        Object response = greetMeServerClient.getGreetings();
        if (isErrorObject(response)) {
            ErrorObject<?> error = (ErrorObject<?>) response;
            if (!model.containsAttribute("errors")) {
                model.addAttribute("errors", Arrays.asList(error));
            }
            else {
                List<ErrorObject<?>> errorsFromPostRequest = (List<ErrorObject<?>>) model.asMap().get("errors");
                List<ErrorObject<?>> errors = new ArrayList<>(errorsFromPostRequest);
                errors.add(error);
                model.addAttribute("errors", errors);
            }
            return Optional.empty();
        }
        return Optional.of((List<Greeting>) response);
    }

    private boolean isErrorObject(Object response) {
        return (response instanceof ErrorObject<?>);
    }

    @PostMapping
    public String addGreeting(@ModelAttribute Person person, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(person);

        Object response = greetMeServerClient.postPersonToGreet(person);
        if (isErrorObject(response)) {
            redirectAttributes.addFlashAttribute("errors", Arrays.asList((ErrorObject<?>) response));
        }

        return "redirect:/";
    }
}