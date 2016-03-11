package org.arquillian.drone.browserstack.extension.webdriver.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;

/**
 * Is responsible for starting a BrowserStackLocal binary
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackLocalRunner {

    private static Logger log = Logger.getLogger(BrowserStackLocalRunner.class.getName());

    private static BrowserStackLocalRunner browserStackLocalRunner = null;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final File browserStackLocalDirectory = new File("target" + File.separator + "browserstacklocal");
    private final File browserStackLocalFile =
        new File(browserStackLocalDirectory.getPath() + File.separator + "BrowserStackLocal" + (PlatformUtils.isWindows() ? ".exe" : ""));
    private final String basicUrl = "https://www.browserstack.com/browserstack-local/";
    private Process process = null;

    private BrowserStackLocalRunner() {
    }

    /**
     * Returns an instance of BrowserStackLocalRunner. If there has been already created, returns this one, otherwise
     * creates and returns a new one - behaves like singleton
     *
     * @return An instance of BrowserStackLocalRunner
     */
    public static BrowserStackLocalRunner createBrowserStackLocalInstance() {
        if (browserStackLocalRunner == null) {
            browserStackLocalRunner = new BrowserStackLocalRunner();
        }
        return browserStackLocalRunner;
    }

    /**
     * Runs BrowserStackLocal binary. In case that the binary has been already ran, then does nothing.
     *
     * @param accessKey An accessKey the binary should be ran with
     */
    public void runBrowserStackLocal(String accessKey) {
        if (process != null) {
            return;
        }
        if (!browserStackLocalFile.exists()) {
            prepareBrowserStackLocal();
        }

        ProcessBuilder processBuilder =
            new ProcessBuilder().command(browserStackLocalFile.getAbsolutePath(), "-v", accessKey);
        try {
            process = processBuilder.start();

            final Reader reader = new Reader();
            reader.start();
            Runtime.getRuntime().addShutdownHook(new CloseChildProcess());
            countDownLatch.await(20, TimeUnit.SECONDS);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the BrowserStackLocal binary. Creates the directory target/browserstacklocal; downloads a zip file
     * containing the binary; extracts it into the created directory and marks the binary as executable.
     */
    private void prepareBrowserStackLocal() {
        String platformBinaryNameUrl = getPlatformBinaryNameUrl();
        File browserStackLocalZipFile =
            new File(browserStackLocalDirectory.getPath() + File.separator + platformBinaryNameUrl);
        String url = basicUrl + platformBinaryNameUrl;

        log.info("Creating directory: " + browserStackLocalDirectory);
        browserStackLocalDirectory.mkdir();

        log.info("downloading zip file from: " + url + " to " + browserStackLocalZipFile.getPath());
        Spacelift.task(DownloadTool.class).from(url).to(browserStackLocalZipFile.getPath()).execute().await();

        log.info("extracting zip file: " + browserStackLocalZipFile + " to " + browserStackLocalDirectory.getPath());
        Spacelift.task(browserStackLocalZipFile, UnzipTool.class).toDir(browserStackLocalDirectory.getPath()).execute()
            .await();

        log.info("marking binary file: " + browserStackLocalFile.getPath() + " as executable");
        browserStackLocalFile.setExecutable(true);
    }

    /**
     * Returns name of a zip file, that should contain the BrowserStackLocal binary. The name contains corresponding
     * name of the platform the program is running on.
     *
     * @return Formatted name of the BrowserStackLocal zip file
     */
    private String getPlatformBinaryNameUrl() {
        String binary = "BrowserStackLocal-%s.zip";
        switch (PlatformUtils.platform().os()) {
            case WINDOWS:
                return String.format(binary, "win32");
            case UNIX:
                if (PlatformUtils.is64()) {
                    return String.format(binary, "linux-x64");
                } else {
                    return String.format(binary, "linux-ia32");
                }
            case MACOSX:
                return String.format(binary, "darwin-x64");
            default:
                throw new IllegalStateException("The current platform is not supported."
                                                    + "Supported platforms are windows, linux and macosx."
                                                    + "Your platform has been detected as "
                                                    + PlatformUtils.platform().os().toString().toLowerCase() + ""
                                                    + "from the the system property 'os.name' => '" + PlatformUtils.OS
                                                    + "'.");

        }
    }

    /**
     * This thread reads an output from the BrowserStackLocal binary and prints it on the standard output. At the same
     * time it checks if the output contains one of the strings that indicate that the binary has been successfully
     * started and the connection established or that another BrowserStackLocal binary is already running
     */
    private class Reader extends Thread {
        public void run() {

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isAlreadyRunning = false;

            while (!isAlreadyRunning) {
                try {
                    synchronized (process) {
                        if (!process.isAlive()) {
                            break;
                        }
                        while (in.ready() && (line = in.readLine()) != null) {
                            System.out.println("[BrowserStackLocal]$ " + line);

                            if (countDownLatch.getCount() > 0) {
                                if (line.contains(
                                    "You can now access your local server(s) in our remote browser.")) {
                                    countDownLatch.countDown();
                                } else if (line.contains(
                                    "Either another browserstack local client is running on your machine or some server is listening on port")) {
                                    isAlreadyRunning = true;
                                    countDownLatch.countDown();
                                }
                            }
                        }
                        Thread.sleep(10);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Is responsible for destroying a running BrowserStackLocal binary process
     */
    private class CloseChildProcess extends Thread {
        public void run() {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
