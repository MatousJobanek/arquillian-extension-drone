package org.jboss.arquillian.drone.webdriver.factory;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeDriver;

public class EdgeDriverFactory extends AbstractWebDriverFactory<EdgeDriver> implements
        Configurator<EdgeDriver, WebDriverConfiguration>, Instantiator<EdgeDriver, WebDriverConfiguration>,
        Destructor<EdgeDriver> {

    private static final String BROWSER_CAPABILITIES = new BrowserCapabilitiesList.Edge().getReadableName();

    private static final String EDGE_DRIVER_BINARY_KEY = "webdriver.edge.driver";

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void destroyInstance(EdgeDriver instance) {
        instance.quit();
    }

    @Override
    public EdgeDriver createInstance(WebDriverConfiguration configuration) {

        String edgeDriverBinary = configuration.getEdgeDriverBinary();

        if (Validate.empty(edgeDriverBinary)) {
            edgeDriverBinary = SecurityActions.getProperty(EDGE_DRIVER_BINARY_KEY);
        }

        if (Validate.nonEmpty(edgeDriverBinary)) {
            Validate.isExecutable(edgeDriverBinary, "Edge driver binary must point to an executable file, " + edgeDriverBinary);
            SecurityActions.setProperty(EDGE_DRIVER_BINARY_KEY, edgeDriverBinary);
        }

        return SecurityActions.newInstance(configuration.getImplementationClass(), new Class<?>[] { Capabilities.class },
                new Object[] { getCapabilities(configuration, true) }, EdgeDriver.class);
    }

    @Override
    protected String getDriverReadableName() {
        return BROWSER_CAPABILITIES;
    }

    public Capabilities getCapabilities(WebDriverConfiguration configuration, boolean performValidations) {
        return configuration.getCapabilities();
    }
}
