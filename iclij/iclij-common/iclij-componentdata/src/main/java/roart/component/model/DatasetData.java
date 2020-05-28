package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import roart.iclij.config.Market;
import roart.iclij.model.action.MarketActionData;

public class DatasetData extends ComponentMLData {

    public DatasetData(ComponentData componentparam) {
        super(componentparam);
    }

    @Override
    public int setDates(String aDate, List<String> stockdates, MarketActionData actionData, Market market) throws ParseException {
        this.setFutureDate(LocalDate.now());
        return getOffset();
    }
         
}
