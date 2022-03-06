package roart.filesystem.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.FileSystemConstants;
import roart.common.constants.FileSystemConstants.FileSystemType;
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
import roart.common.util.IOUtil;
import roart.filesystem.FileSystemOperations;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileSystem extends FileSystemOperations {

    private static final Logger log = LoggerFactory.getLogger(LocalFileSystem.class);

    public LocalFileSystem(String nodename, String configid, MyMyConfig nodeConf) {
        super(nodename, configid, nodeConf);
    }

    @Override
    public FileSystemFileObjectResult listFiles(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        List<FileObject> foList = new ArrayList<>();
        File dir = objectToFile(f);
        File[] listDir = dir.listFiles();
        if (listDir != null) {
            for (File file : listDir) {
                FileObject fo = new FileObject(f.location, file.getAbsolutePath());
                foList.add(fo);
            }
        }     
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        result.setFileObject(foList.stream().toArray(FileObject[]::new));
        return result;
    }

    @Override
    public FileSystemMyFileResult listFilesFull(FileSystemFileObjectParam param) throws Exception {
        FileObject f = param.fo;
        Map<String, MyFile> map = new HashMap<>();
        //List<FileObject> foList = new ArrayList<>();
        File dir = objectToFile(f);
        File[] listDir = dir.listFiles();
        if (listDir != null) {
            for (File file : listDir) {
                FileObject[] fo = new FileObject[1];
                fo[0] = new FileObject(f.location, file.getAbsolutePath());
                MyFile my = getMyFile(fo, false);
                if (my.exists) {
                    map.put(my.absolutePath, my);
                }
            }
        }     
        FileSystemMyFileResult result = new FileSystemMyFileResult();
        result.map = map;
        return result;
    }

    @Override
    public FileSystemBooleanResult exists(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        FileSystemBooleanResult result = new FileSystemBooleanResult();
        result.bool = objectToFile(f).exists();
        return result;
    }

    @Override
    public FileSystemPathResult getAbsolutePath(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        FileSystemPathResult result = new FileSystemPathResult();
        result.setPath(objectToFile(f).getAbsolutePath());
        return result;
    }

    @Override
    public FileSystemBooleanResult isDirectory(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        FileSystemBooleanResult result = new FileSystemBooleanResult();
        result.bool = objectToFile(f).isDirectory();
        return result;
    }

    @Override
    public FileSystemByteResult getInputStream(FileSystemFileObjectParam param) throws Exception {
        FileSystemByteResult result = new FileSystemByteResult();
        result.bytes = getBytesInner(param.fo);
        return result;
    }

    private InputStream getInputStreamInner(FileObject f) throws IOException {
        return new FileInputStream( objectToFile(f));
    }
    
    private byte[] getBytesInner(FileObject f) throws IOException {
        byte[] bytes;
        try {
            InputStream is = new FileInputStream( objectToFile(f) /*new File(getAbsolutePath(f))*/);
            bytes = IOUtil.toByteArrayMax(is);
            is.close();
        } catch (FileNotFoundException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
        return bytes;
    }

    @Override
    public FileSystemMyFileResult getWithInputStream(FileSystemPathParam param, boolean with) throws Exception {
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

    private MyFile getMyFile(FileObject[] fo, boolean withBytes) throws IOException {
        MyFile my = new MyFile();
        my.fileObject = fo;
        if (fo[0] != null) {
            my.exists = objectToFile(fo[0]).exists();
            if (my.exists) {
                my.isDirectory = objectToFile(fo[0]).isDirectory();
                my.absolutePath = objectToFile(fo[0]).getAbsolutePath();
                my.mtime = getMtime(fo[0]);
                my.ctime = getCtime(fo[0]);
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
        try {
            File file = objectToFile(f);
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attr.lastModifiedTime().toMillis();
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return 0;
        }
    }

    private long getCtime(FileObject f) {
        try {
            File file = objectToFile(f);
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attr.creationTime().toMillis();
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return 0;
        }
    }

    @Override
    public FileSystemFileObjectResult get(FileSystemPathParam param) {
        FileObject[] fo = getInner(param.path);
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        result.setFileObject(fo);
        return result;
    }

    private FileObject[] getInner(FileObject filename) {
        //if (filename.startsWith(FileSystemConstants.FILE)) {
        //    filename = filename.substring(5);
        //}
        FileObject[] fo = new FileObject[1];
        //fo[0] = new FileObject(filename, new Location(nodename, FileSystemConstants.LOCALTYPE));
        fo[0] = filename;
        return fo;
    }

    @Override
    public FileSystemFileObjectResult getParent(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        String parent = objectToFile(f).getParent();
        File file = new File(parent);
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fo = new FileObject[1];
        fo[0] = new FileObject(f.location, file.getAbsolutePath());
        result.setFileObject(fo);
        return result;
    }

    @Override
    public FileSystemConstructorResult destroy() {
        return null;
    }

    private File objectToFile(FileObject fo) {
        File result = null;
        if (fo.object instanceof String) {
            result = new File((String) fo.object);
        }
        return result;
    }

    public FileSystemMessageResult readFile(FileSystemFileObjectParam param) throws Exception {
        Map<String, InmemoryMessage> map = new HashMap<>();
        for (FileObject filename : param.fos) {
            InputStream inputStream;
            String md5;
            try {
                inputStream = getInputStreamInner(filename);
                md5 = getMd5(filename);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                continue;
            }
            Inmemory inmemory = InmemoryFactory.get(nodeConf.getInmemoryServer(), nodeConf.getInmemoryHazelcast(), nodeConf.getInmemoryRedis());
            InmemoryMessage msg = inmemory.send(EurekaConstants.READFILE + filename.toString(), inputStream, md5);
            map.put(filename.object, msg);
        }
        FileSystemMessageResult result = new FileSystemMessageResult();
        result.message = map;
        return result;
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
}
