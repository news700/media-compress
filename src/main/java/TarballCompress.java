

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class TarballCompress {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        StopWatch sw = new StopWatch();
        sw.start();

        try (S3Client s3Client = createS3Client()) {
            ListObjectsRequest request = ListObjectsRequest.builder().bucket("senecomptest").build();
            ListObjectsResponse response = s3Client.listObjects(request);

            List<S3Object> objects = response.contents();
            long count = objects.stream().filter(o -> o.size() > 0).count();
            System.out.println("File count: " + count);

            ThreadLocal<Integer> counter = new ThreadLocal<>();
            ForkJoinPool forkjoinPool = new ForkJoinPool(10);

            forkjoinPool.submit(() -> {
                objects.parallelStream().forEach(o -> {
                    if (o.size() == 0) {
                        return;
                    }
                    System.out.println("Object: " + o);
                    S3AsyncClient client = S3AsyncClient.create();
                    final String input = "/tmp/senecomptest/" + StringUtils.replace(o.key(), "/", "_");
                    final GetObjectRequest req = GetObjectRequest.builder().bucket("senecomptest").key(o.key()).build();
                    final CompletableFuture<GetObjectResponse> futureGet = client.getObject(req, AsyncResponseTransformer.toFile(Paths.get(input)));
                    futureGet.whenComplete((resp, err) -> {
                        try {
                            if (resp != null) {
                                counter.set(counter.get() + 1);
                                System.out.println(resp + ", Counter: " + counter.get());
                            } else {
                                err.printStackTrace();
                            }
                        } finally {
                            client.close();
                        }
                    });
                    futureGet.join();
                });
            }).get();

            System.out.println("Counter: " + counter.get());

            CompressUtils.compressTarGZ("/tmp/senecomptest", "/tmp/senecomptest.tar");
        }

        sw.stop();

        System.out.println(sw.formatTime());
    }

    private static S3Client createS3Client() {
        return S3Client.builder().region(Region.AP_NORTHEAST_2).build();
    }
}