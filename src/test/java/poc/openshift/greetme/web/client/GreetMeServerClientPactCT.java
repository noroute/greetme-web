package poc.openshift.greetme.web.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import au.com.dius.pact.model.PactSpecVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Maps;
import org.junit.Rule;
import org.junit.Test;
import poc.openshift.greetme.web.controller.Greeting;
import poc.openshift.greetme.web.controller.Person;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class GreetMeServerClientPactCT {

    private static final int SOME_PORT = 8040;
    private static final String GREETINGS_RESOURCE_URL = "/greetings";

    private static final Map<String, String> CONTENT_TYPE_IS_APPLICATION_JSON_UTF_8_HEADER = Maps.newHashMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);

    private static final String SOME_ERROR_ID = UUID.randomUUID().toString();

    private GreetMeServerClient greetMeServerClient = new GreetMeServerClient("http://localhost:" + SOME_PORT, GREETINGS_RESOURCE_URL, new ObjectMapper());

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule(
            "greetme_server_provider",
            "localhost", SOME_PORT,
            PactSpecVersion.V3,
            this);

    @Pact(consumer = "greetme_web_consumer")
    public PactFragment serverCreatesOneGreeting(PactDslWithProvider builder) {
        DslPart personAsJson = new PactDslJsonBody()
                .stringValue("name", "Bob")
                .stringValue("nativeLanguageCode", "en");

        DslPart greetingAsJson = new PactDslJsonBody()
                .id("id", 1l)
                .stringValue("message", "Hello, Bob!");

        return builder
                .uponReceiving("POST /greetings creates greeting for person")
                .method("POST")
                .path(GREETINGS_RESOURCE_URL)
                .headers(Maps.newHashMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .body(personAsJson)
                .willRespondWith()
                .headers(CONTENT_TYPE_IS_APPLICATION_JSON_UTF_8_HEADER)
                .status(201)
                .body(greetingAsJson)
                .toFragment();
    }

    @Pact(consumer = "greetme_web_consumer")
    public PactFragment serverRespondsWithBadRequestWhenBodyIsEmpty(PactDslWithProvider builder) {
        DslPart errorObjectAsJson = new PactDslJsonBody()
                .stringValue("error_message", "Invalid JSON body")
                .stringMatcher("error_details", "Required request body is missing.*", "Required request body is missing")
                .uuid("error_id", SOME_ERROR_ID);

        return builder
                .uponReceiving("POST /greetings with empty body causes Bad Request (invalid JSON body)")
                .method("POST")
                .path(GREETINGS_RESOURCE_URL)
                .headers(Maps.newHashMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .body("")
                .willRespondWith()
                .headers(CONTENT_TYPE_IS_APPLICATION_JSON_UTF_8_HEADER)
                .status(400)
                .body(errorObjectAsJson)
                .toFragment();
    }

    @Pact(consumer = "greetme_web_consumer")
    public PactFragment serverRespondsWithBadRequestWhenValidationFailed(PactDslWithProvider builder) {
        DslPart personWithInvalidLanuageAsJson = new PactDslJsonBody()
                .stringValue("name", "Mallory")
                .stringValue("nativeLanguageCode", "invalidLanguageCode");

        DslPart errorObjectAsJson = new PactDslJsonBody()
                .stringValue("error_message", "Validation failed")
                .array("error_details")
                .stringValue("nativeLanguageCode must be an ISO 639 language code")
                .closeArray()
                .asBody()
                .uuid("error_id", SOME_ERROR_ID);

        return builder
                .uponReceiving("POST /greetings with invalid person data causes Bad Request (validation failed)")
                .method("POST")
                .path(GREETINGS_RESOURCE_URL)
                .headers(Maps.newHashMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .body(personWithInvalidLanuageAsJson)
                .willRespondWith()
                .headers(CONTENT_TYPE_IS_APPLICATION_JSON_UTF_8_HEADER)
                .status(400)
                .body(errorObjectAsJson)
                .toFragment();
    }

    @Pact(consumer = "greetme_web_consumer")
    public PactFragment serverRespondsWithAtLeastOneGreeting(PactDslWithProvider builder) {
        DslPart greetingsArrayAsJson = PactDslJsonArray.arrayMinLike(1)
                .id("id", 2l)
                .stringMatcher("message", ".*, .*!", "Hello, Alice!");

        return builder
                .given("at_least_one_greeting")
                .uponReceiving("GET /greetings gets array containing greeting(s)")
                .method("GET")
                .path(GREETINGS_RESOURCE_URL)
                .willRespondWith()
                .headers(CONTENT_TYPE_IS_APPLICATION_JSON_UTF_8_HEADER)
                .body(greetingsArrayAsJson)
                .toFragment();
    }

    @Test
    @PactVerification(fragment = "serverCreatesOneGreeting")
    public void client_returns_greeting_for_posted_person() throws Exception {
        // when
        Object response = greetMeServerClient.postPersonToGreet(new Person("Bob", "en"));

        // then
        Greeting expectedGreeting = new Greeting(1, "Hello, Bob!");
        assertThat(response).isEqualTo(expectedGreeting);
    }

    @Test
    @PactVerification(fragment = "serverRespondsWithBadRequestWhenBodyIsEmpty")
    public void client_returns_error_for_post_request_with_empty_body() throws Exception {
        // when
        Object response = greetMeServerClient.postPersonToGreet(null);

        // then
        ErrorObject<String> expectedErrorObject = new ErrorObject<>();
        expectedErrorObject.setErrorMessage("Invalid JSON body");
        expectedErrorObject.setErrorDetails("Required request body is missing");
        expectedErrorObject.setErrorId(SOME_ERROR_ID);

        assertThat(response).isEqualToComparingFieldByField(expectedErrorObject);
    }

    @Test
    @PactVerification(fragment = "serverRespondsWithBadRequestWhenValidationFailed")
    public void client_returns_error_for_post_request_with_invalid_person_data() throws Exception {
        // when
        Object response = greetMeServerClient.postPersonToGreet(new Person("Mallory", "invalidLanguageCode"));

        // then
        ErrorObject<List<String>> expectedErrorObject = new ErrorObject<>();
        expectedErrorObject.setErrorMessage("Validation failed");
        expectedErrorObject.setErrorDetails(Arrays.asList("nativeLanguageCode must be an ISO 639 language code"));
        expectedErrorObject.setErrorId(SOME_ERROR_ID);

        assertThat(response).isEqualToComparingFieldByField(expectedErrorObject);
    }

    @Test
    @PactVerification(fragment = "serverRespondsWithAtLeastOneGreeting")
    public void client_returns_all_greetings() throws Exception {
        // when
        Object response = greetMeServerClient.getGreetings();

        // then
        Greeting expectedGreeting = new Greeting(2, "Hello, Alice!");
        assertThat(response).isEqualTo(Arrays.asList(expectedGreeting));
    }
}