package org.arquillian.drone.saucelabs.extension.webdriver;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utils class to make UI tests easier
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class WebDriverUtil {

    protected static void checkElementIsPresent(final WebDriver driver, final By by, final String errorMsg) {
        new WebDriverWaitWithMessage(driver, 10).failWith(errorMsg).until(new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(WebDriver webDriver) {
                try {
                    return driver.findElement(by) != null;
                } catch (NoSuchElementException ignored) {
                    return false;
                } catch (StaleElementReferenceException ignored) {
                    return false;
                }
            }
        });
    }

    // check if element is presence on page, fails otherwise
    protected static void checkElementContent(final WebDriver driver, final By by, final String expectedContent,
        final String errorMsg) {
        new WebDriverWaitWithMessage(driver, 10).failWith(errorMsg).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                try {
                    String text = driver.findElement(by).getText();
                    if (text != null) {
                        return text.contains(expectedContent);
                    }
                    return false;
                } catch (NoSuchElementException ignored) {
                    return false;
                } catch (StaleElementReferenceException ignored) {
                    return false;
                }
            }
        });
    }

    protected static class WebDriverWaitWithMessage extends WebDriverWait {

        private String message;

        public WebDriverWaitWithMessage(WebDriver driver, long timeOutInSeconds) {
            super(driver, timeOutInSeconds);
        }

        public WebDriverWait failWith(String message) {
            if (message == null || message.length() == 0) {
                throw new IllegalArgumentException("Error message must not be null nor empty");
            }
            this.message = message;
            return this;
        }

        @Override
        public <V> V until(Function<? super WebDriver, V> isTrue) {
            if (message == null) {
                return super.until(isTrue);
            } else {
                try {
                    return super.until(isTrue);
                } catch (TimeoutException e) {
                    throw new TimeoutException(message, e);
                }
            }
        }
    }
}
