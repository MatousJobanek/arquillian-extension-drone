package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.net.URI;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class GitHubSource implements ExternalBinarySource {

    private String projectUrl;

    // URL suffixes
    private String latestUrl = "/releases/latest";
    private String assetsUrl = "/releases/%s/assets";
    private String releasesUrl = "/releases";

    // JSON keys
    private String tagNameKey = "tag_name";
    private String idKey = "id";
    private String assetNameKey = "name";
    private String browserDownloadUrlKey = "browser_download_url";

    private ExternalBinary binaryRelease;

    public GitHubSource(String organization, String project) {
        projectUrl = String.format("https://api.github.com/repos/%s/%s", organization, project);
    }

    public ExternalBinary getLatestRelease() throws Exception {

        JsonObject latestRelease = sentGetRequest(projectUrl + latestUrl).getAsJsonObject();

        String tagName = latestRelease.get(tagNameKey).getAsString();
        String id = latestRelease.get(idKey).getAsString();
        binaryRelease = new ExternalBinary(tagName);
        setAssets(id);

        return binaryRelease;
    }

    public ExternalBinary getReleaseForVersion(String version) throws Exception {
        JsonArray releases = sentGetRequest(projectUrl + releasesUrl).getAsJsonArray();
        Iterator<JsonElement> iterator = releases.iterator();

        while (iterator.hasNext()) {
            JsonObject releaseObject = iterator.next().getAsJsonObject();
            String releaseTagName = releaseObject.get(tagNameKey).getAsString();

            if (version.equals(releaseTagName)) {
                binaryRelease = new ExternalBinary(releaseTagName);
                String id = releaseObject.get(idKey).getAsString();
                setAssets(id);
                return binaryRelease;
            }
        }
        return null;
    }

    protected abstract String getExpectedFileNameRegex();

    private void setAssets(String releaseId) throws Exception {
        JsonArray assets = sentGetRequest(String.format(projectUrl + assetsUrl, releaseId)).getAsJsonArray();

        Iterator<JsonElement> iterator = assets.iterator();
        while (iterator.hasNext()) {
            JsonObject asset = iterator.next().getAsJsonObject();
            String name = asset.get(assetNameKey).getAsString();

            if (name.matches(getExpectedFileNameRegex())) {
                String browserDownloadUrl = asset.get(browserDownloadUrlKey).getAsString();
                binaryRelease.setUrl(browserDownloadUrl);
                break;
            }
        }
    }

    private JsonElement sentGetRequest(String url) throws Exception {

        JsonElement result = sentGetRequestWithPagination(url, 1, JsonElement.class);

        if (result.isJsonArray()) {
            JsonArray resultArray = result.getAsJsonArray();
            int i = 2;
            while (true) {
                JsonArray page = sentGetRequestWithPagination(url, i, JsonArray.class);
                if (page.size() == 0) {
                    break;
                }
                resultArray.addAll(page);
                i++;
            }
            return resultArray;
        }
        return result;

    }

    private <T> T sentGetRequestWithPagination(String url, int pageNumber, Class<T> expectedType) throws Exception {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        URI uri = new URIBuilder(url).setParameter("page", String.valueOf(pageNumber)).build();
        HttpGet request = new HttpGet(uri);
        HttpResponse result = client.execute(request);
        String json = EntityUtils.toString(result.getEntity(), "UTF-8");
        Gson gson = new Gson();
        return gson.fromJson(json, expectedType);
    }

    public ExternalBinary getBinaryRelease() {
        return binaryRelease;
    }

}
