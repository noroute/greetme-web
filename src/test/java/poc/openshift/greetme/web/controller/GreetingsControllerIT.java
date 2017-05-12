package poc.openshift.greetme.web.controller;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class GreetingsControllerIT extends AbstractWebDriverTestCase {

    @ClassRule
    public static final DockerComposeRule DOCKER_COMPOSE_RULE = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("greetme-server", HealthChecks.toRespondOverHttp(8080, dockerPort -> dockerPort.inFormat("http://$HOST:$EXTERNAL_PORT/greetings")))
            .saveLogsTo("target/docker-compose-rule-logs")
            .build();

    @Test
    public void alice_receives_a_greeting_when_she_requests_one() throws Exception {
        // given
        driver.get("http://localhost:8080");

        WebElement nameTextField = driver.findElement(By.id("name"));
        nameTextField.clear();
        nameTextField.sendKeys("Alice");

        // when
        nameTextField.submit();
        new WebDriverWait(driver, 2).until(ExpectedConditions.presenceOfElementLocated(By.id("greetings")));

        // then
        List<WebElement> greetings = driver.findElements(By.xpath("//*[@id='greetings']/li"));
        assertThat(greetings.get(0).getText()).isEqualTo("Hello, Alice!");
    }
}