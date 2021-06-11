package roart.iclij.model.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class CrossTestActionData extends MarketActionData {

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
    protected boolean wantsUpdate(IclijConfig config) {
        return false;
    }
}
