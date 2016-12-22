package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.drone.webdriver.binary.BinaryFilesUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.Downloader;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.binary.downloading.source.ExternalBinarySource;
import org.jboss.arquillian.drone.webdriver.utils.Constants;
import org.jboss.arquillian.drone.webdriver.utils.PropertySecurityAction;
import org.jboss.arquillian.drone.webdriver.utils.Validate;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class AbstractBinaryHandler implements BinaryHandler {

    private Logger log = Logger.getLogger(this.getClass().toString());

    public static final String DOWNLOAD_BINARIES_PROPERTY = "downloadBinaries";

    public String checkAndSetBinary(boolean performExecutableValidations) {
        String driverBinary = PropertySecurityAction.getProperty(getSystemBinaryProperty());

        if (Validate.empty(driverBinary)) {
            driverBinary = PropertySecurityAction.getProperty(getBinaryProperty());
        }

        if (Validate.empty(driverBinary) && !Validate.empty(getBinaryProperty())) {
            driverBinary = (String) getCapabilities().getCapability(getBinaryProperty());
        }

        if (Validate.empty(driverBinary)) {
            String downloadBinaries = (String) getCapabilities().getCapability(DOWNLOAD_BINARIES_PROPERTY);
            if (Validate.empty(downloadBinaries)
                || (!downloadBinaries.toLowerCase().trim().equals("false")
                && !downloadBinaries.toLowerCase().trim().equals("no"))) {

                try {
                    driverBinary = downloadAndPrepare().toString();
                } catch (Exception e) {
                    throw new IllegalStateException(
                        "Something bad happened when Drone was trying to download and prepare a binary. "
                            + "For more information see the stacktrace.", e);
                }
            }
        }
        setBinaryAsSystemProperty(performExecutableValidations, driverBinary);
        return driverBinary;
    }

    protected void setBinaryAsSystemProperty(boolean performExecutableValidations, String driverBinary) {
        if (Validate.nonEmpty(driverBinary) && Validate.nonEmpty(getSystemBinaryProperty())) {
            if (performExecutableValidations) {
                Validate.isExecutable(driverBinary,
                                      "The binary must point to an executable file, " + driverBinary);
            }
            PropertySecurityAction.setProperty(getSystemBinaryProperty(), driverBinary);
        }
    }

    public File downloadAndPrepare() throws Exception {
        String url = null;
        if (!Validate.empty(getUrlToDownloadProperty())) {
            url = (String) getCapabilities().getCapability(getUrlToDownloadProperty());
        }

        String desiredVersion = null;
        if (!Validate.empty(getDesiredVersionProperty())) {
            desiredVersion = (String) getCapabilities().getCapability(getDesiredVersionProperty());
        }

        if (Validate.nonEmpty(url)) {
            if (Validate.empty(desiredVersion)) {
                return downloadAndPrepare(null, url);
            } else {
                return downloadAndPrepare(createAndGetCacheDirectory(desiredVersion), url);
            }
        }

        if (getExternalBinarySource() == null) {
            return null;
        }
        ExternalBinary release = null;
        if (Validate.nonEmpty(desiredVersion)) {
            release = getExternalBinarySource().getReleaseForVersion(desiredVersion);
        } else {
            release = getExternalBinarySource().getLatestRelease();
        }

        return downloadAndPrepare(createAndGetCacheDirectory(release.getVersion()), release.getUrl());
    }

    protected File downloadAndPrepare(File targetDir, String from) throws Exception {
        return downloadAndPrepare(targetDir, new URL(from));
    }

    protected File downloadAndPrepare(File targetDir, URL from) throws Exception {
        File downloaded = Downloader.download(targetDir, from);
        File extraction = BinaryFilesUtils.extract(downloaded);
        File[] files = extraction.listFiles(file -> file.isFile());
        if (files.length == 0) {
            throw new IllegalStateException(
                "The number of extracted files in the directory " + extraction + " is 0. There is no file to use");
        }
        File binaryFile = files[0];
        log.info("marking binary file: " + binaryFile.getPath() + " as executable");
        try {
            binaryFile.setExecutable(true);
        } catch (SecurityException se) {
            log.severe("The downloaded binary: " + binaryFile
                           + " could not be set as executable. This may cause additional problems.");
        }
        return binaryFile;
    }

    private File createAndGetCacheDirectory(String subdirectory) {
        String dirPath = Constants.ARQUILLIAN_DRONE_CACHE_DIRECTORY
            + getArquillianCacheSubdirectory()
            + (subdirectory == null ? "" : File.separator + subdirectory);
        File dir = new File(dirPath);
        dir.mkdirs();
        return dir;
    }

    protected abstract String getBinaryProperty();

    public abstract String getSystemBinaryProperty();

    protected abstract String getArquillianCacheSubdirectory();

    protected abstract String getDesiredVersionProperty();

    protected abstract String getUrlToDownloadProperty();

    protected abstract ExternalBinarySource getExternalBinarySource();

    protected abstract DesiredCapabilities getCapabilities();

}
