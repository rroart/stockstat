package roart.filesystem.swift;

import org.junit.jupiter.api.Test;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.FileSystemConstants;
import roart.common.filesystem.FileSystemBooleanResult;
import roart.common.filesystem.FileSystemFileObjectParam;
import roart.common.filesystem.FileSystemFileObjectResult;
import roart.common.filesystem.FileSystemParam;
import roart.common.filesystem.FileSystemPathParam;
import roart.common.model.FileObject;
import roart.common.model.Location;
import roart.filesystem.swift.Swift;
import roart.filesystem.swift.SwiftConfig;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.junit.jupiter.api.BeforeEach;
//import roart.common.util.FsUtil;

public class SwiftIT {
    
    private SwiftConfig conf;
    private Swift swift;
    
   @BeforeEach
    public void setup() {
       try {
       conf = new SwiftConfig();
       String url = "http://172.17.0.2:8080/auth/v1.0";
       String username = "test:tester";
       String password = "doowimihiree";
       //log.info("INFO " + url + " " + username + "  " + password);
       if (url != null) {
           AccountConfig config;
           config = new AccountConfig();
           config.setUsername( username);
           config.setPassword(password);
           config.setAuthUrl(url);
           config.setAuthenticationMethod(AuthenticationMethod.BASIC);
           Account account = new AccountFactory(config).createAccount();
           conf.account = account;
           swift = new Swift(null, null, null);
           swift.conf = conf;
           //log.info("here");
       }
   } catch (Exception e) {
       //log.error("Exception", e);
       //return null;
   }
        
    }
    
    @Test
    public void test() {
        FileSystemPathParam paramp = new FileSystemPathParam();
        getParamConf(paramp);
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        getParamConf(param);
        paramp.path = null; // FsUtil.getFileObject(":swift:chess:xiangqi");
        param.fo = new FileObject(new Location(null, FileSystemConstants.S3TYPE, "chess"), "xiangqi");
        FileSystemFileObjectResult get = swift.get(paramp);
        System.out.println("r " + get.getFileObject());
        param.fo = get.getFileObject()[0];
        FileSystemBooleanResult ex = swift.exists(param);
        System.out.println("Ex " + ex.bool);
    }
    
    @Test
    public void test2() {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        FileSystemPathParam paramp = new FileSystemPathParam();
        getParamConf(param);
        getParamConf(paramp);
        paramp.path = null; // FsUtil.getFileObject(":swift:chess:.");
        FileSystemFileObjectResult res;
        FileSystemFileObjectResult resp;
        resp = swift.get(paramp);
        param.fo = resp.getFileObject()[0];
        res = swift.listFiles(param);
        if (res != null) {
        System.out.println("l1 " + res.getFileObject().length);
        }
        paramp.path = null; // FsUtil.getFileObject(":swift:chess:/");
        resp = swift.get(paramp);
        //param.fo = new FileObject("/");
        param.fo = resp.getFileObject()[0];
        res = swift.listFiles(param);
        if (res != null) {
        System.out.println("l2 " + res.getFileObject().length + " " + res.getFileObject()[0].object);
        }
        paramp.path = null; // FsUtil.getFileObject(":swift:chess:xiangqi");
        resp = swift.get(paramp);
        //param.fo = new FileObject("xiangqi");
        param.fo = resp.getFileObject()[0];
        res = swift.listFiles(param);
        if (res != null) {
        System.out.println("l3 " + res.getFileObject().length);
        }
        paramp.path = null; // FsUtil.getFileObject(":swift:chess:/xiangqi/");
        resp = swift.get(paramp);
        //param.fo = new FileObject("xiangqi");
        param.fo = resp.getFileObject()[0];
        res = swift.listFiles(param);
        if (res != null) {
        System.out.println("l4 " + res.getFileObject().length+ " " + res.getFileObject()[0].object);
        }
    }

    private void getParamConf(FileSystemParam param) {
        //param.conf = new MyMyConfig();
        //param.conf.setConfigValueMap(new HashMap<>());
        //param.conf.getConfigValueMap().put(ConfigConstants.FILESYSTEMSWIFTSWIFTCONFCONTAINER, "chess");
    }
}
