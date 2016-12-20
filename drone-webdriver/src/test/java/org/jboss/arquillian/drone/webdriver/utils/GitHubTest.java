package org.jboss.arquillian.drone.webdriver.utils;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
@RunWith(Arquillian.class)
public class GitHubTest {

    @BeforeClass
    public static void beforeClass() {
    }

//    @Drone WebDriver browser;

    @Test
    public void runTest() throws Exception {
//        browser.get("http://www.google.com");
        Boolean bla = Boolean.valueOf("true");
        System.out.println(bla);
    }
}
