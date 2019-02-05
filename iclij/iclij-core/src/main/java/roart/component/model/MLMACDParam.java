package roart.component.model;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import roart.result.model.ResultMeta;
import roart.service.ControlService;

public class MLMACDParam extends ComponentParam {
    private int daysafterzero;
    
    private Map<String, List<Object>> resultMap;
    
    private List<List> resultMetaArray;
    
    private List<ResultMeta> resultMeta;
    
    private int offset;
    
    public int getDaysafterzero() {
        return daysafterzero;
    }

    public void setDaysafterzero(int daysafterzero) {
        this.daysafterzero = daysafterzero;
    }

    public Map<String, List<Object>> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, List<Object>> resultMap) {
        this.resultMap = resultMap;
    }

    public List<List> getResultMetaArray() {
        return resultMetaArray;
    }

    public void setResultMetaArray(List<List> resultMetaArray) {
        this.resultMetaArray = resultMetaArray;
    }

    public List<ResultMeta> getResultMeta() {
        return resultMeta;
    }

    public void setResultMeta(List<ResultMeta> resultMeta) {
        this.resultMeta = resultMeta;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setDatesAndOffset(ControlService srv, int daysafterzero, Integer offset, String aDate) throws ParseException {
        this.offset = setDates(srv, daysafterzero, offset, aDate);
    }
}
