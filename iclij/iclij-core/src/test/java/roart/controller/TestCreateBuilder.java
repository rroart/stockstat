package roart.controller;

import java.util.List;
import java.util.concurrent.Executor;

import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ACLCreateModeStatBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ACLPathAndBytesable;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathAndBytesable;
import org.apache.curator.framework.api.CreateBackgroundModeStatACLable;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CreateBuilder2;
import org.apache.curator.framework.api.CreateBuilderMain;
import org.apache.curator.framework.api.CreateProtectACLCreateModePathAndBytesable;
import org.apache.curator.framework.api.ErrorListenerPathAndBytesable;
import org.apache.curator.framework.api.ProtectACLCreateModeStatPathAndBytesable;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

public class TestCreateBuilder implements CreateBuilder {

    @Override
    public ProtectACLCreateModeStatPathAndBytesable<String> creatingParentsIfNeeded() {
        return new TestProtectACLCreateModeStatPathAndBytesable();
    }

    @Override
    public ProtectACLCreateModeStatPathAndBytesable<String> creatingParentContainersIfNeeded() {
        return null;
    }

    @Override
    public ACLPathAndBytesable<String> withProtectedEphemeralSequential() {
        return null;
    }

    @Override
    public ACLCreateModeStatBackgroundPathAndBytesable<String> withProtection() {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<String> inBackground() {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<String> inBackground(Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<String> inBackground(BackgroundCallback callback) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<String> inBackground(BackgroundCallback callback, Object context) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<String> inBackground(BackgroundCallback callback, Executor executor) {
        return null;
    }

    @Override
    public ErrorListenerPathAndBytesable<String> inBackground(BackgroundCallback callback, Object context,
            Executor executor) {
        return null;
    }

    @Override
    public String forPath(String path, byte[] data) throws Exception {
        return null;
    }

    @Override
    public String forPath(String path) throws Exception {
        return null;
    }

    @Override
    public ACLBackgroundPathAndBytesable<String> withMode(CreateMode mode) {
        return null;
    }

    @Override
    public BackgroundPathAndBytesable<String> withACL(List<ACL> aclList, boolean applyToParents) {
        return null;
    }

    @Override
    public BackgroundPathAndBytesable<String> withACL(List<ACL> aclList) {
        return null;
    }

    @Override
    public CreateBackgroundModeStatACLable compressed() {
        return null;
    }

    @Override
    public CreateProtectACLCreateModePathAndBytesable<String> storingStatIn(Stat stat) {
        return null;
    }

    @Override
    public CreateBuilder2 idempotent() {
        return null;
    }

    @Override
    public CreateBuilderMain withTtl(long ttl) {
        return null;
    }

    @Override
    public CreateBuilder2 orSetData() {
        return null;
    }

    @Override
    public CreateBuilder2 orSetData(int version) {
        return null;
    }

}
