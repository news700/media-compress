import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.util.Objects;

public class ApacheCompress {
    public static final String currPath = System.getProperty("user.dir");//프로젝트(현재) 경로

    public static void main(String[] args) throws IOException {
        String path = currPath + File.separator + "data";
        File file = new File(path);
        String[] files;

        //파일이 디렉토리 일경우 리스트를 읽어오고
        //파일이 디렉토리가 아니면 첫번째 배열에 파일이름을 넣는다.
        if (file.isDirectory()) {
            files = file.list();
        } else {
            files = new String[1];
            files[0] = file.getName();
        }

        //buffer size
        int size = 1024;
        byte[] buf = new byte[size];
        String outZipNm = path + File.separator + "apache.zip";

        FileInputStream fis = null;
        ZipArchiveOutputStream zos = null;
        BufferedInputStream bis = null;

        try {
            //Zip 파일생성
            zos = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(outZipNm)));
            for (String s : Objects.requireNonNull(files)) {
                //해당 폴더안에 다른 폴더가 있다면 지나간다.
                if (new File(path + "/" + s).isDirectory()) {
                    continue;
                }
                //encoding 설정
                zos.setEncoding("UTF-8");

                //buffer 에 해당파일의 stream 을 입력한다.
                fis = new FileInputStream(path + "/" + s);
                bis = new BufferedInputStream(fis, size);

                //zip 에 넣을 다음 entry 를 가져온다.
                zos.putArchiveEntry(new ZipArchiveEntry(s));

                //준비된 버퍼에서 집출력스트림으로 write 한다.
                int len;
                while ((len = bis.read(buf, 0, size)) != -1) {
                    zos.write(buf, 0, len);
                }

                bis.close();
                fis.close();
                zos.closeArchiveEntry();

            }
            zos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }
}
