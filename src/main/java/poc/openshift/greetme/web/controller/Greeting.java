package poc.openshift.greetme.web.controller;

import lombok.Value;

@Value
public class Greeting {

    private long id;
    private String message;
}
