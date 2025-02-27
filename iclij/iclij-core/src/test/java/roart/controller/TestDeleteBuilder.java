package roart.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.ChildrenDeletable;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.DeleteBuilderMain;
import org.apache.curator.framework.api.ErrorListenerPathable;

public class TestDeleteBuilder implements DeleteBuilder {

    @Override
    public DeleteBuilderMain quietly() {
        return null;
    }

    @Override
    public ChildrenDeletable guaranteed() {
        return null;
    }

    @Override
    public ErrorListenerPathable<Void> inBackground() {
        return null;
    }

    @Override
    public ErrorListenerPathable<Void> inBackground(Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Void> inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Void> inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Void> inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public ErrorListenerPathable<Void> inBackground(BackgroundCallback callback, Object context, Executor executor) {
        return null;
    }

    @Override
    public Void forPath(String path) throws Exception {
        Files.delete(Paths.get("/tmp" + path));
        return null;
    }

    @Override
    public BackgroundPathable<Void> withVersion(int version) {
        return null;
    }

    @Override
    public BackgroundVersionable deletingChildrenIfNeeded() {
        return null;
    }

    @Override
    public DeleteBuilderMain idempotent() {
        return null;
    }

}
