package poc.openshift.greetme.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GreetMeWebConfig {

    @Bean
    public RestTemplate greetMeServer(@Value("${greetme.server.baseurl}") String greetMeServerBaseUrl) {
        return new RestTemplateBuilder().rootUri(greetMeServerBaseUrl).build();
    }
}
