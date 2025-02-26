package roart.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.ErrorListenerPathable;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.GetDataWatchBackgroundStatable;
import org.apache.curator.framework.api.WatchPathable;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

public class TestGetDataBuilder implements GetDataBuilder {

    @Override
    public BackgroundPathable<byte[]> watched() {
        return null;
    }

    @Override
    public BackgroundPathable<byte[]> usingWatcher(Watcher watcher) {
        return null;
    }

    @Override
    public BackgroundPathable<byte[]> usingWatcher(CuratorWatcher watcher) {
        return null;
    }

    @Override
    public ErrorListenerPathable<byte[]> inBackground() {
        return null;
    }

    @Override
    public ErrorListenerPathable<byte[]> inBackground(Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<byte[]> inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public ErrorListenerPathable<byte[]> inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<byte[]> inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public ErrorListenerPathable<byte[]> inBackground(BackgroundCallback callback, Object context, Executor executor) {
        return null;
    }

    @Override
    public byte[] forPath(String path) throws Exception {
        return Files.readAllBytes(Paths.get("/tmp" + path));
    }

    @Override
    public WatchPathable<byte[]> storingStatIn(Stat stat) {
        return null;
    }

    @Override
    public GetDataWatchBackgroundStatable decompressed() {
        return null;
    }

}
