package roart.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.ACLCreateModeBackgroundPathAndBytesable;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.ProtectACLCreateModeStatPathAndBytesable;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class TestProtectACLCreateModeStatPathAndBytesable implements ProtectACLCreateModeStatPathAndBytesable {

    @Override
    public ACLCreateModeBackgroundPathAndBytesable withProtection() {
        return null;
    }

    @Override
    public Object withACL(List aclList, boolean applyToParents) {
        return null;
    }

    @Override
    public Object withACL(List aclList) {
        return null;
    }

    @Override
    public Object inBackground() {
        return null;
    }

    @Override
    public Object inBackground(Object context) {
        return null;
    }

    @Override
    public Object inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public Object inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public Object inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public Object inBackground(BackgroundCallback callback, Object context, Executor executor) {
        return null;
    }

    @Override
    public Object forPath(String path, byte[] data) throws Exception {
        Files.createDirectories(Paths.get("/tmp" + path).getParent());
        Files.write(Paths.get("/tmp" + path), data);
        return null;
    }

    @Override
    public Object forPath(String path) throws Exception {
        return null;
    }

    @Override
    public Object withMode(CreateMode mode) {
        return null;
    }

    @Override
    public Object storingStatIn(Stat stat) {
        return null;
    }

}
