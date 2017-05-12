package poc.openshift.greetme.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

@Slf4j
public abstract class AbstractWebDriverTestCase {

    protected static WebDriver driver = null;

    @BeforeClass
    public static void createWebDriver() {
        try {
            driver = new FirefoxDriver();
        }
        catch (Exception e) {
            final String newLine = System.getProperty("line.separator");
            log.error(newLine
                    + newLine
                    + "====================================================" + newLine
                    + "WebDriver could not start Firefox. Please make sure:" + newLine
                    + newLine
                    + "1. you have Firefox 52+ installed" + newLine
                    + "-- If Firefox is not installed in the default directory, set the path to its binary with '-Dwebdriver.firefox.bin=/path/to/firefox-binary'" + newLine
                    + newLine
                    + "2. you have the needed 'geckodriver' installed; you can download it from: https://github.com/mozilla/geckodriver/releases" + newLine
                    + newLine
                    + "3. the 'geckodriver' can be found via your system's PATH, otherwise set the path to it with '-Dwebdriver.gecko.driver=/path/to/geckodriver'" + newLine
                    + "====================================================" + newLine
                    + newLine
                    + "Here is the problem that caused the error:", e);
            throw e;
        }
    }

    @AfterClass
    public static void quitWebDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
