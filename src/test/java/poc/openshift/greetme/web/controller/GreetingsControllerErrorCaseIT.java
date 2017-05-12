package poc.openshift.greetme.web.controller;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class GreetingsControllerErrorCaseIT extends AbstractWebDriverTestCase {

    @Test
    public void error_is_shown_when_server_is_unreachable() throws Exception {
        // when
        driver.get("http://localhost:8080");

        // then
        List<WebElement> errors = driver.findElements(By.xpath("//*[@id='errors']/li"));
        assertThat(errors.get(0).getText()).startsWith("ErrorObject(errorMessage=Could not communicate with GreetMe server");
    }
}