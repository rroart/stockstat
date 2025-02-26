package roart.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.ACLableExistBuilderMain;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.ErrorListenerPathable;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

public class TestExistsBuilder implements ExistsBuilder {

    @Override
    public BackgroundPathable<Stat> watched() {
        return null;
    }

    @Override
    public BackgroundPathable<Stat> usingWatcher(Watcher watcher) {
        return null;
    }

    @Override
    public BackgroundPathable<Stat> usingWatcher(CuratorWatcher watcher) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Stat> inBackground() {
        return null;
    }

    @Override
    public ErrorListenerPathable<Stat> inBackground(Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Stat> inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Stat> inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Stat> inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Stat> inBackground(BackgroundCallback callback, Object context, Executor executor) {
        return null;
    }

    @Override
    public Stat forPath(String path) throws Exception {
        return Files.exists(Paths.get("/tmp" + path)) ? new Stat() : null;
    }

    @Override
    public ACLableExistBuilderMain creatingParentsIfNeeded() {
        return null;
    }

    @Override
    public ACLableExistBuilderMain creatingParentContainersIfNeeded() {
        return null;
    }

}
