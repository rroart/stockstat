package roart.client;

import roart.model.ResultItem;
import roart.model.Stock;
import roart.util.Constants;
import roart.service.ControlService;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.util.Date;
import java.util.TreeSet;
import java.io.File;
import java.io.InputStream;




//import roart.beans.session.misc.Unit;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Alignment;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.PopupDateField;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.server.FileResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.FileDownloader;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.Window;
import com.vaadin.annotations.Push;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.shared.ui.label.ContentMode;




//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Push
//@Theme("mytheme")
@Theme("valo")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI
{

    private static Logger log = LoggerFactory.getLogger(MyVaadinUI.class);
    //private static final Logger log = LoggerFactory.getLogger(MyVaadinUI.class);

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "roart.client.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

   private TabSheet tabsheet = null;
    public Label statLabel = null;

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
	VerticalLayout searchTab = null, controlPanelTab = null;

	com.vaadin.server.Page.getCurrent().setTitle("Stock statistics by Roar Thronæs");

        layout.setMargin(true);
        setContent(layout);

	HorizontalLayout topLine = new HorizontalLayout();
	Label topTitle = new Label("Stock statistics");
	topTitle.setWidth("90%");
	topLine.addComponent(topTitle);
	topLine.setHeight("10%");
	topLine.setWidth("100%");	
	statLabel = new Label("", ContentMode.PREFORMATTED);
	statLabel.setWidth("50%");
	topLine.addComponent(statLabel);
	layout.addComponent(topLine);

	tabsheet = new TabSheet();
	tabsheet.setHeight("80%");
	layout.addComponent(tabsheet);
	// Create the first tab
    searchTab = getSearchTab();
	getSession().setAttribute("search", searchTab);
	// This tab gets its caption from the component caption
	controlPanelTab = getControlPanelTab();
	getSession().setAttribute("controlpanel", controlPanelTab);

	tabsheet.addTab(searchTab);
	// This tab gets its caption from the component caption
	tabsheet.addTab(controlPanelTab);
	//tabsheet.addTab(statTab);

	HorizontalLayout bottomLine = new HorizontalLayout();
	bottomLine.setHeight("10%");
	bottomLine.setWidth("90%");
	Label licenseLabel = new Label("Affero GPL");
	//licenseLabel.setWidth("30%");
	bottomLine.addComponent(licenseLabel);
	//bottomLine.setComponentAlignment(licenseLabel, Alignment.BOTTOM_RIGHT);
	layout.addComponent(bottomLine);
    }

    private VerticalLayout getControlPanelTab() {
	VerticalLayout tab = new VerticalLayout();
	tab.setCaption("Control Panel");
	HorizontalLayout horNewInd = new HorizontalLayout();
	horNewInd.setHeight("20%");
	horNewInd.setWidth("90%");
	HorizontalLayout horStat = new HorizontalLayout();
	horStat.setHeight("20%");
	horStat.setWidth("90%");
	horStat.addComponent(getOverlapping());
	HorizontalLayout horDb = new HorizontalLayout();
	horDb.setHeight("20%");
	horDb.setWidth("60%");
	
	/*
	tab.addComponent(getCleanup());
	tab.addComponent(getCleanup2());
	tab.addComponent(getCleanupfs());
	*/

	tab.addComponent(horNewInd);
	tab.addComponent(horStat);
	tab.addComponent(horDb);
	return tab;
    }

    private VerticalLayout getSearchTab() {
        List list = ControlService.getContent();
        displayResultListsTab(list);
        VerticalLayout tab = new VerticalLayout();
    tab.setCaption("Search");
    HorizontalLayout horNewInd = new HorizontalLayout();
    horNewInd.setHeight("20%");
    horNewInd.setWidth("90%");
    HorizontalLayout horStat = new HorizontalLayout();
    horStat.setHeight("20%");
    horStat.setWidth("90%");
    horStat.addComponent(getOverlapping());
    HorizontalLayout horDb = new HorizontalLayout();
    horDb.setHeight("20%");
    horDb.setWidth("60%");
    horDb.addComponent(getDate());
    horDb.addComponent(getResetDate());
    horDb.addComponent(getDbItem());
    horDb.addComponent(getMarkets());
    horDb.addComponent(getDays());
    
    tab.addComponent(horNewInd);
    tab.addComponent(horStat);
    tab.addComponent(horDb);
    return tab;
    }

    private InlineDateField getDate() {
        InlineDateField tf = new InlineDateField("Set comparison date");
        // Create a DateField with the default style                            
        // Set the date and time to present                                     
        Date date = new Date();
        // temp fix                                                             
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        tf.setValue(date);

        // Handle changes in the value                                          
        tf.addValueChangeListener(new Property.ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    // Assuming that the value type is a String                 
                    Date date = (Date) event.getProperty().getValue();
                    // Do something with the value                              
                    ControlService maininst = new ControlService();
                    long time = date.getTime();
                    // get rid of millis garbage
                    time = time / 1000;
                    time = time * 1000;
                    date = new Date(time);
                    //System.out.println("bla " + time + " " +date);
                    try {
                        maininst.setdate(date);
                        Notification.show("Request sent");
                        List list = ControlService.getContent();
                        displayResultListsTab(list);
                   } catch (Exception e) {
                       e.printStackTrace();
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            });
        // Fire value changes immediately when the field loses focus            
        tf.setImmediate(true);
        return tf;
    }

    private Button getResetDate() {
        Button button = new Button("Reset date");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
        ControlService maininst = new ControlService();
        try {
            maininst.setdate(null);
            Notification.show("Request sent");
            List list = ControlService.getContent();
            displayResultListsTab(list);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
            }
        });
    return button;
    }

   private Button getOverlapping() {
        Button button = new Button("Overlapping");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
		ControlService maininst = new ControlService();
		maininst.overlapping();
		Notification.show("Request sent");
            }
        });
	return button;
    }

    private TextField getDbItem() {
	TextField tf = new TextField("Database md5 id");

	// Handle changes in the value
	tf.addValueChangeListener(new Property.ValueChangeListener() {
		public void valueChange(ValueChangeEvent event) {
		    // Assuming that the value type is a String
		    String value = (String) event.getProperty().getValue();
		    // Do something with the value
		    ControlService maininst = new ControlService();
		    try {
			maininst.dbindex(value);
			Notification.show("Request sent");
		    } catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
		    }
		}
	    });
	// Fire value changes immediately when the field loses focus
	tf.setImmediate(true);
	return tf;
    }

    private ListSelect getMarkets() {
        ListSelect ls = new ListSelect("Get market");
        Set<String> languages = null;
        try {
            List<String> langs = Stock.getMarkets();
            langs.remove(null);
            languages = new TreeSet<String>(langs);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return ls;
        }
        log.info("languages " + languages);
        if (languages == null ) {
            return ls;
        }
        ls.addItems(languages);
        ls.setNullSelectionAllowed(false);
        // Show 5 items and a scrollbar if there are more                       
        ls.setRows(5);
        ls.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String                 
                String value = (String) event.getProperty().getValue();
                // Do something with the value                              
                ControlService maininst = new ControlService();
                try {
                maininst.setMarket(value);
                Notification.show("Request sent");
                List list = ControlService.getContent();
                displayResultListsTab(list);
                 } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                }
            }
            });
        // Fire value changes immediately when the field loses focus
        ls.setImmediate(true);
        return ls;
    }
    
    private TextField getDays() {
	TextField tf = new TextField("Interval days");

	// Handle changes in the value
	tf.addValueChangeListener(new Property.ValueChangeListener() {
		public void valueChange(ValueChangeEvent event) {
		    // Assuming that the value type is a String
		    String value = (String) event.getProperty().getValue();
		    // Do something with the value
		    ControlService maininst = new ControlService();
		    try {
			maininst.setDays(new Integer(value));
			Notification.show("Request sent");
                List list = ControlService.getContent();
                displayResultListsTab(list);
		    } catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
		    }
		}
	    });
	// Fire value changes immediately when the field loses focus
	tf.setImmediate(true);
	return tf;
    }

    void addListTable(VerticalLayout ts, List<ResultItem> strarr) {
	if (strarr.size() <= 1) {
	    return;
	}

	Table table = new Table("Table");
	table.setWidth("90%");
	int columns = strarr.get(0).get().size();
	int mdcolumn = 0;
	for (int i=0; i<strarr.size(); i++) {
	    if (strarr.get(i).get().size() != columns) {
		System.out.println("column differs " + columns + " found " + strarr.get(i).get().size());
		break;
	    }
	}
	for (int i = 0; i < columns; i++) {
	    Object object = null;
	    for (int j = 1; j < strarr.size(); j++) {
	        object = strarr.get(j).get().get(i);
	        if (object != null) {
	            break;
	        }
	    }
	    //Object object = strarr.get(1).get().get(i);
	    if (object == null) {
	        table.addContainerProperty(strarr.get(0).get().get(i), String.class, null);
	        continue;
	    }
	    switch (object.getClass().getName()) {
	        case "java.lang.String":
	            table.addContainerProperty(strarr.get(0).get().get(i), String.class, null);
	        break;
	           case "java.lang.Integer":
	                table.addContainerProperty(strarr.get(0).get().get(i), Integer.class, null);
	            break;
	            case "java.lang.Double":
                table.addContainerProperty(strarr.get(0).get().get(i), Double.class, null);
            break;
            case "java.util.Date":
                table.addContainerProperty(strarr.get(0).get().get(i), Date.class, null);
            break;
            case "java.sql.Timestamp":
                table.addContainerProperty(strarr.get(0).get().get(i), Timestamp.class, null);
            break;
	        default:
	        System.out.println("not found" + strarr.get(0).get().get(i).getClass().getName() + "|" + object.getClass().getName());
	        break;
	    }
	    //table.addContainerProperty(strarr.get(0).get().get(i), String.class, null);
	}
for (int i = 1; i < strarr.size(); i++) {
	    ResultItem str = strarr.get(i);
	    //System.out.println("" + );
	    try {
	    table.addItem(str.getarr(), i);
	    } catch (Exception e) {
	        log.error("i " + i + " " + str.get().get(0));
	        e.printStackTrace();
	    }
	}
	//table.setPageLength(table.size());
	ts.addComponent(table);
    }

    void addList(VerticalLayout ts, List<String> strarr) {
	for (int i=0; i<strarr.size(); i++) {
	    String str = strarr.get(i);
	    ts.addComponent(new Label(str));
	}
    }

    private VerticalLayout getResultTemplate() {
	final Component content = getContent();
	VerticalLayout res = new VerticalLayout();
	res.addComponent(new Label("Search results"));
	/*
        Button button = new Button("Back to main page");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                setContent(content);
            }
        });
	res.addComponent(button);
	*/
	return res;
    }

    @SuppressWarnings("rawtypes")
	public void displayResultLists(List<List> lists) {
	VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Search results");

	VerticalLayout result = getResultTemplate();
	if (lists != null) {
	    for (List<ResultItem> list : lists) {
		addListTable(result, list);
	    }
	}
	tab.addComponent(result);

	tabsheet.addComponent(tab);
	tabsheet.getTab(tab).setClosable(true);

	//System.out.println("setcont" +this);
	//getSession().getLockInstance().lock();
	//setContent(result);
	//getSession().getLockInstance().unlock();
    }

    @SuppressWarnings("rawtypes")
	public void displayResultListsTab(List<List> lists) {
	VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Results");

	VerticalLayout result = getResultTemplate();
	if (lists != null) {
	    for (List<ResultItem> list : lists) {
		addListTable(result, list);
	    }
	}
	tab.addComponent(result);

	tabsheet.addComponent(tab);
	tabsheet.getTab(tab).setClosable(true);
	Notification.show("New result available");
    }

    public void notify(String text) {
	Notification.show(text);
    }

}
