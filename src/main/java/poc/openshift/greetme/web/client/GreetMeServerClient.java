package poc.openshift.greetme.web.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import poc.openshift.greetme.web.controller.Greeting;
import poc.openshift.greetme.web.controller.Person;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
public class GreetMeServerClient {

    private RestTemplate serverTemplate;
    private String greetingsResourceUrl;
    private ObjectMapper objectMapper;

    @Autowired
    public GreetMeServerClient(@Value("${greetme.server.baseurl}") String greetMeServerBaseUrl,
                               @Value("${greetme.server.greetings.resource.url}") String greetingsResourceUrl,
                               ObjectMapper objectMapper) {

        this.serverTemplate = new RestTemplateBuilder().rootUri(greetMeServerBaseUrl).build();
        this.greetingsResourceUrl = greetingsResourceUrl;
        this.objectMapper = objectMapper;
    }

    public Object postPersonToGreet(Person person) {
        Supplier<Object> postPersonToGreetCall = () -> serverTemplate.exchange(greetingsResourceUrl, HttpMethod.POST, asHttpEntity(person), Greeting.class).getBody();
        return callGreetMeServerWithExceptionHandling(postPersonToGreetCall, "greeting for person: " + person);
    }

    public Object getGreetings() {
        Supplier<Object> getGreetingsCall = () -> Arrays.asList(serverTemplate.getForObject(greetingsResourceUrl, Greeting[].class));
        return callGreetMeServerWithExceptionHandling(getGreetingsCall, "all greetings");
    }

    // RestTemplate.postForObject(..) will not set the Content-Type header to application/json when person == null
    // (i.e. when a POST request without a body happens). So we make sure the Content-Type header is always set.
    private HttpEntity<Person> asHttpEntity(Person person) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return new HttpEntity<>(person, headers);
    }

    private Object callGreetMeServerWithExceptionHandling(Supplier<?> greetMeServerCall, String callDescription) {
        log.info("Requesting {}", callDescription);
        Object result;
        try {
            result = greetMeServerCall.get();
            log.info("Received {}: {}", callDescription, result);
        }
        catch (HttpStatusCodeException e) {
            result = deserializeJsonErrorContent(e);
            log.error("Received error for requesting {}: {}", callDescription, result);
        }
        catch (RestClientException e) {
            result = new ErrorObject<String>(e);
            log.error("Received error for requesting {}: {}", callDescription, result);
        }
        return result;
    }

    private ErrorObject<?> deserializeJsonErrorContent(HttpStatusCodeException hsce) {
        String jsonErrorContent = hsce.getResponseBodyAsString();
        try {
            return objectMapper.readValue(jsonErrorContent, ErrorObject.class);
        }
        catch (IOException ioe) {
            String message = "Could not deserialize JSON error content to ErrorObject: " + jsonErrorContent;
            RuntimeException re = new RuntimeException(message, ioe);
            log.error(message, re);
            throw re;
        }
    }
}