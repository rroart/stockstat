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

public class MachineLearningActionData extends MarketActionData {

    public MachineLearningActionData(IclijConfig iclijConfig, IclijDbDao dbDao) {
        super(iclijConfig, dbDao);
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getPersisttime();
    }

    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { null };
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getMachineLearningThreshold();
    }

    @Override
    public String getName() {
        return IclijConstants.MACHINELEARNING;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsMachineLearningPredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsMachineLearningMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsMachineLearningMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsMachineLearningMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsMachineLearningMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsMachineLearningMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsMachineLearningMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsMachineLearningMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.MACHINELEARNING;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getMachineLearningFuturedays();
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
        return config.wantsMachineLearningUpdate();
    }

    @Override
	public String getEvolutionConfig(IclijConfig config) {
		return config.getMachineLearningEvolutionConfig();
	}

    @Override
	public String getMLConfig(IclijConfig config) {
		return config.getMachineLearningMLConfig();
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
