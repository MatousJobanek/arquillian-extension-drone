package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public interface BinaryHandler {

    String checkAndSetBinary(boolean performExecutableValidations);

    File downloadAndPrepare() throws Exception;

    String getSystemBinaryProperty();
}
