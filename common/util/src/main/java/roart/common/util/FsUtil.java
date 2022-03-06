package roart.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.FileSystemConstants;
import roart.common.constants.FileSystemConstants.FileSystemType;
import roart.common.model.FileLocation;
import roart.common.model.FileObject;
import roart.common.model.Location;

public class FsUtil {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Deprecated
    public static boolean isRemote(String filename) {
        return false;
        //return filename.startsWith(FileSystemConstants.HDFS) || filename.startsWith(FileSystemConstants.SWIFT);
    }
      
    @Deprecated
    public static FileSystemType getFileSystemType(String filename) {
        // TODO
        /*
        System.out.println("FN "+ filename);
        if (filename.indexOf(':') < 0) {
           return FileSystemType.LOCAL; 
        }
        if (filename.startsWith(FileSystemConstants.LOCAL)) {
            return FileSystemType.LOCAL;
         }
        if (filename.startsWith(FileSystemConstants.FILE)) {
           return FileSystemType.LOCAL;
        }
        if (filename.startsWith(FileSystemConstants.HDFS)) {
           return FileSystemType.HDFS;
        }
        if (filename.startsWith(FileSystemConstants.SWIFT)) {
            return FileSystemType.SWIFT;
        }
        */
        return null;
    }
    
    public static FileSystemType getFilenameType(String filename) {
        System.out.println("FN "+ filename);
        /*
        if (filename.indexOf(':') < 0) {
           //return FileSystemType.LOCAL; 
        }
        if (filename.startsWith(FileSystemConstants.FILESLASH)) {
           return FileSystemType.LOCAL;
        }
        if (filename.startsWith(FileSystemConstants.HDFSSLASH)) {
           return FileSystemType.HDFS;
        }
        if (filename.startsWith(FileSystemConstants.SWIFTSLASH)) {
            return FileSystemType.SWIFT;
        }
        */
        return null;
    }
    
    public static String getFsPath(String filesystem) {
        String path = filesystem;
        int index = filesystem.indexOf(':');
        if (index >= 0) {
            path = path.substring(index + 1);
        }
        return path;
    }
    /*
    public void decide() {
        String filename = null;
        String file = filename;
        String prefix = "";
        // TODO redo this if system. make it oo.
        if (filename.startsWith(FileSystemConstants.FILESLASH) || filename.startsWith(FileSystemConstants.HDFSSLASH) || filename.startsWith(FileSystemConstants.SWIFTSLASH)) {
                int split;
                if (filename.startsWith(FileSystemConstants.SWIFT)) {
                        prefix = file.substring(0, FileSystemConstants.SWIFTLEN); // no double slash
                    file = file.substring(FileSystemConstants.SWIFTSLASHLEN);
                    split = file.indexOf("/");
                    this.node = file.substring(0, split);
                } else {
                prefix = file.substring(0, FileSystemConstants.FILELEN); // no double slash
            file = file.substring(FileSystemConstants.FILESLASHLEN);
            split = file.indexOf("/");
            this.node = file.substring(0, split);
                }
            if (this.node == null || this.node.length() == 0) {
                log.error("No nodename " + filename + " , " + file);
            }
            this.filename = prefix + file.substring(split);
        } else {
            this.node = csNodename;
        if (this.node == null || this.node.length() == 0) {
        log.error("No nodename " + filename + " , " + file);
        }       
    }
    */

    public static FileObject getFileObject(FileLocation fl) {
        Location lo = getLocation(fl.getNode());
        return new FileObject(lo, fl.getFilename());
    }

    public static FileObject getFileObject(String s) {
        s = transformOld(s);
        String[] list = s.split(":");
        int len = list.length;
        int objectIndex = StringUtils.ordinalIndexOf(s, ":", 3);        
        String path = s.substring(objectIndex + 1);
        String nodename = null;
        String fs = null;
        String extra = null;
        if (len > 3) {
            extra = list[2];
        }
        if (len > 2) {
            fs = list[1];
        }
        if (len > 1) {
            nodename = list[0];
        }
        Location lo = new Location(nodename, fs, extra);
        //lo = transformOld(lo);
        return new FileObject(lo, path);
    }
    
    public static Location getLocation(String s) {
        String[] list = s.split(":");
        int len = list.length;
        String nodename = null;
        String fs = null;
        String extra = null;
        if (len >= 3) {
            extra = list[2];
        }
        if (len >= 2) {
            fs = list[1];
        }
        if (len >= 1) {
            nodename = list[0];
        }
        return transformOld(new Location(nodename, fs, extra));
    }

    private static Location transformOld(Location location) {
        // deprecated
        if ("file".equals(location.nodename) && location.fs == null && location.extra == null) {
            location.nodename = null;
        }
        return location;
    }

    private static String transformOld(String s) {
        // deprecated
        if (s.startsWith("file:")) {
            return s.substring(5);
        }
        if (s.startsWith("file://localhost")) {
            return s.substring(16);
        }
        return s;
    }

    public static FileLocation getFileLocation(String s) {
        FileObject fo = getFileObject(s);
        return new FileLocation(fo.location.toString(), fo.object);
    }
}
