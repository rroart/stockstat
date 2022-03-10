package roart.filesystem.hdfs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

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
import roart.common.filesystem.MyFile;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.inmemory.model.InmemoryUtil;
import roart.common.filesystem.FileSystemPathParam;
import roart.common.filesystem.FileSystemPathResult;
import roart.common.filesystem.FileSystemStringResult;
import roart.common.model.FileObject;
import roart.common.model.Location;
import roart.common.util.FsUtil;
import roart.common.util.IOUtil;
import roart.filesystem.FileSystemOperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDFS extends FileSystemOperations {

    private static final Logger log = LoggerFactory.getLogger(HDFS.class);

    private HDFSConfig conf;

    public HDFS(String nodename, String configid, MyMyConfig nodeConf) {
        super(nodename, configid, nodeConf);
        conf = new HDFSConfig();
        Configuration configuration = new Configuration();
        conf.configuration = configuration;
        String fsdefaultname = nodeConf.getHDFSDefaultName();
        if (fsdefaultname != null) {
            configuration.set("fs.default.name", fsdefaultname);
            log.info("Setting hadoop fs.default.name " + fsdefaultname);
        }
    }

    @Override
    public FileSystemConstructorResult destroy() throws IOException {
        // TODO right?
        conf.configuration.clear();
        return null;
    }

    @Override
    public FileSystemFileObjectResult listFiles(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        List<FileObject> foList = new ArrayList<FileObject>();
        FileSystem fs;
        try {
            fs = FileSystem.get(conf.configuration);
            Path dir = new Path(f.object);
            FileStatus[] status = fs.listStatus(dir);
            Path[] listedPaths = FileUtil.stat2Paths(status);
            for (Path path : listedPaths) {
                FileObject fo = new FileObject(f.location, path.toUri().getPath());
                foList.add(fo);
            }
            result.setFileObject(foList.toArray(new FileObject[0]));
            return result;
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public FileSystemMyFileResult listFilesFull(FileSystemFileObjectParam param) throws Exception {
        FileObject f = param.fo;
        Map<String, MyFile> map = new HashMap<>();
        FileSystem fs;
        try {
            fs = FileSystem.get(conf.configuration);
            Path dir = new Path(f.object);
            FileStatus[] status = fs.listStatus(dir);
            Path[] listedPaths = FileUtil.stat2Paths(status);
            for (Path path : listedPaths) {
                FileObject[] fo = new FileObject[1];
                fo[0] = new FileObject(f.location, path.toUri().getPath());
                MyFile my = getMyFile(fo, false);
                if (my.exists) {
                    map.put(my.absolutePath, my);
                }
            }
            FileSystemMyFileResult result = new FileSystemMyFileResult();
            result.map = map;
            return result;
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public FileSystemBooleanResult exists(FileSystemFileObjectParam param) {
        FileSystemBooleanResult result = new FileSystemBooleanResult();
        result.bool = existsInner(param.fo);
        return result;
    }

    private boolean existsInner(FileObject f) {
        Path path = new Path(f.object);
        boolean exist;
        try {
            FileSystem fs = FileSystem.get(conf.configuration);
            exist = fs.exists(path);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            exist = false;
        }
        return exist;
    }

    @Override
    public FileSystemPathResult getAbsolutePath(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        String p = getAbsolutePathInner(f);
        FileSystemPathResult result = new FileSystemPathResult();
        result.setPath(p);
        return result;
        /*
		try {
			FileSystem fs = FileSystem.get(configuration);
			FileStatus fstat = fs.getFileStatus(path);

		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
			return null;
		}
         */
    }

    private String getAbsolutePathInner(FileObject f) {
        // TODO
        Path path = new Path(f.object);
        //log.info("mypath " + path.getName() + " " + path.getParent().getName() + " " + path.toString());
        // this is hdfs://server/path
        String p = path.toUri().getPath();
        //p = p.substring(7);
        //int i = p.indexOf("/");
        //p = FileSystemConstants.HDFS + p.substring(i);
        //log.info("p " + p);
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
        Path path = new Path(f.object);
        boolean isDirectory;
        try {
            FileSystem fs = FileSystem.get(conf.configuration);
            FileStatus status = fs.getFileStatus(path);
            isDirectory = status.isDirectory();
         } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            isDirectory = false;
        }
        return isDirectory;
    }

    @Override
    public FileSystemByteResult getInputStream(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        FileSystemByteResult result = new FileSystemByteResult();
        result.bytes = getBytesInner(f);
        return result;
    }

    private byte[] getBytesInner(FileObject f) {
        FileSystem fs;
        byte[] bytes;
        try {
            fs = FileSystem.get(conf.configuration);
            InputStream is = fs.open(new Path(f.object));
            bytes = IOUtil.toByteArrayMax(is);
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(Constants.EXCEPTION, e);
            return null;
        }
        return bytes;
    }

    private InputStream getInputStreamInner(FileObject f) throws IOException {
        FileSystem fs = FileSystem.get(conf.configuration);
        return fs.open(new Path(f.object));
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
            my.exists = existsInner(fo[0]);
            if (my.exists) {
                my.isDirectory = isDirectoryInner(fo[0]);
                my.absolutePath = getAbsolutePathInner(fo[0]);
                my.mtime = getMtime(fo[0]);
                my.ctime = my.mtime;
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
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf.configuration);
            return fs.getFileStatus(new Path(f.object)).getModificationTime();
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
            return 0;
        }
    }

    @Override
    public FileSystemFileObjectResult getParent(FileSystemFileObjectParam param) {
        FileObject f = param.fo;
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fo = new FileObject[1];
        Path parent = new Path(f.object).getParent();
        fo[0] = new FileObject(f.location, parent.toUri().getPath());
        result.setFileObject(fo);
        return result;
    }

    @Override
    public FileSystemFileObjectResult get(FileSystemPathParam param) {
        FileObject string = param.path;
        FileObject[] fo = new FileObject[] { string };
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        result.setFileObject(fo);
        return result;
    }

    @Deprecated
    private FileObject[] getInner(String string) {
        // TODO
        //if (string.startsWith(FileSystemConstants.HDFS)) {
       //    string = string.substring(FileSystemConstants.HDFSLEN);
        //}
        FileObject[] fo = new FileObject[1];
        //fo[0] = new FileObject(string, new Location(nodename, FileSystemConstants.HDFSTYPE));
        return fo;
    }

    @Override
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
            FileSystem fs = FileSystem.get(conf.configuration);
            FSDataOutputStream stream = fs.create(new Path(filename.object));
            stream.write(content.getBytes());
            stream.close();
            inmemory.delete(msg);
        }
        FileSystemMessageResult result = new FileSystemMessageResult();
        result.message = map;
        return result;
    }
}
