package roart.iclij.model.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class ImproveProfitActionData extends MarketActionData {

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getImprovetime();
    }
    
    @Override
    public Boolean[] getBooleans() {
        // return new Boolean[] { false, true };
        return new Boolean[] { null };
    }
    
    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getFindProfitThreshold();
    }

    @Override
    public String getName() {
        return IclijConstants.IMPROVEPROFIT;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsImproveProfitRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsImproveProfitPredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsImproveProfitMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsImproveProfitMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsImproveProfitMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsImproveProfitMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsImproveProfitMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsImproveProfitMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
       if (config.wantsImproveProfitMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }
    
    @Override
    public String getPriority() {
        return IclijConfigConstants.IMPROVEPROFIT;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
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
        return config.wantsImproveProfitUpdate();
    }

    @Override
	public String getEvolutionConfig(IclijConfig config) {
		return config.getImproveProfitEvolutionConfig();
	}

    @Override
	public String getMLConfig(IclijConfig config) {
		return config.getImproveProfitMLConfig();
	}
    
    @Override
	public boolean doHandleMLMeta() {
		return true;
	}
	
    @Override
	public boolean isEvolving() {
		return true;
	}
	
    @Override
	public boolean doSaveTiming() {
		return false;
	}
    
    @Override
    public Object[] getScoreDescription(Object[] accuracy, Map<String, Object> scoreMap) {
        Double score = null;
        String description = null;
        List<Object> scoreList = ((List<Object>) scoreMap.get("scores"));
        score = scoreList
                .stream()
                .mapToDouble(e -> (Double) e)
                .max()
                .orElse(-2);
        if (scoreMap.size() > 1) {
            description = scoreList.stream().mapToDouble(e -> (Double) e).filter(e -> e >= 0).summaryStatistics().toString();
        }
        if (score == -1) {
        	score = null;
        	description = "Interrupted";
        }
        return new Object[] { score, description };
    }
}
