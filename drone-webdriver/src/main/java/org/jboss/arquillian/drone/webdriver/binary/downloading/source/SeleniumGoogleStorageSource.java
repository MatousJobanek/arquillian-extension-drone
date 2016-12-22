package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class SeleniumGoogleStorageSource extends GoogleStorageSource {

    public static final String SELENIUM_BASE_STORAGE_URL = "http://selenium-release.storage.googleapis.com/";

    public SeleniumGoogleStorageSource() {
        super(SELENIUM_BASE_STORAGE_URL);
    }

    protected String getDirectoryFromFullVersion(String version) {
        if (version.contains("-")){
            int index = version.indexOf("-");
            String number = version.substring(0, index);
            return getShortNumber(number) + version.substring(index);
        }
        return getShortNumber(version);
    }

    private String getShortNumber(String fullNumber){
        return fullNumber.substring(0, fullNumber.lastIndexOf("."));
    }
}
