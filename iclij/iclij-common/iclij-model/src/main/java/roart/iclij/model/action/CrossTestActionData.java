package roart.iclij.model.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class CrossTestActionData extends MarketActionData {

    public CrossTestActionData(IclijDbDao dbDao) {
        super(dbDao);
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getCrosstime();
    }

    @Override
    public Boolean[] getBooleans() {
        //return new Boolean[] { false, true };
        return new Boolean[] { null };
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getCrosstestThreshold();
    }

    @Override
    public String getName() {
        return IclijConstants.CROSSTEST;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsCrosstestPredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsCrosstestMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsCrosstestMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsCrosstestMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsCrosstestMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsCrosstestMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsCrosstestMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsCrosstestMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.CROSSTEST;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getCrosstestFuturedays();
    }

    @Override
    public String getParamDateFromConfig(Market market, List<String> stockDates) {
        String date = getMlDate(market, stockDates);
        if (date == null) {
            date = getMlDays(market, stockDates);
        }
        return date;
    }

    public String getMlDate(Market market, List<String> stockDates) {
        String date = market.getConfig().getMldate();
        if (date != null) {
            LocalDate adate = TimeUtil.getEqualBefore(stockDates, date);
            date = TimeUtil.convertDate2(adate);
        }
        return date;
    }

    public String getMlDays(Market market, List<String> stockDates) {
        Short days = market.getConfig().getMldays();
        if (days != null) {
            return stockDates.get(stockDates.size() - 1 - days);
        }
        return null;
    }

    @Override
    public boolean wantsUpdate(IclijConfig config) {
        return false;
    }
    
    @Override
	public boolean doHandleMLMeta() {
		return true;
	}
    
    @Override
    public Object[] getScoreDescription(Object[] accuracy, Map<String, Object> scoreMap) {
        Double score = null;
        String description = null;
        try {
            Object[] result = accuracy;
            score = (Double) result[0];
            description = (String) result[1];
            if (result[2] != null) {
                description =  (String) result[2] + " " + description;
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new Object[] { score, description };
    }
}
