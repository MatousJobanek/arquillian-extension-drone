package org.jboss.arquillian.drone.webdriver.binary.downloading;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.Execution;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.task.net.DownloadTool;

import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class Downloader {

    private static Logger log = Logger.getLogger(Downloader.class.toString());

    public static final String DRONE_TARGET_DOWNLOADED_DIRECTORY =
        DRONE_TARGET_DIRECTORY + "downloaded" + File.separator;

    public static File download(File targetDir, URL from) {
        if (targetDir == null){
            targetDir = new File(DRONE_TARGET_DOWNLOADED_DIRECTORY);
        }
        String fromUrl = from.toString();
        String fileName = fromUrl.substring(fromUrl.lastIndexOf("/") + 1);
        File target = new File(targetDir + File.separator + fileName);
        File downloaded = null;

        if (target.exists() && target.isFile()) {
            downloaded = target;
        } else if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        if (downloaded == null) {

            for (int i = 0; i < 3; i++) {
                try {
                    downloaded = runDownloadExecution(from, target.getAbsolutePath(), fileName).await();
                } catch (ExecutionException ee) {
                    System.err.print("ERROR: the downloading has failed. ");
                    if (2 - i > 0) {
                        System.err.println("Trying again - number of remaining attempts: " + (2 - i));
                        continue;
                    } else {
                        System.err.println("For more information see the stacktrace of an exception");
                        throw ee;
                    }
                }
                break;
            }
        }
        return downloaded;
    }

    private static Execution<File> runDownloadExecution(URL from, String target, String fileName) {
        Execution<File> execution = Spacelift.task(DownloadTool.class).from(from).to(target).execute();
        System.out.println(String.format("Drone: downloading %s from %s to %s ", fileName, from, target));

        while (!execution.isFinished()) {
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warning("Problem occurred when the thread was sleeping:\n" + e.getMessage());
            }
        }
        System.out.println();

        return execution;
    }
}
