import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

public class CompressUtils {
    public static void compressTarGZ(String input, String output) throws FileNotFoundException, IOException {
        TarArchiveOutputStream tos = null;
        try {
            tos = new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(new File(output))));
            //tos = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(new File(output)))));
            addFileToTarGz(tos, input, "");
        } finally {
            if (tos != null) {
                tos.finish();
                tos.close();
            }
        }
    }

    public static void addFileToTarGz(TarArchiveOutputStream tos, String input, String base) throws IOException {
        File file = new File(input);
        String entryName = base + file.getName();

        TarArchiveEntry entry = new TarArchiveEntry(file, entryName);
        tos.putArchiveEntry(entry);
        if (file.isFile()) {
            IOUtils.copy(new FileInputStream(file), tos);
            tos.closeArchiveEntry();
        } else {
            tos.closeArchiveEntry();
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    System.out.println(child.getName());
                    addFileToTarGz(tos, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

    public static void uncompressTarGZ(File tarFile, File dest) throws IOException {
        dest.mkdir();

        TarArchiveInputStream tis;

        tis = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(tarFile))));
        TarArchiveEntry entry = tis.getNextTarEntry();

        while (entry != null) {
            File destPath = new File(dest, entry.getName());
            System.out.println("working: " + destPath.getCanonicalPath());

            if (entry.isDirectory()) {
                destPath.mkdirs();
            } else {
                destPath.createNewFile();
                byte[] btoRead = new byte[1024];
                BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath));
                int len;
                while ((len = tis.read(btoRead)) != -1) {
                    bout.write(btoRead, 0, len);
                }
                bout.close();
            }
            entry = tis.getNextTarEntry();
        }
        tis.close();
    }
}
