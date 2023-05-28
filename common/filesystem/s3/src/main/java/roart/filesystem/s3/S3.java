package roart.filesystem.s3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.config.ConfigConstants;
import roart.common.config.ConfigData;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.FileSystemConstants;
import roart.common.filesystem.FileSystemBooleanResult;
import roart.common.filesystem.FileSystemByteResult;
import roart.common.filesystem.FileSystemConstructorResult;
import roart.common.filesystem.FileSystemFileObjectParam;
import roart.common.filesystem.FileSystemFileObjectResult;
import roart.common.filesystem.FileSystemMessageResult;
import roart.common.filesystem.FileSystemMyFileResult;
import roart.common.filesystem.FileSystemPathParam;
import roart.common.filesystem.FileSystemPathResult;
import roart.common.filesystem.FileSystemStringResult;
import roart.common.filesystem.MyFile;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.inmemory.model.InmemoryUtil;
import roart.common.model.FileObject;
import roart.common.model.Location;
import roart.common.util.FsUtil;
import roart.common.util.IOUtil;
import roart.filesystem.FileSystemOperations;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;

public class S3 extends FileSystemOperations {

    private static final Logger log = LoggerFactory.getLogger(S3.class);

    private static final String DELIMITER = "/";

    /*private*/ S3Config conf;

    public S3(String nodename, String configid, ConfigData configData) {
        super(nodename, configid, configData);
        try {
            
            AwsBasicCredentials credentials = AwsBasicCredentials.create(nodeConf.getS3AccessKey(), nodeConf.getS3SecretKey());
            //ClientConfiguration clientConfiguration = new ClientConfiguration();
            //clientConfiguration.setSignerOverride("AWSS3V4SignerType");

            //S3Client s3Client2 = null;
            S3Client s3Client = S3Client.builder()
                    .region(Region.of(nodeConf.getS3Region()))
                    .endpointOverride(new URI("http://" + nodeConf.getS3Host() + ":" + nodeConf.getS3Port()))
                    //.withPathStyleAccessEnabled(true)
                    //.withClientConfiguration(clientConfiguration)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
            
            conf = new S3Config();
            conf.s3client = s3Client;
        } catch (Exception e) {
            log.error("Exception", e);
            //return null;
        }
    }

