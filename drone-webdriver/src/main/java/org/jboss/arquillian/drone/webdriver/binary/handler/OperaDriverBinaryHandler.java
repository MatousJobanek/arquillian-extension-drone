package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class OperaDriverBinaryHandler extends AbstractBinaryHandler {

    private static final String OPERA_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.opera.driver";
    private static final String OPERA_DRIVER_BINARY_PROPERTY = "operaDriverBinary";
    private static final String OPERA_DRIVER_VERSION_PROPERTY = "operaDriverVersion";
    private static final String OPERA_DRIVER_URL_PROPERTY = "operaDriverUrl";

    private DesiredCapabilities capabilities;

    public OperaDriverBinaryHandler(DesiredCapabilities capabilities){
        this.capabilities = capabilities;
    }

    @Override
    protected String getBinaryProperty() {
        return OPERA_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return OPERA_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.Opera().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return OPERA_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return OPERA_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return null;
    }

    @Override
    protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }
}
