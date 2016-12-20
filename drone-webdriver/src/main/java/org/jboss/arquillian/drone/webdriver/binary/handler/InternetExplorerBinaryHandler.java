package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;

import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.StorageSource;
import org.jboss.arquillian.drone.webdriver.factory.BrowserCapabilitiesList;
import org.jboss.arquillian.phantom.resolver.maven.PlatformUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class InternetExplorerBinaryHandler extends AbstractBinaryHandler {

    public static final String IE_SYSTEM_DRIVER_BINARY_PROPERTY = "webdriver.ie.driver";
    private static final String IE_DRIVER_BINARY_PROPERTY = "ieDriverBinary";
    private static final String IE_DRIVER_VERSION_PROPERTY = "ieDriverVersion";
    private static final String IE_DRIVER_URL_PROPERTY = "ieDriverUrl";

    private DesiredCapabilities capabilities;

    public InternetExplorerBinaryHandler(DesiredCapabilities capabilities){
        this.capabilities = capabilities;
    }

    @Override
    protected String getArquillianCacheSubdirectory() {
        return new BrowserCapabilitiesList.InternetExplorer().getReadableName();
    }

    @Override
    protected String getDesiredVersionProperty() {
        return IE_DRIVER_VERSION_PROPERTY;
    }

    @Override
    protected String getUrlToDownloadProperty() {
        return IE_DRIVER_URL_PROPERTY;
    }

    @Override
    protected ExternalBinarySource getExternalBinarySource() {
        return new IeStorageSource((String) capabilities.getCapability(IE_DRIVER_VERSION_PROPERTY));
    }

    @Override protected DesiredCapabilities getCapabilities() {
        return capabilities;
    }

    @Override protected String getBinaryProperty() {
        return IE_DRIVER_BINARY_PROPERTY;
    }

    @Override
    public String getSystemBinaryProperty() {
        return IE_SYSTEM_DRIVER_BINARY_PROPERTY;
    }

    public File downloadAndPrepare() throws Exception {
        return super.downloadAndPrepare();
    }


    private class IeStorageSource extends StorageSource {

        private String version;

        IeStorageSource(String version) {
            super("http://selenium-release.storage.googleapis.com/");
            this.version = version;
        }

        @Override
        protected String getExpectedKeyRegex(String requiredVersion, String directory) {
            StringBuffer regexBuffer = new StringBuffer("%s/IEDriverServer_");
            if (PlatformUtils.is32()) {
                regexBuffer.append("Win32");
            } else {
                regexBuffer.append("x64");
            }
            regexBuffer.append("_%s.zip");

            String regex;
            if (version == null) {
                regex = String.format(regexBuffer.toString(), directory, directory + ".*");
            } else {
                regex = String.format(regexBuffer.toString(), version.substring(0, version.lastIndexOf(".")), version);
            }
            return regex;
        }
    }
}
