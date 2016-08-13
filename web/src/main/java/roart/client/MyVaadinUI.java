package roart.client;

import roart.model.ResultItem;
import roart.model.Stock;
import roart.util.Constants;
import roart.util.SvgUtil;
import roart.service.ControlService;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;




import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;




















//import roart.beans.session.misc.Unit;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Image;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Link;
import com.vaadin.ui.Alignment;
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
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.Window;
import com.vaadin.annotations.Push;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.server.Sizeable;
























//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;


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

    public static int x = 0, y = 0;
    
    @Override
    protected void init(VaadinRequest request) {
        
        final VerticalLayout layout = new VerticalLayout();
        x = com.vaadin.server.Page.getCurrent().getBrowserWindowWidth();
        y = com.vaadin.server.Page.getCurrent().getBrowserWindowHeight();
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
        //horStat.addComponent(getOverlapping());
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
        /*
	HorizontalLayout bla2 = new HorizontalLayout();
	tab.addComponent(bla2);
	JFreeChart c = SvgUtil.bla();
	//OutputStream out = null;
	//InputStream in = null
    StreamResource resource = SvgUtil.chartToResource(c, "/tmp/svg.svg");
	SvgUtil.bla2(bla2, resource);
	SvgUtil.bla3(tab, resource);
	SvgUtil.bla4(tab);
	SvgUtil.bla5(tab, resource);
         */
        return tab;
    }

    private VerticalLayout getSearchTab() {
        //displayResults(new ControlService());
        VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Search");
        HorizontalLayout horNewInd = new HorizontalLayout();
        horNewInd.setHeight("20%");
        horNewInd.setWidth("90%");
        HorizontalLayout horStat = new HorizontalLayout();
        horStat.setHeight("20%");
        horStat.setWidth("90%");
        horStat.addComponent(getDate());
        horStat.addComponent(getResetDate());
        horStat.addComponent(getStat());
       //horStat.addComponent(getOverlapping());
        HorizontalLayout horDb = new HorizontalLayout();
        horDb.setHeight("20%");
        horDb.setWidth("60%");
        //horDb.addComponent(getDbItem());
        horDb.addComponent(getMarkets());

        HorizontalLayout horDb2 = new HorizontalLayout();
        horDb2.setHeight("20%");
        horDb2.setWidth("60%");
        //horDb.addComponent(getDbItem());
        horDb2.addComponent(getDays());
        horDb2.addComponent(getTableDays());
        horDb2.addComponent(getTableIntervalDays());
        horDb2.addComponent(getTopBottom());
        HorizontalLayout horDb3 = new HorizontalLayout();
        /*
        horDb3.addComponent(getTodayZero());
        */
        //horDb3.addComponent(getEqualize());

        VerticalLayout verManualList = new VerticalLayout();
        verManualList.setHeight("20%");
        verManualList.setWidth("60%");
        
        HorizontalLayout horManual = new HorizontalLayout();
        horManual.setHeight("20%");
        horManual.setWidth("60%");
        //horDb.addComponent(getDbItem());
        horManual.addComponent(getMarkets2(horManual, verManualList));

        HorizontalLayout horChooseGraph = new HorizontalLayout();
        horChooseGraph.setHeight("20%");
        horChooseGraph.setWidth("60%");
        //horDb.addComponent(getDbItem());
        horChooseGraph.addComponent(getEqualizeGraph());
        horChooseGraph.addComponent(getEqualizeUnify());
       horChooseGraph.addComponent(getChooseGraph(verManualList));

        //tab.addComponent(horNewInd);
        tab.addComponent(horStat);
        tab.addComponent(horDb); 
        tab.addComponent(horDb2);
        tab.addComponent(horDb3);
        tab.addComponent(horManual);
        tab.addComponent(verManualList);
        tab.addComponent(horChooseGraph);
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
                try {
                    maininst.setdate(date);
                    Notification.show("Request sent");
                    displayResults(maininst);
                } catch (Exception e) {
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
                    displayResults(maininst);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getStat() {
        Button button = new Button("Get stats");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                ControlService maininst = new ControlService();
                try {
                    Notification.show("Request sent");
                    displayResultsStat(maininst);
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
                    displayResults(maininst);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        ls.setImmediate(true);
        return ls;
    }

    Set<Pair> chosen = new HashSet<Pair>();
    
    private Button getChooseGraph(VerticalLayout ver) {
        Button button = new Button("Choose graph");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                ControlService maininst = new ControlService();
                try {
                    
                    Notification.show("Request sent");
                    displayResultsGraph(maininst, chosen);
                    chosen.clear();
                    ver.removeAllComponents();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private ListSelect getMarkets2(HorizontalLayout horManual, VerticalLayout verManualList) {
        ListSelect ls = new ListSelect("Market");
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
                    ListSelect ls2 = getUnits(value, ls, verManualList);
                    horManual.addComponent(ls2);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        ls.setImmediate(true);
        return ls;
    }

    private ListSelect getUnits(String market, ListSelect ls2, VerticalLayout verManualList) {
        ListSelect ls = new ListSelect("Units");
        Set<String> languages = null;
        System.out.println("m " + market);
        List<Stock> stocks = null;
        try {
            stocks = Stock.getAll(market);
            stocks.remove(null);
            languages = new TreeSet<String>();
            for (Stock stock : stocks) {
                languages.add(stock.getName());
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return ls;
        }
        final List<Stock> finalstocks = stocks;
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
                    String id = null;
                    for (Stock stock : finalstocks) {
                        if (value.equals(stock.getName())) {
                            id = stock.getId();
                            break;
                        }
                    }
                    Pair pair = new Pair(market, id);
                    chosen.add(pair);
                    verManualList.addComponent(new Label(market + " " + id + " " + value));
                    ComponentContainer parent = (ComponentContainer) ls.getParent();
                    parent.removeComponent(ls);
                    System.out.println("value " +value);
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
        TextField tf = new TextField("Single interval days");
        tf.setValue("" + new ControlService().getDays());

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
                    displayResults(maininst);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

    private TextField getTableIntervalDays() {
        TextField tf = new TextField("Table interval days");
        tf.setValue("" + new ControlService().getTableIntervalDays());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                ControlService maininst = new ControlService();
                try {
                    maininst.setTableIntervalDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults(maininst);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

    private TextField getTableDays() {
        TextField tf = new TextField("Table days");
        tf.setValue("" + new ControlService().getTableDays());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                ControlService maininst = new ControlService();
                try {
                    maininst.setTableDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults(maininst);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

    private TextField getTopBottom() {
        TextField tf = new TextField("Table top/bottom");
        tf.setValue("" + new ControlService().getTopBottom());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                ControlService maininst = new ControlService();
                try {
                    maininst.setTopBottom(new Integer(value));
                    Notification.show("Request sent");
                    displayResults(maininst);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }

        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

    private void displayResults(ControlService maininst) {
        List list = maininst.getContent();
        Layout layout = displayResultListsTab(list);
        List listGraph = maininst.getContentGraph();
        displayListGraphTab(layout, listGraph);
    }


    private void displayResultsStat(ControlService maininst) {
        List list = maininst.getContentStat();
        Layout layout = displayResultListsTab(list);
    }

    private void displayResultsGraph(ControlService maininst, Set<Pair> ids) {
        VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Graph results");
        tabsheet.addComponent(tab);
        tabsheet.getTab(tab).setClosable(true);
        List listGraph = maininst.getContentGraph(ids);
        displayListGraphTab(tab, listGraph);
    }

    protected void displayListGraphTab(Layout layout, List<StreamResource> listGraph) {
        if (listGraph == null) {
            return;
        }
        for (StreamResource resource : listGraph) {
            //SvgUtil. bla3(layout, resource);
            //SvgUtil. bla5(layout, resource);
            //if (true) continue;
            Image image = new Image ("Image", resource);
            //Embedded image = new Embedded("1", img);
            int xsize = 100 + 300 + 10 * ControlService.getTableDays();
            int ysize = 200 + 400 + 10 * ControlService.getTopBottom();
            //System.out.println("xys1 " + xsize + " " + ysize);
            if (xsize + 100 > x) {
                xsize = x - 100;
            }
            /*
            if (ysize + 200 > y) {
                ysize = y - 200;
            }
            */
            //System.out.println("xys2 " + xsize + " " + ysize);
            image.setHeight(ysize, Sizeable.Unit.PIXELS );
            image.setWidth(xsize, Sizeable.Unit.PIXELS );
            layout.addComponent(image);
        }
    }

    private CheckBox getEqualize() {
        CheckBox cb = new CheckBox("Equalize sample sets");
        cb.setValue(new ControlService().isEqualize());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                ControlService maininst = new ControlService();
                try {
                    maininst.setEqualize(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getEqualizeGraph() {
        CheckBox cb = new CheckBox("Equalize graphic table");
        cb.setValue(new ControlService().isGraphEqualize());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                ControlService maininst = new ControlService();
                try {
                    maininst.setGraphEqualize(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getEqualizeUnify() {
        CheckBox cb = new CheckBox("Equalize merge price and index table");
        cb.setValue(new ControlService().isGraphEqUnify());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                ControlService maininst = new ControlService();
                try {
                    maininst.setGraphEqUnify(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
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
                log.error("column differs " + columns + " found " + strarr.get(i).get().size());
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
                if (i == 0 && strarr.get(0).get().get(0).equals(Constants.IMG)) {
                    table.addContainerProperty(strarr.get(0).get().get(i), Button.class, null);
                } else {
                    table.addContainerProperty(strarr.get(0).get().get(i), String.class, null);
                }
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
                log.error("not found" + strarr.get(0).get().get(i).getClass().getName() + "|" + object.getClass().getName());
                System.out.println("not found" + strarr.get(0).get().get(i).getClass().getName() + "|" + object.getClass().getName());
                break;
            }
            //table.addContainerProperty(strarr.get(0).get().get(i), String.class, null);
        }
        for (int i = 1; i < strarr.size(); i++) {
            ResultItem str = strarr.get(i);
            try {
                if (strarr.get(0).get().get(0).equals(Constants.IMG)) {
                    String id = (String) str.get().get(0);
                    str.get().set(0, getImage(id));
                    table.addItem(str.getarr(), i);                    
                }
                table.addItem(str.getarr(), i);                    
            } catch (Exception e) {
                log.error("i " + i + " " + str.get().get(0));
                log.error(Constants.EXCEPTION, e);
            }
        }
        //table.setPageLength(table.size());
        ts.addComponent(table);
    }

    private Button getImage(final String id) {
        Button button = new Button("Img");
        button.setHtmlContentAllowed(true);
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                ControlService maininst = new ControlService();
                String idarr[] = id.split(",");
                Set<Pair> ids = new HashSet<Pair>();
                for (String id : idarr) {
                    ids.add(new Pair(ControlService.getMarket(), id));
                }
                displayResultsGraph(maininst, ids);
                Notification.show("Request sent");
            }
        });
        return button;
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
    }

    @SuppressWarnings("rawtypes")
    public Layout displayResultListsTab(List<List> lists) {
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
        return tab;
    }

    public void notify(String text) {
        Notification.show(text);
    }

}
