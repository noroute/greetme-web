package poc.openshift.greetme.web.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorObject<T> {

    private String errorMessage;
    private T errorDetails; // T is of type String or List<String>
    private String errorId;

    public ErrorObject(RestClientException e) {
        errorMessage = "Could not communicate with GreetMe server";
        errorDetails = (T) e.getMessage();
        errorId = UUID.randomUUID().toString();
    }
}