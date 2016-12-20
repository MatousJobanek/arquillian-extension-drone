package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.jboss.arquillian.drone.webdriver.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.jboss.arquillian.drone.webdriver.utils.HttpUtils.sentGetRequest;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public abstract class StorageSource implements ExternalBinarySource {

    private Logger log = Logger.getLogger(StorageSource.class.toString());

    private String storageUrl;
    private String urlToLatestRelease;
    private ArrayList<Content> contents;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private String latestVersion;

    public StorageSource(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public StorageSource(String storageUrl, String urlToLatestRelease) {
        this.urlToLatestRelease = urlToLatestRelease;
        this.storageUrl = storageUrl;
    }

    @Override
    public ExternalBinary getLatestRelease() throws Exception {
        if (urlToLatestRelease != null) {
            latestVersion = StringUtils.trimMultiline(sentGetRequest(urlToLatestRelease));
        } else {
            retrieveContents();
        }
        return getReleaseForVersion(latestVersion);
    }

    private void retrieveContents() throws Exception {
        if (contents == null) {
            contents = new ArrayList<>();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(sentGetRequest(storageUrl)));
            Document doc = db.parse(is);
            NodeList contentNodes = ((Element) doc.getFirstChild()).getElementsByTagName("Contents");
            for (int i = 0; i < contentNodes.getLength(); i++) {
                Element item = (Element) contentNodes.item(i);
                Content content = new Content();
                String key = getContentOfFirstElement(item, "Key");
                if (key.contains("/")) {
                    content.setKey(key);
                    content.setLastModified(getContentOfFirstElement(item, "LastModified"));
                    content.setDirectory(key.substring(0, key.indexOf("/")));
                    contents.add(content);
                }
            }
        }
    }

    private Date parseDate(String date, String key) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            log.warning("Date " + date + " of content " + storageUrl + key
                            + " could not have been parsed. This content will be omitted. See the exception msg: "
                            + e.getMessage());
        }
        return null;
    }

    private String getContentOfFirstElement(Element element, String tagName) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (elementsByTagName.getLength() == 0) {
            return "";
        }
        return elementsByTagName.item(0).getTextContent();
    }

    @Override
    public ExternalBinary getReleaseForVersion(String requiredVersion) throws Exception {
        retrieveContents();
        List<Content> matched = contents
            .stream()
            .filter(content -> {
                return content.getKey().matches(getExpectedKeyRegex(requiredVersion, content.getDirectory()));
            })
            .collect(Collectors.toList());

        if (matched.size() == 0) {
            throw new IllegalStateException(
                "There wasn't found any binary with the key matching regex "
                    + getExpectedKeyRegex(requiredVersion, "directory") + " in the storage: " + storageUrl);
        }

        if (requiredVersion != null) {
            return new ExternalBinary(requiredVersion,  storageUrl + matched.get(0).getKey());
        } else {
            Content latestContent = findLatestContent(matched);
            return new ExternalBinary(latestContent.getDirectory(),  storageUrl + latestContent.getKey());

        }
    }

    private Content findLatestContent(List<Content> matched) {
        return matched.stream()
            .sorted((c1, c2) -> {
                Date c1Date = parseDate(c1.getLastModified(), c1.getKey());
                Date c2Date = parseDate(c2.getLastModified(), c2.getKey());
                if (c1Date == null) return -1;
                if (c2Date == null) return 1;
                return Long.compare(c2Date.getTime(), c1Date.getTime());
            })
            .findFirst().get();
    }

    protected abstract String getExpectedKeyRegex(String requiredVersion, String directory);

    class Content {
        private String key;
        private String directory;
        private String lastModified;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }
}
