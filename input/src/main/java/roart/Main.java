package roart;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import roart.db.model.HibernateUtil;
import roart.db.model.Meta;
import roart.db.model.Relation;
import roart.db.model.Stock;

public class Main {

    private static HibernateUtil hu = new HibernateUtil(null);

    public static void main(String[] argv) {
        try {
            File file = new File(argv[0]);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            handleRelation(doc);
            handleMeta(doc);
            NodeList nl = handleStock(doc); 
            hu.commit();
            System.out.println("Added for length " + nl.getLength());
        } catch (Exception e) {
            System.out.println("Exception " +e);
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void print(String json, int start) {
        System.out.println(json.length() + " " + json.substring(start + 0, start + 200));
    }

    private static NodeList handleStock(Document doc) throws Exception, ParseException {
        NodeList nl = doc.getElementsByTagName(Constants.ROW);
        for (int i = 0 ; i < nl.getLength(); i++ ) {
            Node node = nl.item(i);
            Element elem = (Element) node;
            Element idElem = (Element) elem.getElementsByTagName(Constants.ID).item(0);
            Element isinElem = (Element) elem.getElementsByTagName(Constants.ISIN).item(0);
            Element marketidElem = (Element) elem.getElementsByTagName(Constants.MARKETID).item(0);
            Element nameElem = (Element) elem.getElementsByTagName(Constants.NAME).item(0);
            Element dateElem = (Element) elem.getElementsByTagName(Constants.DATE).item(0);
            Element indexvalueElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUE).item(0);
            Element indexvaluelowElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUELOW).item(0);
            Element indexvaluehighElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUEHIGH).item(0);
            Element indexvalueopenElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUEOPEN).item(0);
            Element priceElem = (Element) elem.getElementsByTagName(Constants.PRICE).item(0);
            Element pricelowElem = (Element) elem.getElementsByTagName(Constants.PRICELOW).item(0);
            Element pricehighElem = (Element) elem.getElementsByTagName(Constants.PRICEHIGH).item(0);
            Element priceopenElem = (Element) elem.getElementsByTagName(Constants.PRICEOPEN).item(0);
            Element currElem = (Element) elem.getElementsByTagName(Constants.CURRENCY).item(0);
            Element volumeElem = (Element) elem.getElementsByTagName(Constants.VOLUME).item(0);
            Element period1Elem = (Element) elem.getElementsByTagName(Constants.PERIOD1).item(0);
            Element period2Elem = (Element) elem.getElementsByTagName(Constants.PERIOD2).item(0);
            Element period3Elem = (Element) elem.getElementsByTagName(Constants.PERIOD3).item(0);
            Element period4Elem = (Element) elem.getElementsByTagName(Constants.PERIOD4).item(0);
            Element period5Elem = (Element) elem.getElementsByTagName(Constants.PERIOD5).item(0);
            Element period6Elem = (Element) elem.getElementsByTagName(Constants.PERIOD6).item(0);
            Element period7Elem = (Element) elem.getElementsByTagName(Constants.PERIOD7).item(0);
            Element period8Elem = (Element) elem.getElementsByTagName(Constants.PERIOD8).item(0);
            Element period9Elem = (Element) elem.getElementsByTagName(Constants.PERIOD9).item(0);
            String id = idElem.getTextContent();
            if (id == null || id.isEmpty()) {
                continue;
            }
            String isin = null;
            if (isinElem != null) {
                isin = isinElem.getTextContent();
            }
            String marketid = marketidElem.getTextContent();
            String name = nameElem.getTextContent();
            String datestr = dateElem.getTextContent();
            String indexvalue = null;
            if (indexvalueElem != null) {
                indexvalue = reformat(indexvalueElem.getTextContent());
            }
            String indexvaluelow = null;
            if (indexvaluelowElem != null) {
                indexvaluelow = reformat(indexvaluelowElem.getTextContent());
            }
            String indexvaluehigh = null;
            if (indexvaluehighElem != null) {
                indexvaluehigh = reformat(indexvaluehighElem.getTextContent());
            }
            String indexvalueopen = null;
            if (indexvalueopenElem != null) {
                indexvalueopen = reformat(indexvalueopenElem.getTextContent());
            }
            String price = null;
            if (priceElem != null) {
                price = reformat(priceElem.getTextContent());
            }
            String pricelow = null;
            if (pricelowElem != null) {
                pricelow = reformat(pricelowElem.getTextContent());
            }
            String pricehigh = null;
            if (pricehighElem != null) {
                pricehigh = reformat(pricehighElem.getTextContent());
            }
            String priceopen = null;
            if (priceopenElem != null) {
                priceopen = reformat(priceopenElem.getTextContent());
            }
            String currency = null;
            if (currElem != null) {
                currency = currElem.getTextContent();
            }
            String volume = null;
            if (volumeElem != null) {
                volume = reformat(volumeElem.getTextContent());
            }
            String period1 = null;
            if (period1Elem != null) {
                period1 = reformat(period1Elem.getTextContent());
            }
            String period2 = null;
            if (period2Elem != null) {
                period2 = reformat(period2Elem.getTextContent());
            }
            String period3 = null;
            if (period3Elem != null) {
                period3 = reformat(period3Elem.getTextContent());
            }
            String period4 = null;
            if (period4Elem != null) {
                period4 = reformat(period4Elem.getTextContent());
            }
            String period5 = null;
            if (period5Elem != null) {
                period5 = reformat(period5Elem.getTextContent());
            }
            String period6 = null;
            if (period6Elem != null) {
                period6 = reformat(period6Elem.getTextContent());
            }
            String period7 = null;
            if (period7Elem != null) {
                period7 = reformat(period7Elem.getTextContent());
            }
            String period8 = null;
            if (period8Elem != null) {
                period8 = reformat(period8Elem.getTextContent());
            }
            String period9 = null;
            if (period9Elem != null) {
                period9 = reformat(period9Elem.getTextContent());
            }
            
