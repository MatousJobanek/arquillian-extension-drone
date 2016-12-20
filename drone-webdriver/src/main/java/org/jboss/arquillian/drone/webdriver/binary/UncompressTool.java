package org.jboss.arquillian.drone.webdriver.binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.archive.UntarTool;
import org.arquillian.spacelift.task.archive.UnzipTool;

import static org.jboss.arquillian.drone.webdriver.utils.Constants.DRONE_TARGET_DIRECTORY;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class UncompressTool {

    private static Logger log = Logger.getLogger(UncompressTool.class.toString());

    public static File extract(File toExtract) throws Exception {

        String dir = getMd5hash(toExtract);
        if (dir == null) {
            dir = UUID.randomUUID().toString();
        }
        File targetDir = new File(DRONE_TARGET_DIRECTORY + File.separator + dir);
        if (!targetDir.exists() || targetDir.listFiles(file -> file.isFile()).length == 0) {

            String filePath = toExtract.getAbsolutePath();

            log.info("Extracting zip file: " + toExtract + " to " + targetDir.getPath());
            if (filePath.endsWith(".zip")) {
                Spacelift.task(toExtract, UnzipTool.class).toDir(targetDir).execute().await();

            } else if (filePath.endsWith(".tar.gz")) {
                Spacelift.task(toExtract, UntarTool.class).gzip(true).toDir(targetDir).execute().await();

            } else if (filePath.endsWith(".tar.bz2")) {
                Spacelift.task(toExtract, UntarTool.class).bzip2(true).toDir(targetDir).execute().await();

            } else {
                log.info(
                    "The file " + toExtract + " is not compressed by format by a format that is supported by Drone. "
                        + "Drone supported formats are .zip, .tar.gz, .tar.bz2. The file will be only copied");
                targetDir.mkdirs();
                Files.copy(toExtract.toPath(), new File(targetDir + File.separator + toExtract.getName()).toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return targetDir;
    }

    private static String getMd5hash(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return DigestUtils.md5Hex(fis);
        } catch (IOException e) {
            log.warning("A problem occurred when md5 hash of a file " + file + " was being retrieved:\n"
                            + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.warning("A problem occurred when FileInputStream of a file " + file
                                    + "was being closed:\n" + e.getMessage());
                }
            }
        }
        return null;
    }
}