    @Override
    public FileSystemFileObjectResult listFiles(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        List<FileObject> foList = new ArrayList<FileObject>();
        String bucket = f.location.extra;
        String prefix = f.object;
        ListObjectsRequest req = ListObjectsRequest.builder().bucket(bucket).prefix(prefix).delimiter(DELIMITER).build();
        ListObjectsResponse listing = conf.s3client.listObjects(req);
        try {
            for (S3Object summary: listing.contents()) {
                System.out.println(summary.key());
                FileObject fo = new FileObject(f.location, formatBack(summary.key()));
                foList.add(fo);
            }
            for (CommonPrefix commonPrefix : listing.commonPrefixes()) {
                System.out.println(commonPrefix);
            }
            FileSystemFileObjectResult result = new FileSystemFileObjectResult();
            result.setFileObject(foList.toArray(new FileObject[0]));
            return result;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public FileSystemMyFileResult listFilesFull(FileSystemFileObjectParam param) throws Exception {
        FileObject f = param.fo;
        Map<String, MyFile> map = new HashMap<>();
        String bucket = f.location.extra;
        String prefix = f.object;
        ListObjectsRequest req = ListObjectsRequest.builder().bucket(bucket).prefix(prefix).delimiter(DELIMITER).build();
        ListObjectsResponse listing = conf.s3client.listObjects(req);
        try {
            for (S3Object summary: listing.contents()) {
                System.out.println(summary.key());
                FileObject[] fo = new FileObject[1];
                fo[0] = new FileObject(f.location, formatBack(summary.key()));
                MyFile my = getMyFile(fo, false);
                if (my.exists) {
                    map.put(formatBack(summary.key()), my);
                }
            }
            for (CommonPrefix commonPrefix : listing.commonPrefixes()) {
                System.out.println(commonPrefix);
                FileObject[] fo = new FileObject[1];
                fo[0] = new FileObject(f.location, formatBack(commonPrefix.prefix()));
                MyFile my = getMyFile(fo, false);
                if (my.exists) {
                    map.put(formatBack(commonPrefix.prefix()), my);
                }
            }
            FileSystemMyFileResult result = new FileSystemMyFileResult();
            result.map = map;
            return result;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public FileSystemBooleanResult exists(FileSystemFileObjectParam param) {
         FileSystemBooleanResult result = new FileSystemBooleanResult();
        result.bool = getExistInner(param.fo);
        return result;
    }

    private boolean getExistInner(FileObject f) {
        if (f.object.endsWith("/")) {
            return true; //conf.s3client.doesBucketExist(f.location.extra + "/" + f.object);
        }
        try {
            HeadObjectResponse headResponse = conf.s3client.headObject(HeadObjectRequest.builder().bucket(f.location.extra).key(f.object).build());
            return true;
        } catch (/*NoSuchKey*/Exception e) {
            return false;
        }
    }

    @Override
    public FileSystemPathResult getAbsolutePath(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        String p = getAbsolutePathInner(f);
        FileSystemPathResult result = new FileSystemPathResult();
        result.setPath(p);
        return result;
    }

    private String getAbsolutePathInner(FileObject f) {
        String p = f.object;
        return p;
    }

    @Override
    public FileSystemBooleanResult isDirectory(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        boolean isDirectory = isDirectoryInner(f);
        FileSystemBooleanResult result = new FileSystemBooleanResult();
        result.bool = isDirectory;
        return result;	    
    }

    private boolean isDirectoryInner(FileObject f) {
        boolean isDirectory;
        try {
            isDirectory = f.object.endsWith(DELIMITER);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            isDirectory = false;
        }
        return isDirectory;
    }

    @Override
    public FileSystemByteResult getInputStream(FileSystemFileObjectParam param) {
        FileSystemByteResult result = new FileSystemByteResult();
        result.bytes = getBytesInner(param.fo);
        return result;
    }

    private byte[] getBytesInner(FileObject f) {
        try (ResponseInputStream<GetObjectResponse> inputStream = conf.s3client.getObject(GetObjectRequest.builder().bucket(f.location.extra).key(f.object).build())) {
            return IOUtil.toByteArrayMax(inputStream);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    private InputStream getInputStreamInner(FileObject f) throws IOException {
        return conf.s3client.getObject(GetObjectRequest.builder().bucket(f.location.extra).key(f.object).build());
        /*
        try (ResponseInputStream<GetObjectResponse> inputStream = conf.s3client.getObject(GetObjectRequest.builder().bucket(f.location.extra).key(f.object).build())) {
            return inputStream;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
        */
    }

    @Override
    public FileSystemMyFileResult getWithInputStream(FileSystemPathParam param, boolean with) {
        Map<String, MyFile> map = new HashMap<>();
        for (FileObject filename : param.paths) {
            FileObject[] fo = new FileObject[] { filename };
            MyFile my = getMyFile(fo, with);
            if (my.exists) {
                map.put(filename.object, my);
            }
        }
        FileSystemMyFileResult result = new FileSystemMyFileResult();
        result.map = map;
        return result;
    }

    private MyFile getMyFile(FileObject[] fo, boolean withBytes) {
        MyFile my = new MyFile();
        my.fileObject = fo;
        if (fo[0] != null) {
            my.exists = getExistInner(fo[0]);
            if (my.exists) {
                my.isDirectory = isDirectoryInner(fo[0]);
                my.absolutePath = fo[0].object;
                if (!my.isDirectory) {
                    my.mtime = getMtime(fo[0]);
                    my.ctime = my.mtime;
                }
                if (withBytes) {
                    my.bytes = getBytesInner(fo[0]);
                }
            } else {
                log.info("File does not exist {}", fo[0]);            
            }
        }
        return my;
    }

    private long getMtime(FileObject f) {
        try (ResponseInputStream<GetObjectResponse> inputStream = conf.s3client.getObject(GetObjectRequest.builder().bucket(f.location.extra).key(f.object).build())) {
            return inputStream.response().lastModified().toEpochMilli();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return 0;
        }
    }

    @Override
    public FileSystemFileObjectResult getParent(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        String name = f.object;
        File fi = new File(name);
        String parent = fi.getParent();
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fo = new FileObject[1];
        fo[0] = new FileObject(f.location, parent);
        result.setFileObject(fo);
        return result;
    }

    @Override
    public FileSystemFileObjectResult get(FileSystemPathParam param) {
        FileObject[] fos = new FileObject[] { param.path };
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        result.setFileObject(fos);
        return result;
    }

    private FileObject[] getInner(FileObject filename, String containerName) {
        FileObject[] fos = new FileObject[1];
        try {
            FileObject fo;
            // if it exists, it is a file and not a dir
            if (getExistInner(filename)) {
                //fo = new FileObject(filename, new Location(nodename, FileSystemConstants.S3TYPE, containerName));
            } else {
               // fo = new FileObject(filename, new Location(nodename, FileSystemConstants.S3TYPE, containerName));
            }
            fos[0] = filename;
        } catch (Exception e) {
            log.error("Exception", e);
            return null;
        }
        return fos;
    }

    @Override
    public FileSystemConstructorResult destroy() {
        return null;
    }

    @Override
    public FileSystemMessageResult readFile(FileSystemFileObjectParam param) throws Exception {
        Map<String, InmemoryMessage> map = new HashMap<>();
        for (FileObject filename : param.fos) {
            String md5 = getMd5(filename);
            try (InputStream inputStream  = getInputStreamInner(filename)) {
                Inmemory inmemory = InmemoryFactory.get(nodeConf.getInmemoryServer(), nodeConf.getInmemoryHazelcast(), nodeConf.getInmemoryRedis());
                InmemoryMessage msg = inmemory.send(EurekaConstants.READFILE + filename.toString(), inputStream, md5);
                map.put(filename.object, msg);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                return null;
            }
        }
        FileSystemMessageResult result = new FileSystemMessageResult();
        result.message = map;
        return result;
    }

    private String format(String string) {
        if (string.startsWith("/")) {
            string = string.substring(1);
        }
        return string;

    }

    private String formatBack(String string) {
        if (!string.startsWith("/")) {
            string = "/" + string;
        }
        return string;

    }
    
    public String getMd5(FileObject fo) throws Exception {
        try (InputStream is = getInputStreamInner(fo)) {
            return DigestUtils.md5Hex( is );
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    public FileSystemStringResult getMd5(FileSystemFileObjectParam param) {
        Map<String, String> map = new HashMap<>();
        for (FileObject filename : param.fos) {
            String md5;
            try {
                md5 = getMd5(filename);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                continue;
            }
            map.put(filename.object, md5);
        }
        FileSystemStringResult result = new FileSystemStringResult();
        result.map = map;
        return result;
    }

    @Override
    public FileSystemMessageResult writeFile(FileSystemFileObjectParam param) throws Exception {
        Map<String, InmemoryMessage> map = new HashMap<>();
        Inmemory inmemory = InmemoryFactory.get(nodeConf.getInmemoryServer(), nodeConf.getInmemoryHazelcast(), nodeConf.getInmemoryRedis());
        for (Entry<String, InmemoryMessage> entry : param.map.entrySet()) {
            FileObject filename = FsUtil.getFileObject(entry.getKey());
            InmemoryMessage msg = entry.getValue();
            String content = inmemory.read(msg);
            conf.s3client.putObject(PutObjectRequest.builder().bucket(filename.location.extra).key(filename.object).build(), RequestBody.fromBytes(content.getBytes()));
            inmemory.delete(msg);
        }
        FileSystemMessageResult result = new FileSystemMessageResult();
        result.message = map;
        return result;
    }

}
