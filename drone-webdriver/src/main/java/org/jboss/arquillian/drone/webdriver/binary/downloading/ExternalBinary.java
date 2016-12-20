package org.jboss.arquillian.drone.webdriver.binary.downloading;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class ExternalBinary {

    private String version;
    private String url;

    public ExternalBinary(String version, String url) {
        this.version = version;
        this.url = url;
    }

    public ExternalBinary(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
