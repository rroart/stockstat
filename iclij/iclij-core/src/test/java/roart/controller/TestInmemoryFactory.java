package roart.controller;

import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;

public class TestInmemoryFactory extends InmemoryFactory {

    @Override 
    public Inmemory get(String server, String hz, String redis) {
        return new TestInmemory(server);
    }
}
