package roart.iclij.config;

import java.util.ArrayList;
import java.util.List;

public class ComponentConstants {
    public static List<String> getSimulateInvestConfig() {
        List<String> confList = new ArrayList<>();
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCEVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCEFINDTIMES);
        confList.add(IclijConfigConstants.SIMULATEINVESTABOVEBELOW);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCEHOLDINCREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCEHOLDINCREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCETRENDINCREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTCONFIDENCETRENDINCREASETIMES);
        confList.add(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCETRENDDECREASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTNOCONFIDENCETRENDDECREASETIMES);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOPLOSS);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOPLOSSVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSS);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVALSTOPLOSSVALUE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORPURE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORREBASE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORREVERSE);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORDIRECTION);
        confList.add(IclijConfigConstants.SIMULATEINVESTINDICATORDIRECTIONUP);
        confList.add(IclijConfigConstants.SIMULATEINVESTMLDATE);
        confList.add(IclijConfigConstants.SIMULATEINVESTSTOCKS);
        confList.add(IclijConfigConstants.SIMULATEINVESTBUYWEIGHT);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERVAL);
        confList.add(IclijConfigConstants.SIMULATEINVESTINTERPOLATE);
        confList.add(IclijConfigConstants.SIMULATEINVESTADVISER);
        confList.add(IclijConfigConstants.SIMULATEINVESTPERIOD);
        confList.add(IclijConfigConstants.SIMULATEINVESTDAY);
        return confList;
    }

    public static List<String> getAutoSimulateInvestConfig() {
        List<String> confList = new ArrayList<>();
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTPERIOD);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTLASTCOUNT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTDELLIMIT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTSCORELIMIT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTAUTOSCORELIMIT);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISER);
        confList.add(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISERLIMIT);
        return confList;
    }
}
