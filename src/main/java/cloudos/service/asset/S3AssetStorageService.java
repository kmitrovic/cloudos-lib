package cloudos.service.asset;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.*;


@Slf4j
public class S3AssetStorageService extends AssetStorageService {

    public static final String PROP_ACCESS_KEY = "accessKey";
    public static final String PROP_SECRET_KEY = "secretKey";
    public static final String PROP_BUCKET = "bucket";
    public static final String PROP_PREFIX = "prefix";
    public static final String PROP_LOCAL_CACHE = "localCache";
    public static final String CACHE_DISABLED = "disabled";

    @Getter @Setter private String accessKey;
    @Getter @Setter private String secretKey;
    @Getter @Setter private String bucket;
    @Getter @Setter private String prefix;
    @Getter @Setter private File localCache;

    private final AmazonS3Client s3Client;

    public S3AssetStorageService(Map<String, String> config) {
        setAccessKey(config.get(PROP_ACCESS_KEY));
        setSecretKey(config.get(PROP_SECRET_KEY));
        setBucket(config.get(PROP_BUCKET));
        setPrefix(config.get(PROP_PREFIX));

        String local = config.get(PROP_LOCAL_CACHE);
        if (empty(local)) local = System.getProperty("java.io.tmpdir");

        if (local.equals(CACHE_DISABLED)) {
            localCache = null;
        } else {
            setLocalCache(mkdirOrDie(local));
        }

        s3Client = new AmazonS3Client(new AWSCredentials() {
            @Override public String getAWSAccessKeyId() { return getAccessKey(); }
            @Override public String getAWSSecretKey() { return getSecretKey(); }
        });
    }

    @Override public AssetStream load(String uri) {
        if (localCache != null) {
            final File cachefile = new File(abs(localCache) + "/" + uri);
            if (cachefile.exists()) {
                try {
                    return new AssetStream(uri, new FileInputStream(cachefile), toStringOrDie(abs(cachefile) + ".contentType"));
                } catch (IOException e) {
                    die("load: " + e, e);
                }
            }
        }
        try {
            final S3Object s3Object = s3Client.getObject(bucket, prefix + "/" + uri);
            return new AssetStream(uri, s3Object.getObjectContent(), s3Object.getObjectMetadata().getContentType());
        } catch (Exception e) {
            log.warn("load: "+e);
            return null;
        }
    }

    @Override public boolean exists(String uri) {
        if (localCache != null && new File(abs(localCache) + "/" + uri).exists()) return true;
        try {
            s3Client.getObject(bucket, prefix + "/" + uri);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override public String store(InputStream fileStream, String filename, String path) {

        final String mimeType = filename.endsWith(".json") ? "application/json" : Mimetypes.getInstance().getMimetype(filename);

        try {
            final File temp = File.createTempFile("s3file-", ".tmp");
            FileUtil.toFile(temp, fileStream);
            if (path == null) path = getUri(temp, filename);

            final File stored = (localCache == null) ? temp : new File(abs(localCache) + "/" + path);

            if (localCache != null) {
                FileUtil.toFile(abs(stored) + ".contentType", mimeType);
                mkdirOrDie(stored.getParentFile());
                if (!temp.renameTo(stored)) die("store: error renaming " + abs(temp) + " -> " + abs(stored));
            }

            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(mimeType);
            metadata.setContentLength(stored.length());
            put(path, stored, metadata);

            return path;

        } catch (Exception e) {
            return die("store: "+e, e);
        }
    }

    public void put(String path, File stored, ObjectMetadata metadata) throws IOException {
        if (metadata == null) {
            put(path, stored);
        } else {
            // ok we have to use an input stream to do the metadata in the same call
            try (InputStream in = new FileInputStream(stored)) {
                s3Client.putObject(bucket, prefix + "/" + path, in, metadata);
            }
        }
    }

    public void put(String path, File stored) throws IOException {
        s3Client.putObject(bucket, prefix + "/" + path, stored);
    }
}
