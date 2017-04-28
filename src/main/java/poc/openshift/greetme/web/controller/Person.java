package poc.openshift.greetme.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String nativeLanguageCode;
}