            String adatestr = datestr;
            Date date;
            SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy"); 
            if (datestr.length() == 13 && datestr.matches("-?\\d+")) {
                date = new Date(Long.valueOf(datestr));
                date.setHours(0);
                date.setMinutes(0);
                date.setSeconds(0);
                adatestr = dt.format(date);
            } else {
                date = dt.parse(datestr);
            }

            String dbid = marketid + "_" + id + "_" + adatestr;
            Stock stock = Stock.ensureExistence(dbid, hu);
            stock.setId(id);
            stock.setIsin(isin);
            stock.setMarketid(marketid);
            stock.setName(name);
            stock.setDate(date);
            if (indexvalue == null || indexvalue.equals("-")) {
                stock.setIndexvalue(null);
            } else {
                stock.setIndexvalue(Double.valueOf(indexvalue));
            }
            if (indexvaluelow == null || indexvaluelow.equals("-")) {
                stock.setIndexvaluelow(null);
            } else {
                stock.setIndexvaluelow(Double.valueOf(indexvaluelow));
            }
            if (indexvaluehigh == null || indexvaluehigh.equals("-")) {
                stock.setIndexvaluehigh(null);
            } else {
                stock.setIndexvaluehigh(Double.valueOf(indexvaluehigh));
            }
            if (indexvalueopen == null || indexvalueopen.equals("-")) {
                stock.setIndexvalueopen(null);
            } else {
                stock.setIndexvalueopen(Double.valueOf(indexvalueopen));
            }
            if (volume == null || volume.equals("-")) {
                stock.setVolume(null);
            } else {
                stock.setVolume(new Long(volume));
            }
            stock.setCurrency(currency);
            if (price == null || price.equals("-")) {
                stock.setPrice(null);
            } else {
                stock.setPrice(Double.valueOf(price));
            }
            if (pricelow == null || pricelow.equals("-")) {
                stock.setPricelow(null);
            } else {
                stock.setPricelow(Double.valueOf(pricelow));
            }
            if (pricehigh == null || pricehigh.equals("-")) {
                stock.setPricehigh(null);
            } else {
                stock.setPricehigh(Double.valueOf(pricehigh));
            }
            if (priceopen == null || priceopen.equals("-")) {
                stock.setPriceopen(null);
            } else {
                stock.setPriceopen(Double.valueOf(priceopen));
            }
            if (period1 == null || period1.equals("-")) {
                stock.setPeriod1(null);
            } else {
                stock.setPeriod1(Double.valueOf(period1));
            }
            if (period2 == null || period2.equals("-")) {
                stock.setPeriod2(null);
            } else {
                stock.setPeriod2(Double.valueOf(period2));
            }
            if (period3 == null || period3.equals("-")) {
                stock.setPeriod3(null);
            } else {
                stock.setPeriod3(Double.valueOf(period3));
            }
            if (period4 == null || period4.equals("-")) {
                stock.setPeriod4(null);
            } else {
                stock.setPeriod4(Double.valueOf(period4));
            }
            if (period5 == null || period5.equals("-")) {
                stock.setPeriod5(null);
            } else {
                stock.setPeriod5(Double.valueOf(period5));
            }
            if (period6 == null || period6.equals("-")) {
                stock.setPeriod6(null);
            } else {
                stock.setPeriod6(Double.valueOf(period6));
            }
            if (period7 == null || period7.equals("-")) {
                stock.setPeriod7(null);
            } else {
                stock.setPeriod7(Double.valueOf(period7));
            }
            if (period8 == null || period8.equals("-")) {
                stock.setPeriod8(null);
            } else {
                stock.setPeriod8(Double.valueOf(period8));
            }
            if (period9 == null || period9.equals("-")) {
                stock.setPeriod9(null);
            } else {
                stock.setPeriod9(Double.valueOf(period9));
            }
            //String  = Elem.getTextContent();
            //Element Elem = elem.getElementsByTagName();
        }
        return nl;
    }

    private static void handleRelation(Document doc) throws Exception {
        NodeList nlmeta = doc.getElementsByTagName(Constants.RELATIONROW);
        for (int i = 0 ; i < nlmeta.getLength(); i++ ) {
            Node node = nlmeta.item(i);
            Element elem = (Element) node;
            Element altidElem = (Element) elem.getElementsByTagName(Constants.ALTID).item(0);
            Element idElem = (Element) elem.getElementsByTagName(Constants.ID).item(0);
            Element marketElem = (Element) elem.getElementsByTagName(Constants.MARKET).item(0);
            Element otheraltidElem = (Element) elem.getElementsByTagName(Constants.OTHERALTID).item(0);
            Element otheridElem = (Element) elem.getElementsByTagName(Constants.OTHERID).item(0);
            Element othermarketElem = (Element) elem.getElementsByTagName(Constants.OTHERMARKET).item(0);
            Element typeElem = (Element) elem.getElementsByTagName(Constants.TYPE).item(0);
            Element valueElem = (Element) elem.getElementsByTagName(Constants.VALUE).item(0);
            String altid = null;
            if (altidElem != null) {
                altid = reformat(altidElem.getTextContent());
            }
            String id = null;
            if (idElem != null) {
                id = reformat(idElem.getTextContent());
            }
            String market = null;
            if (marketElem != null) {
                market = reformat(marketElem.getTextContent());
            }
            String otheraltid = null;
            if (otheraltidElem != null) {
                otheraltid = reformat(otheraltidElem.getTextContent());
            }
            String otherid = null;
            if (otheridElem != null) {
                otherid = reformat(otheridElem.getTextContent());
            }
            String othermarket = null;
            if (othermarketElem != null) {
                othermarket = reformat(othermarketElem.getTextContent());
            }
            String type = null;
            if (typeElem != null) {
                type = reformat(typeElem.getTextContent());
            }
            String value = null;
            if (valueElem != null) {
                value = reformat(valueElem.getTextContent());
            }
            Relation relation = Relation.ensureExistence(hu);
            if (altid == null || altid.equals("-")) {
                relation.setAltId(null);
            } else {
                relation.setAltId(new String(altid));
            }
            if (id == null || id.equals("-")) {
                relation.setId(null);
            } else {
                relation.setId(new String(id));
            }
            if (market == null || market.equals("-")) {
                relation.setMarket(null);
            } else {
                relation.setMarket(new String(market));
            }
            if (otheraltid == null || otheraltid.equals("-")) {
                relation.setOtherAltId(null);
            } else {
                relation.setOtherAltId(new String(otheraltid));
            }
            if (otherid == null || otherid.equals("-")) {
                relation.setOtherId(null);
            } else {
                relation.setOtherId(new String(otherid));
            }
            if (othermarket == null || othermarket.equals("-")) {
                relation.setOtherMarket(null);
            } else {
                relation.setOtherMarket(new String(othermarket));
            }
            relation.setRecord(LocalDate.now());
            if (type == null || type.equals("-")) {
                relation.setType(null);
            } else {
                relation.setType(new String(type));
            }
            if (value == null || value.equals("-")) {
                relation.setValue(null);
            } else {
                relation.setValue(Double.valueOf(value));
            }

        }
    }

    private static void handleMeta(Document doc) throws Exception {
        NodeList nlmeta = doc.getElementsByTagName(Constants.META);
        for (int i = 0 ; i < nlmeta.getLength(); i++ ) {
            Node node = nlmeta.item(i);
            Element elem = (Element) node;
            Element marketidElem = (Element) elem.getElementsByTagName(Constants.MARKETID).item(0);
            Element period1Elem = (Element) elem.getElementsByTagName(Constants.PERIOD1).item(0);
            Element period2Elem = (Element) elem.getElementsByTagName(Constants.PERIOD2).item(0);
            Element period3Elem = (Element) elem.getElementsByTagName(Constants.PERIOD3).item(0);
            Element period4Elem = (Element) elem.getElementsByTagName(Constants.PERIOD4).item(0);
            Element period5Elem = (Element) elem.getElementsByTagName(Constants.PERIOD5).item(0);
            Element period6Elem = (Element) elem.getElementsByTagName(Constants.PERIOD6).item(0);
            Element period7Elem = (Element) elem.getElementsByTagName(Constants.PERIOD7).item(0);
            Element period8Elem = (Element) elem.getElementsByTagName(Constants.PERIOD8).item(0);
            Element period9Elem = (Element) elem.getElementsByTagName(Constants.PERIOD9).item(0);
            Element orderElem = (Element) elem.getElementsByTagName(Constants.PRIORITY).item(0);
            Element resetElem = (Element) elem.getElementsByTagName(Constants.RESET).item(0);
            Element lhcElem = (Element) elem.getElementsByTagName(Constants.LHC).item(0);
            String marketid = marketidElem.getTextContent();
            String period1 = null;
            if (period1Elem != null) {
                period1 = reformat(period1Elem.getTextContent());
            }
            String period2 = null;
            if (period2Elem != null) {
                period2 = reformat(period2Elem.getTextContent());
            }
            String period3 = null;
            if (period3Elem != null) {
                period3 = reformat(period3Elem.getTextContent());
            }
            String period4 = null;
            if (period4Elem != null) {
                period4 = reformat(period4Elem.getTextContent());
            }
            String period5 = null;
            if (period5Elem != null) {
                period5 = reformat(period5Elem.getTextContent());
            }
            String period6 = null;
            if (period6Elem != null) {
                period6 = reformat(period6Elem.getTextContent());
            }
            String period7 = null;
            if (period7Elem != null) {
                period7 = reformat(period7Elem.getTextContent());
            }
            String period8 = null;
            if (period8Elem != null) {
                period8 = reformat(period8Elem.getTextContent());
            }
            String period9 = null;
            if (period9Elem != null) {
                period9 = reformat(period9Elem.getTextContent());
            }
            String order = null;
            if (orderElem != null) {
                order = reformat(orderElem.getTextContent());
            }
            String reset = null;
            if (resetElem != null) {
                reset = reformat(resetElem.getTextContent());
            }
            String lhc = null;
            if (lhcElem != null) {
                lhc = reformat(lhcElem.getTextContent());
            }            
            Meta meta = Meta.ensureExistence(marketid, hu);
            if (period1 == null || period1.equals("-")) {
                meta.setPeriod1(null);
            } else {
                meta.setPeriod1(new String(period1));
            }
            if (period2 == null || period2.equals("-")) {
                meta.setPeriod2(null);
            } else {
                meta.setPeriod2(new String(period2));
            }
            if (period3 == null || period3.equals("-")) {
                meta.setPeriod3(null);
            } else {
                meta.setPeriod3(new String(period3));
            }
            if (period4 == null || period4.equals("-")) {
                meta.setPeriod4(null);
            } else {
                meta.setPeriod4(new String(period4));
            }
            if (period5 == null || period5.equals("-")) {
                meta.setPeriod5(null);
            } else {
                meta.setPeriod5(new String(period5));
            }
            if (period6 == null || period6.equals("-")) {
                meta.setPeriod6(null);
            } else {
                meta.setPeriod6(new String(period6));
            }
            if (period7 == null || period7.equals("-")) {
                meta.setPeriod7(null);
            } else {
                meta.setPeriod7(new String(period7));
            }
            if (period8 == null || period8.equals("-")) {
                meta.setPeriod8(null);
            } else {
                meta.setPeriod8(new String(period8));
            }
            if (period9 == null || period9.equals("-")) {
                meta.setPeriod9(null);
            } else {
                meta.setPeriod9(new String(period9));
            }
            if (order == null) {
                meta.setPriority(null);
            } else {
                meta.setPriority(new String(order));
            }
            if (reset == null) {
                meta.setReset(null);
            } else {
                meta.setReset(new String(reset));
            }
            if (lhc == null) {
                meta.setLhc(false);
            } else {
                meta.setLhc(Boolean.valueOf(lhc));
            }

        }
    }

    static String reformat(String str) {
        if (str != null) {
            str = str.replace(",", ".").replace(" ", "");
        }
        return str;
    }

}
