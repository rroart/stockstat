package roart.client;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import roart.model.IncDecItem;
import roart.model.ResultItem;
import roart.model.ResultItemBytes;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.ResultItemText;
import roart.model.ResultMeta;
import roart.service.IclijWebControlService;
import roart.util.Constants;
import roart.util.EurekaUtil;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyIclijUI extends UI {

    private static Logger log = LoggerFactory.getLogger(MyIclijUI.class);

    private TabSheet tabsheet = null;
    public Label statLabel = null;

    IclijWebControlService controlService = null;

    VerticalLayout controlPanelTab;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        EurekaUtil.initEurekaClient();

        controlService = new IclijWebControlService();
        //controlService.getConfig();
        final VerticalLayout layout = new VerticalLayout();

        VerticalLayout searchTab;
        VerticalLayout configTab;

        com.vaadin.server.Page.getCurrent().setTitle("Stock statistics iclij by Roar Thronæs");

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
        // This tab gets its caption from the component caption
        configTab = getConfigTab();
        getSession().setAttribute("config", configTab);

        tabsheet.addTab(searchTab);
        // This tab gets its caption from the component caption
        tabsheet.addTab(controlPanelTab);
        //tabsheet.addTab(statTab);
        tabsheet.addTab(configTab);

        HorizontalLayout bottomLine = new HorizontalLayout();
        bottomLine.setHeight("10%");
        bottomLine.setWidth("90%");
        Label licenseLabel = new Label("Affero GPL");
        //licenseLabel.setWidth("30%");
        bottomLine.addComponent(licenseLabel);
        
        layout.addComponent(bottomLine);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyIclijUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
    
    private Button getMarket() {
        Button button = new Button("Get market data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private VerticalLayout getControlPanelTab() {
        boolean isProductionMode = VaadinService.getCurrent()
                .getDeploymentConfiguration().isProductionMode();
        VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Control Panel");
        HorizontalLayout horMisc = new HorizontalLayout();
        horMisc.setHeight("20%");
        horMisc.setWidth("90%");
        HorizontalLayout horStat = new HorizontalLayout();
        horStat.setHeight("20%");
        horStat.setWidth("90%");
        horStat.addComponent(new Label("b1"));
        VerticalLayout v1 = new VerticalLayout();
        horStat.addComponent(v1);

        v1.addComponent(new Label("b2"));
        HorizontalLayout h1 = new HorizontalLayout();
        v1.addComponent(h1);

        h1.addComponent(new Label("b3"));
        VerticalLayout v2 = new VerticalLayout();
        h1.addComponent(v2);

        v2.addComponent(new Label("b4"));

        //horStat.addComponent(getOverlapping());
        HorizontalLayout horDb = new HorizontalLayout();
        horDb.setHeight("20%");
        horDb.setWidth("60%");
        HorizontalLayout horTree = new HorizontalLayout();
        //ConfigTreeMap map2 = controlService.conf.configTreeMap;
        //componentMap = new HashMap<>();
        //print(map2, horTree);

        tab.addComponent(horMisc);
        //tab.addComponent(horMACD);
        //tab.addComponent(horRSI);
        tab.addComponent(horStat);
        tab.addComponent(horDb);
        tab.addComponent(horTree);
        return tab;
    }
/*
    Map<String, Component> componentMap ;
    private void print(ConfigTreeMap map2, HorizontalLayout tab) {
        Map<String, Object> map = controlService.conf.configValueMap;
        String name = map2.name;
        System.out.println("name " + name);
        Object object = map.get(name);
        Component o = null;
        String text = controlService.conf.text.get(name);
        if (object == null) {
            //System.out.println("null for " + name);
            String labelname = name;
            int last = name.lastIndexOf(".");
            if (last >=0) {
                labelname = name.substring(last + 1);
            }
            o = new Label(labelname + "    ");
            tab.addComponent(o);
            componentMap.put(name, o);
        } else {
            switch (object.getClass().getName()) {
            case "java.lang.String":
                o = getStringField(text, name);
                break;
            case "java.lang.Double":
                o = getDoubleField(text, name);
                break;
            case "java.lang.Integer":
                o = getIntegerField(text, name);
                break;
            case "java.lang.Boolean":
                o = getCheckbox(text, name);
                break;
            default:
                System.out.println("unknown " + object.getClass().getName());
                log.info("unknown " + object.getClass().getName());

            }
            tab.addComponent(o);
            componentMap.put(name, o);
        }
        //System.out.print(space.substring(0, indent));
        //System.out.println("map2 " + map2.name + " " + map2.enabled);
        Map<String, ConfigTreeMap> map3 = map2.configTreeMap;
        if (!map3.keySet().isEmpty()) {
            VerticalLayout h = new VerticalLayout();
            tab.addComponent(h);
            h.addComponent(new Label(">"));
            HorizontalLayout n1 = new HorizontalLayout();
            h.addComponent(n1);

            n1.addComponent(new Label(">"));
            VerticalLayout h1 = new VerticalLayout();
            n1.addComponent(h1);

            for (String key : map3.keySet()) {
                System.out.println("key " + key);
                HorizontalLayout n = new HorizontalLayout();
                h1.addComponent(n);
                print(map3.get(key), n);
                //Object value = map.get(key);
                //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            }
        }
    }
*/
    private VerticalLayout getConfigTab() {
        boolean isProductionMode = VaadinService.getCurrent()
                .getDeploymentConfiguration().isProductionMode();
        String DELIMITER = " = ";

        VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Configuration");
        HorizontalLayout hor = new HorizontalLayout();
        hor.setHeight("20%");
        hor.setWidth("90%");
        /*
   if (!isProductionMode) {
    hor.addComponent(getDbEngine());
    }
         */
        tab.addComponent(hor);
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
        //horStat.addComponent(getDate());
        //horStat.addComponent(getResetDate());
        horStat.addComponent(getMarket());
        //horStat.addComponent(getStat());
        //horStat.addComponent(getOverlapping());
        HorizontalLayout horDb = new HorizontalLayout();
        horDb.setHeight("20%");
        horDb.setWidth("60%");
        //horDb.addComponent(getDbItem());
        //horDb.addComponent(getMarkets());

        HorizontalLayout horDb2 = new HorizontalLayout();
        horDb2.setHeight("20%");
        horDb2.setWidth("60%");
        //horDb.addComponent(getDbItem());
        /*
        horDb2.addComponent(getDays());
        horDb2.addComponent(getTableDays());
        horDb2.addComponent(getTableIntervalDays());
        horDb2.addComponent(getTopBottom());
        HorizontalLayout horMACD = new HorizontalLayout();
        horMACD.setHeight("20%");
        horMACD.setWidth("90%");

        horMACD.addComponent(getMACDDeltaDays());
        horMACD.addComponent(getMACDHistogramDeltaDays());
         */
        // not yet:
        /*
        horDb2.addComponent(getATRDeltaDays());
        horDb2.addComponent(getCCIDeltaDays());
        horDb2.addComponent(getSTOCHDeltaDays());
         */
        /*
        HorizontalLayout horRSI = new HorizontalLayout();
        horRSI.setHeight("20%");
        horRSI.setWidth("90%");
        horRSI.addComponent(getRSIDeltaDays());
        horRSI.addComponent(getSTOCHRSIDeltaDays());
        HorizontalLayout horDb3 = new HorizontalLayout();
        horDb3.setHeight("20%");
        horDb3.setWidth("60%");
        horDb3.addComponent(getTableMoveIntervalDays());
         */
        /*
        horDb3.addComponent(getTodayZero());
         */
        //horDb3.addComponent(getEqualize());

        VerticalLayout verManualList = new VerticalLayout();
        verManualList.setHeight("20%");
        verManualList.setWidth("60%");
        HorizontalLayout horTester = new HorizontalLayout();
        horTester.setHeight("20%");
        horTester.setWidth("60%");
        //horTester.addComponent(getTestRecommender(false));
        //horTester.addComponent(getTestRecommender(true));

        HorizontalLayout horManual = new HorizontalLayout();
        horManual.setHeight("20%");
        horManual.setWidth("60%");
        //horDb.addComponent(getDbItem());
        //horManual.addComponent(getMarkets2(horManual, verManualList));

        HorizontalLayout horChooseGraph = new HorizontalLayout();
        horChooseGraph.setHeight("20%");
        horChooseGraph.setWidth("60%");
        //horDb.addComponent(getDbItem());
        //horChooseGraph.addComponent(getEqualizeGraph());
        //horChooseGraph.addComponent(getEqualizeUnify());
        //horChooseGraph.addComponent(getChooseGraph(verManualList));

        //tab.addComponent(horNewInd);
        tab.addComponent(horStat);
        tab.addComponent(horDb); 
        tab.addComponent(horDb2);
        //tab.addComponent(horDb3);
        //tab.addComponent(horMACD);
        //tab.addComponent(horRSI);
        tab.addComponent(horManual);
        tab.addComponent(verManualList);
        tab.addComponent(horChooseGraph);
        tab.addComponent(horTester);
        return tab;
    }
    private void displayResults() {
        System.out.println("h0");
        List<List> list = controlService.getContent();
        System.out.println("h1");
        log.info("listsize " + list.size());
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Results");
        displayResultListsTab(layout, list);
        //List listGraph = controlService.getContentGraph(guiSize);
        //displayResultListsTab(layout, listGraph);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");
    }

    public void displayResultListsTab(Layout tab, List<List> list) {

        final String table = (new ResultItemTable()).getClass().getName();
        final String text = (new ResultItemText()).getClass().getName();
        final String stream = (new ResultItemBytes()).getClass().getName();

        VerticalLayout result = getResultTemplate();
        //addListTable(result, list);
            for (List item : list) {
                System.out.println("here");
                System.out.println("here1");
                addListTable(result, item);
                if (text.equals(item.getClass().getName())) {
                    addListText(result, (ResultItemText) item);
                }
                /*
                if (stream.equals(item.getClass().getName())) {
                    addListStream(result, (ResultItemBytes) item);
                }
                */
            //}
        }
        tab.addComponent(result);
    }

    public void notify(String text) {
        Notification.show(text);
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

    void addListText(VerticalLayout ts, ResultItemText str) {
        ts.addComponent(new Label(str.text));
    }

    void addListTable(VerticalLayout ts, List list) {
        List<IncDecItem> mylist = new ObjectMapper().convertValue(list, new TypeReference<List<IncDecItem>>() { });
        Grid<IncDecItem> table = new Grid<>();
        table.addColumn(IncDecItem::getRecord).setCaption("Record");
        table.addColumn(IncDecItem::getMarket).setCaption("Market");
        table.addColumn(IncDecItem::isIncrease).setCaption("Inc");
        table.addColumn(IncDecItem::getId).setCaption("Id");
        table.addColumn(IncDecItem::getName).setCaption("Name");
        table.addColumn(IncDecItem::getScore).setCaption("Score");
        table.addColumn(IncDecItem::getDescription).setCaption("Description");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        ts.addComponent(table);
    }
/*
    private ListSelect getMarkets() {
        ListSelect ls = new ListSelect("Get market");
        Set<String> marketSet = null;
        try {
            List<String> markets = controlService.getMarkets();
            markets.remove(null);
            marketSet = new TreeSet<String>(markets);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return ls;
        }
        log.info("languages " + marketSet);
        if (marketSet == null ) {
            return ls;
        }
        ls.addItems(marketSet);
        ls.setNullSelectionAllowed(false);
        // Show 5 items and a scrollbar if there are more                       
        ls.setRows(5);
        ls.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String                 
                String value = (String) event.getProperty().getValue();
                // Do something with the value                              
                try {
                    controlService.conf.setMarket(value);
                    Notification.show("Request sent");
                    //displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        ls.setImmediate(true);
        return ls;
    }

*/

}
