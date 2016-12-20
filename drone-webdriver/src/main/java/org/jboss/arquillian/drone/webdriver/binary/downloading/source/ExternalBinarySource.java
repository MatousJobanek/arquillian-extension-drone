package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public interface ExternalBinarySource {

    ExternalBinary getLatestRelease() throws Exception;

    ExternalBinary getReleaseForVersion(String version) throws Exception;



}
