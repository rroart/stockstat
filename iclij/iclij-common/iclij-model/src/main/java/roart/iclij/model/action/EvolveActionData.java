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

public class EvolveActionData extends MarketActionData {

    public EvolveActionData(IclijConfig iclijConfig) {
        super(iclijConfig);
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getEvolvetime();
    }

    @Override
    public Boolean[] getBooleans() {
        //return new Boolean[] { false, true };
        return new Boolean[] { null };
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getEvolveThreshold();
    }
    
    @Override
    public String getName() {
        return IclijConstants.EVOLVE;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsEvolveRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsEvolvePredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsEvolveMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsEvolveMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsEvolveMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsEvolveMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsEvolveMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsEvolveMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsEvolveMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.EVOLVE;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getEvolveFuturedays();
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
        log.info("scorelist {}", scoreList);
        if (scoreList != null) {
        	score = scoreList
        			.stream()
        			.mapToDouble(e -> (Double) e)
        			.max()
        			.orElse(-2);
        	if (scoreList.size() > 1) {
        		description = scoreList.stream().mapToDouble(e -> (Double) e).filter(e -> e >= 0).summaryStatistics().toString();
        	}
        	if (score == -1) {
        		score = null;
        		description = "Interrupted";
        	}
        }
        return new Object[] { score, description };
    }
    /*
     * java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Double (java.lang.String and java.lang.Double are in module java.base of loader 'bootstrap')

     */
}
