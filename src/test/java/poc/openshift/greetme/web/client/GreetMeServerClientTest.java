package poc.openshift.greetme.web.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class GreetMeServerClientTest {

    private GreetMeServerClient greetMeServerClient = new GreetMeServerClient("http://localhost:8080", "/greetings", new ObjectMapper());

    @Test
    public void returns_error_when_server_is_unreachable() throws Exception {
        // when
        Object response = greetMeServerClient.postPersonToGreet(null);

        // then
        assertThat(response).isInstanceOf(ErrorObject.class);

        // and
        @SuppressWarnings("unchecked")
        ErrorObject<String> errorObject = (ErrorObject<String>) response;
        assertThat(errorObject.getErrorMessage()).isEqualTo("Could not communicate with GreetMe server");
    }
}