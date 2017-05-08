package poc.openshift.greetme.web.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import poc.openshift.greetme.web.controller.Greeting;
import poc.openshift.greetme.web.controller.Person;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

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
        Supplier<Object> postPersonToGreetCall = () -> serverTemplate.postForObject(greetingsResourceUrl, person, Greeting.class);
        return callGreetMeServerWithExceptionHandling(postPersonToGreetCall, "greeting for person: " + person);
    }

    public Object getGreetings() {
        Supplier<Object> getGreetingsCall = () -> serverTemplate.exchange(greetingsResourceUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Collection<Greeting>>() {
        }).getBody();
        return callGreetMeServerWithExceptionHandling(getGreetingsCall, "all greetings");
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
        TypeReference<?> parameterizedErrorObject = getParameterizedErrorObject(hsce);
        try {
            return objectMapper.readValue(jsonErrorContent, parameterizedErrorObject);
        }
        catch (IOException ioe) {
            String message = "Could not deserialize JSON error content to ErrorObject: " + jsonErrorContent;
            RuntimeException re = new RuntimeException(message, ioe);
            log.error(message, re);
            throw re;
        }
    }

    private TypeReference<?> getParameterizedErrorObject(HttpStatusCodeException hsce) {
        if (hsce.getStatusCode().series().equals(HttpStatus.Series.CLIENT_ERROR)) {
            return new TypeReference<ErrorObject<List<String>>>() {
            };
        }

        return new TypeReference<ErrorObject<String>>() {
        };
    }
}