package poc.openshift.greetme.web;

import lombok.Value;

@Value
public class Greeting {

    private long id;
    private String message;
}
