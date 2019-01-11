package roart;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import roart.db.model.HibernateUtil;
import roart.db.model.Meta;
import roart.db.model.Stock;

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
	handleMeta(doc);
	NodeList nl = doc.getElementsByTagName(Constants.ROW);
	for (int i = 0 ; i < nl.getLength(); i++ ) {
	    Node node = nl.item(i);
	    Element elem = (Element) node;
	    Element idElem = (Element) elem.getElementsByTagName(Constants.ID).item(0);
        Element marketidElem = (Element) elem.getElementsByTagName(Constants.MARKETID).item(0);
	    Element nameElem = (Element) elem.getElementsByTagName(Constants.NAME).item(0);
	    Element dateElem = (Element) elem.getElementsByTagName(Constants.DATE).item(0);
	    Element indexvalueElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUE).item(0);
        Element indexvaluelowElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUELOW).item(0);
        Element indexvaluehighElem = (Element) elem.getElementsByTagName(Constants.INDEXVALUEHIGH).item(0);
	    Element priceElem = (Element) elem.getElementsByTagName(Constants.PRICE).item(0);
        Element pricelowElem = (Element) elem.getElementsByTagName(Constants.PRICELOW).item(0);
        Element pricehighElem = (Element) elem.getElementsByTagName(Constants.PRICEHIGH).item(0);
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
	    String dbid = marketid + "_" + id + "_" + datestr;
	    Stock stock = Stock.ensureExistence(dbid);
	    stock.setId(id);
	    stock.setMarketid(marketid);
	    stock.setName(name);
	    SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy"); 
	    Date date = dt.parse(datestr); 
	    stock.setDate(date);
	    if (indexvalue == null || indexvalue.equals("-")) {
	    	stock.setIndexvalue(null);
	    } else {
	    	stock.setIndexvalue(new Double(indexvalue));
	    }
        if (indexvaluelow == null || indexvaluelow.equals("-")) {
            stock.setIndexvaluelow(null);
        } else {
            stock.setIndexvaluelow(new Double(indexvaluelow));
        }
        if (indexvaluehigh == null || indexvaluehigh.equals("-")) {
            stock.setIndexvaluehigh(null);
        } else {
            stock.setIndexvaluehigh(new Double(indexvaluehigh));
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
	    	stock.setPrice(new Double(price));
	    }
        if (pricelow == null || pricelow.equals("-")) {
            stock.setPricelow(null);
        } else {
            stock.setPricelow(new Double(pricelow));
        }
        if (pricehigh == null || pricehigh.equals("-")) {
            stock.setPricehigh(null);
        } else {
            stock.setPricehigh(new Double(pricehigh));
        }
	    if (period1 == null || period1.equals("-")) {
	    	stock.setPeriod1(null);
	    } else {
	    	stock.setPeriod1(new Double(period1));
	    }
	    if (period2 == null || period2.equals("-")) {
	    	stock.setPeriod2(null);
	    } else {
	    	stock.setPeriod2(new Double(period2));
	    }
	    if (period3 == null || period3.equals("-")) {
	    	stock.setPeriod3(null);
	    } else {
	    	stock.setPeriod3(new Double(period3));
	    }
	    if (period4 == null || period4.equals("-")) {
	    	stock.setPeriod4(null);
	    } else {
	    	stock.setPeriod4(new Double(period4));
	    }
        if (period5 == null || period5.equals("-")) {
            stock.setPeriod5(null);
        } else {
            stock.setPeriod5(new Double(period5));
        }
        if (period6 == null || period6.equals("-")) {
            stock.setPeriod6(null);
        } else {
            stock.setPeriod6(new Double(period6));
        }
        if (period7 == null || period7.equals("-")) {
            stock.setPeriod7(null);
        } else {
            stock.setPeriod7(new Double(period7));
        }
        if (period8 == null || period8.equals("-")) {
            stock.setPeriod8(null);
        } else {
            stock.setPeriod8(new Double(period8));
        }
        if (period9 == null || period9.equals("-")) {
            stock.setPeriod9(null);
        } else {
            stock.setPeriod9(new Double(period9));
        }
	    //String  = Elem.getTextContent();
	    //Element Elem = elem.getElementsByTagName();
	} 
		HibernateUtil.commit();
        System.out.println("Added for length " + nl.getLength());
    	} catch (Exception e) {
    		System.out.println("Exception " +e);
    		e.printStackTrace();
    	}
    	System.exit(0);
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
            Meta meta = Meta.ensureExistence(marketid);
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

        }
    }

    static String reformat(String str) {
        if (str != null) {
            str = str.replace(",", ".").replace(" ", "");
        }
        return str;
    }
    
}
