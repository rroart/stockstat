package roart;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import roart.model.Stock;
import roart.model.HibernateUtil;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String argv[]) {
    	try {
	File file = new File(argv[0]);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	dbFactory.setValidating(false);
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(file);
	doc.getDocumentElement().normalize();
	NodeList nl = doc.getElementsByTagName("row");
	for (int i = 0 ; i < nl.getLength(); i++ ) {
	    Node node = nl.item(i);
	    Element elem = (Element) node;
	    Element idElem = (Element) elem.getElementsByTagName(Constants.ID).item(0);
	    Element nameElem = (Element) elem.getElementsByTagName(Constants.NAME).item(0);
	    Element dateElem = (Element) elem.getElementsByTagName(Constants.DATE).item(0);
	    Element priceElem = (Element) elem.getElementsByTagName(Constants.PRICE).item(0);
	    Element currElem = (Element) elem.getElementsByTagName(Constants.CURRENCY).item(0);
	    Element dayElem = (Element) elem.getElementsByTagName(Constants.DAY).item(0);
	    Element weekElem = (Element) elem.getElementsByTagName(Constants.WEEK).item(0);
	    Element monthElem = (Element) elem.getElementsByTagName(Constants.MONTH).item(0);
	    Element thisyearElem = (Element) elem.getElementsByTagName(Constants.THISYEAR).item(0);
	    String id = idElem.getTextContent();
	    if (id == null || id.isEmpty()) {
	    	continue;
	    }
	    String name = nameElem.getTextContent();
	    String datestr = dateElem.getTextContent();
	    String price = priceElem.getTextContent().replace(",", ".").replace(" ", "");
	    String currency = currElem.getTextContent();
	    String day = dayElem.getTextContent().replace(",", ".").replace(" ", "");
	    String week = weekElem.getTextContent().replace(",", ".").replace(" ", "");
	    String month = monthElem.getTextContent().replace(",", ".").replace(" ", "");
	    String thisyear = thisyearElem.getTextContent().replace(",", ".").replace(" ", "");
	    String dbid = id + "_" + datestr;
	    Stock stock = Stock.ensureExistence(dbid);
	    stock.setId(id);
	    stock.setName(name);
	    SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy"); 
	    Date date = dt.parse(datestr); 
	    stock.setDate(date);
	    stock.setCurrency(currency);
	    if (price.equals("-")) {
	    	stock.setPrice(null);
	    } else {
	    	stock.setPrice(new Double(price));
	    }
	    if (day.equals("-")) {
	    	stock.setDay(null);
	    } else {
	    	stock.setDay(new Double(day));
	    }
	    if (week.equals("-")) {
	    	stock.setWeek(null);
	    } else {
	    	stock.setWeek(new Double(week));
	    }
	    if (month.equals("-")) {
	    	stock.setMonth(null);
	    } else {
	    	stock.setMonth(new Double(month));
	    }
	    if (thisyear.equals("-")) {
	    	stock.setThisyear(null);
	    } else {
	    	stock.setThisyear(new Double(thisyear));
	    }
	    //String  = Elem.getTextContent();
	    //Element Elem = elem.getElementsByTagName();
	} 
		HibernateUtil.commit();
    	} catch (Exception e) {
    		System.out.println("Exception " +e);
    		e.printStackTrace();
    	}
    	System.exit(0);
    }

}
