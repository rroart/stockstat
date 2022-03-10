package roart.filesystem.s3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
//import software.amazon.awssdk.services.s3.S3Client;

public class S3 extends FileSystemOperations {

    private static final Logger log = LoggerFactory.getLogger(S3.class);

    private static final String DELIMITER = "/";

    /*private*/ S3Config conf;

    public S3(String nodename, String configid, MyMyConfig nodeConf) {
        super(nodename, configid, nodeConf);
        try {
            
            AWSCredentials credentials = new BasicAWSCredentials(nodeConf.getS3AccessKey(), nodeConf.getS3SecretKey());
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setSignerOverride("AWSS3V4SignerType");
            
            //S3Client s3Client2 = null;
            AmazonS3 s3Client = AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://" + nodeConf.getS3Host() + ":" + nodeConf.getS3Port(), nodeConf.getS3Region()))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfiguration)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
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
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(prefix).withDelimiter(DELIMITER);
        ListObjectsV2Result listing = conf.s3client.listObjectsV2(req);
        try {
            for (S3ObjectSummary summary: listing.getObjectSummaries()) {
                System.out.println(summary.getKey());
                FileObject fo = new FileObject(f.location, formatBack(summary.getKey()));
                foList.add(fo);
            }
            for (String commonPrefix : listing.getCommonPrefixes()) {
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
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucket).withPrefix(prefix).withDelimiter(DELIMITER);
        ListObjectsV2Result listing = conf.s3client.listObjectsV2(req);
        try {
            for (S3ObjectSummary summary: listing.getObjectSummaries()) {
                System.out.println(summary.getKey());
                FileObject[] fo = new FileObject[1];
                fo[0] = new FileObject(f.location, formatBack(summary.getKey()));
                MyFile my = getMyFile(fo, false);
                if (my.exists) {
                    map.put(formatBack(summary.getKey()), my);
                }
            }
            for (String commonPrefix : listing.getCommonPrefixes()) {
                System.out.println(commonPrefix);
                FileObject[] fo = new FileObject[1];
                fo[0] = new FileObject(f.location, formatBack(commonPrefix));
                MyFile my = getMyFile(fo, false);
                if (my.exists) {
                    map.put(formatBack(commonPrefix), my);
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
        boolean exist = false;
        if (f.object.endsWith("/")) {
            return true; //conf.s3client.doesBucketExist(f.location.extra + "/" + f.object);
        }
        exist = conf.s3client.doesObjectExist(f.location.extra, f.object);
        return exist;
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
        byte[] bytes;
        try {
            S3Object s3object = conf.s3client.getObject(f.location.extra, f.object);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            bytes = IOUtil.toByteArrayMax(inputStream);
            inputStream.close();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
        return bytes;
    }

    private InputStream getInputStreamInner(FileObject f) throws IOException {
        S3Object s3object = conf.s3client.getObject(f.location.extra, f.object);
        return s3object.getObjectContent();
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
        S3Object s3object = conf.s3client.getObject(f.location.extra, f.object);
        return s3object.getObjectMetadata().getLastModified().getTime();
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
            InputStream inputStream;
            String md5;
            try {
                inputStream  = getInputStreamInner(filename);
                md5 = getMd5(filename);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                return null;
            }
            Inmemory inmemory = InmemoryFactory.get(nodeConf.getInmemoryServer(), nodeConf.getInmemoryHazelcast(), nodeConf.getInmemoryRedis());
            InmemoryMessage msg = inmemory.send(EurekaConstants.READFILE + filename.toString(), inputStream, md5);
            map.put(filename.object, msg);
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
        String md5;
        try {
            InputStream is = getInputStreamInner(fo);
            md5 = DigestUtils.md5Hex( is );
            is.close();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
        return md5;
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
            conf.s3client.putObject(filename.location.extra, filename.object, content);
            inmemory.delete(msg);
        }
        FileSystemMessageResult result = new FileSystemMessageResult();
        result.message = map;
        return result;
    }

}
