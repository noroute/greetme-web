package poc.openshift.greetme.web.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import poc.openshift.greetme.web.controller.Greeting;
import poc.openshift.greetme.web.controller.Person;

import java.util.Collection;

@Component
@Slf4j
public class GreetMeServerClient {

    private RestTemplate serverTemplate;
    private String greetingsResourceUrl;

    @Autowired
    public GreetMeServerClient(@Value("${greetme.server.baseurl}") String greetMeServerBaseUrl,
                               @Value("${greetme.server.greetings.resource.url}") String greetingsResourceUrl) {
        this.serverTemplate = new RestTemplateBuilder().rootUri(greetMeServerBaseUrl).build();
        this.greetingsResourceUrl = greetingsResourceUrl;
    }

    public Greeting postPersonToGreet(Person person) {
        log.info("Requesting greeting for {}", person);
        Greeting greeting = serverTemplate.postForObject(greetingsResourceUrl, person, Greeting.class);
        log.info("Received {}", greeting);
        return greeting;
    }

    public Collection<Greeting> getGreetings() {
        log.info("Requesting all greetings");
        Collection<Greeting> greetings = serverTemplate.exchange(greetingsResourceUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Collection<Greeting>>() {
        }).getBody();
        log.info("Received all greetings: {}", greetings);
        return greetings;
    }
}