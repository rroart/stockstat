package roart.service;

import java.util.List;
import java.util.Set;

import roart.config.MyConfig;
import roart.model.GUISize;

public class ServiceParam {
    public MyConfig config;
    public GUISize guiSize;
    public Set<String> ids;
    public String market;
    public boolean wantMaps;
    public List<String> confList;
}
