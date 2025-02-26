package roart.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.ErrorListenerPathable;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.WatchPathable;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

public class TestGetChildrenBuilder implements GetChildrenBuilder {

    @Override
    public BackgroundPathable<List<String>> watched() {
        return null;
    }

    @Override
    public BackgroundPathable<List<String>> usingWatcher(Watcher watcher) {
        return null;
    }

    @Override
    public BackgroundPathable<List<String>> usingWatcher(CuratorWatcher watcher) {
        return null;
    }

    @Override
    public ErrorListenerPathable<List<String>> inBackground() {
        return null;
    }

    @Override
    public ErrorListenerPathable<List<String>> inBackground(Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<List<String>> inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public ErrorListenerPathable<List<String>> inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<List<String>> inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public ErrorListenerPathable<List<String>> inBackground(BackgroundCallback callback, Object context,
            Executor executor) {
        return null;
    }

    @Override
    public List<String> forPath(String path) throws Exception {
        List<String> retFiles = new ArrayList<>();
        File[] files = new File("/tmp" + path).listFiles();
        for (File file : files) {
            retFiles.add(file.getName());
        }
        return retFiles;
    }

    @Override
    public WatchPathable<List<String>> storingStatIn(Stat stat) {
        return null;
    }

}
