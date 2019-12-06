package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;

public class DatasetData extends ComponentMLData {

    public DatasetData(ComponentData componentparam) {
        super(componentparam);
    }

    @Override
    public int setDates(int futuredaysNot, Integer offsetNot, String aDate) throws ParseException {
        this.setFutureDate(LocalDate.now());
        return getOffset();
    }
         
}
