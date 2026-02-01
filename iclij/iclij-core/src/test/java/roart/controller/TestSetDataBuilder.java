package roart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathAndBytesable;
import org.apache.curator.framework.api.ErrorListenerPathAndBytesable;
import org.apache.curator.framework.api.GetDataWatchBackgroundStatable;
import org.apache.curator.framework.api.PathAndBytesable;
import org.apache.curator.framework.api.SetDataBackgroundVersionable;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.zookeeper.data.Stat;

public class TestSetDataBuilder implements SetDataBuilder {

    @Override
    public ErrorListenerPathAndBytesable<Stat> inBackground() {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<Stat> inBackground(Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<Stat> inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<Stat> inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<Stat> inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<Stat> inBackground(BackgroundCallback callback, Object context,
            Executor executor) {
        return null;
    }

    @Override
    public Stat forPath(String path, byte[] data) throws Exception {
        Files.createDirectories(Paths.get("/tmp" + path).getParent());
        try {
            Files.write(Paths.get("/tmp" + path), data);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public Stat forPath(String path) throws Exception {
        Files.createDirectories(Paths.get("/tmp" + path).getParent());
        try {
            Files.createFile(Paths.get("/tmp" + path));
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public BackgroundPathAndBytesable<Stat> withVersion(int version) {
        return null;
    }

    @Override
    public SetDataBackgroundVersionable compressed() {
        return null;
    }

    @Override
    public SetDataBackgroundVersionable uncompressed() {
        return null;
    }

    @Override
    public SetDataBuilder idempotent() {
        return null;
    }

}