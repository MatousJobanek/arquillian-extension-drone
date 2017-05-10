package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.GitHubLastUpdateCache;
import org.jboss.arquillian.drone.webdriver.utils.HttpClient;
import org.jboss.arquillian.drone.webdriver.utils.PlatformUtils;

import static org.jboss.arquillian.drone.webdriver.binary.handler.PhantomJSDriverBinaryHandler.PHANTOMJS_BINARY_NAME;

/**
 * A slightly changed {@link GitHubSource} implementation handling PhantomJS binaries. The latest version is retrieved
 * from list of GH tags and download URL is constructed to use Bitbucket downloads storage.
 */
public class PhantomJSGitHubBitbucketSource extends GitHubSource {

    private static String TAGS_URL = "/tags";
    private static String TAG_NAME = "name";

    private static String BASE_DOWNLOAD_URL = "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-";

    private static String lastPhantomJSRelease = "2.1.1";

    public PhantomJSGitHubBitbucketSource(HttpClient httpClient, GitHubLastUpdateCache gitHubLastUpdateCache) {
        super("ariya", "phantomjs", httpClient, gitHubLastUpdateCache);
    }

    /**
     * As there was announced the end of the development of PhantomJS:
     * https://groups.google.com/forum/#!msg/phantomjs/9aI5d-LDuNE/5Z3SMZrqAQAJ
     * it is not necessary to check the latest release - it is expected that there won't be any newer than the last
     * one: 2.1.1
     * If the development of PhantomJS is resurrected, then the logic will be uncommented.
     */
    public ExternalBinary getLatestRelease() throws Exception {
        // See the javadoc of this method for explanation why this code is commented out

        //        final HttpClient.Response response =
        //            sentGetRequestWithPagination(getProjectUrl() + TAGS_URL, 1, lastModificationHeader());
        //        final ExternalBinary latestPhantomJSBinary;
        //
        //        if (response.hasPayload()) {
        //            JsonArray releaseTags = getGson().fromJson(response.getPayload(), JsonElement.class).getAsJsonArray();
        //            if (releaseTags.size() == 0) {
        //                return null;
        //            }
        //            String version = releaseTags.get(0).getAsJsonObject().get(TAG_NAME).getAsString();
        //            latestPhantomJSBinary = new ExternalBinary(version);
        //
        //            latestPhantomJSBinary.setUrl(getUrlForVersion(version));
        //            getCache().store(latestPhantomJSBinary, getUniqueKey(), extractModificationDate(response));
        //        } else {
        //            latestPhantomJSBinary = getCache().load(getUniqueKey(), ExternalBinary.class);
        //        }

        ExternalBinary lastPhantomJSVersion = new ExternalBinary(lastPhantomJSRelease);
        lastPhantomJSVersion.setUrl(getUrlForVersion(lastPhantomJSRelease));

        return lastPhantomJSVersion;
    }

    @Override
    public ExternalBinary getReleaseForVersion(String version) throws Exception {
        ExternalBinary phantomJSBinary = new ExternalBinary(version);
        phantomJSBinary.setUrl(getUrlForVersion(version));
        return phantomJSBinary;
    }

    private String getUrlForVersion(String version) {
        StringBuilder phantomJsUrl = new StringBuilder(BASE_DOWNLOAD_URL);
        phantomJsUrl.append(version).append("-");

        if (PlatformUtils.isMac()) {
            phantomJsUrl.append("macosx.zip").toString();
        } else if (PlatformUtils.isWindows()) {
            phantomJsUrl.append("windows.zip");
        } else {
            phantomJsUrl.append("linux-");
            if (PlatformUtils.is32()) {
                phantomJsUrl.append("i686.tar.bz2").toString();
            } else {
                phantomJsUrl.append("x86_64.tar.bz2").toString();
            }
        }
        return phantomJsUrl.toString();
    }

    @Override
    protected String getExpectedFileNameRegex(String version) {
        return PHANTOMJS_BINARY_NAME;
    }
}

