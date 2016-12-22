package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.drone.webdriver.binary.downloading.ExternalBinary;
import org.junit.Test;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class GitHubSourceTestCase {

    @Test
    public void testGetLatestRelease() throws Exception {
        DummyRepositoryGitHubSource testRepositoryGitHubSource = new DummyRepositoryGitHubSource();
        ExternalBinary latestRelease = testRepositoryGitHubSource.getLatestRelease();

        Assertions.assertThat(latestRelease.getUrl()).isEqualTo(DummyRepositoryGitHubSource.URL_TO_LATEST_RELEASE);
        Assertions.assertThat(latestRelease.getVersion()).isEqualTo(DummyRepositoryGitHubSource.LATEST_RELEASE);
    }

    @Test
    public void testGetReleaseForVersion() throws Exception {
        String expectedRelease = "3.0.0.Final";
        DummyRepositoryGitHubSource testRepositoryGitHubSource = new DummyRepositoryGitHubSource();
        ExternalBinary releaseForVersion = testRepositoryGitHubSource.getReleaseForVersion(expectedRelease);

        String expectedUrl = String.format(DummyRepositoryGitHubSource.BASE_URL_TO_RELEASE, expectedRelease);
        Assertions.assertThat(releaseForVersion.getUrl()).isEqualTo(expectedUrl);
        Assertions.assertThat(releaseForVersion.getVersion()).isEqualTo(expectedRelease);
    }

    @Test
    public void testNonExistingVersion() throws Exception {
        String nonExisting = "non-existing";
        DummyRepositoryGitHubSource testRepositoryGitHubSource = new DummyRepositoryGitHubSource();
        ExternalBinary releaseForVersion = testRepositoryGitHubSource.getReleaseForVersion(nonExisting);
        Assertions.assertThat(releaseForVersion).isNull();
    }
}
