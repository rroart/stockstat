package roart.client;

import java.io.BufferedWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.vaadin.annotations.Theme;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import roart.common.config.ConfigTreeMap;
import roart.common.constants.Constants;
import roart.common.model.ConfigItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.RelationItem;
import roart.common.model.TimingItem;
import roart.eureka.util.EurekaUtil;
import roart.iclij.model.MapList;
import roart.iclij.service.IclijServiceList;
import roart.result.model.ResultItemBytes;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemText;
import roart.service.IclijWebControlService;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SpringUI
@SpringViewDisplay
@Theme("mytheme")
public class MyIclijUI extends UI implements ViewDisplay {

    private static Logger log = LoggerFactory.getLogger(MyIclijUI.class);

    private TabSheet tabsheet = null;
    public Label statLabel = null;

    IclijWebControlService controlService = null;

    VerticalLayout controlPanelTab;

    Navigator navigator;
    
    private Panel springViewDisplay;

    @Override
    public void showView(View view) {
        springViewDisplay.setContent((Component) view);
    }
    
    @Override
    protected void init(VaadinRequest vaadinRequest) {
        log.info("testme {}", VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode());
        this.getNavigator().setErrorView(this.getNavigator().getCurrentView());
        //EurekaUtil.initEurekaClient();
        
        controlService = new IclijWebControlService();
        controlService.getConfig();
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

        //navigator = new Navigator(this, this.getContent().getC);
        //this
        
        springViewDisplay = new Panel();
        springViewDisplay.setSizeFull();
        layout.addComponent(springViewDisplay);
        layout.setExpandRatio(springViewDisplay, 1.0f);
        getNavigator().setErrorView(ErrorView.class);
    }
    
    //@WebServlet(value = {"/UI/*",/VAADIN/*})
    //@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    //@VaadinServletConfiguration(ui = MyIclijUI.class, productionMode = false)
    /*
    public static class MyUIServlet extends VaadinServlet {
    }
    */
    
    /*
    private TextField getDays() {
        TextField tf = new TextField("Verify days");
        tf.setValue("" + controlService.getVerifyConfig().getDays());

        // Handle changes in the value
        tf.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(HasValue.ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getValue();
                // Do something with the value
                try {
                    controlService.getVerifyConfig().setDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //tf.setImmediate(true);
        return tf;
    }
*/
    
    private Button getVerify() {
        Button button = new Button("Get verification data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayVerify();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getVerifyLoop() {
        Button button = new Button("Get verification data loop");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayVerifyLoop();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
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

    private Button getMarketImprove() {
        Button button = new Button("Get market improve data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayImproveResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getMarketEvolve() {
        Button button = new Button("Get market evolve data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayEvolveResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getMarketMachineLearning() {
        Button button = new Button("Get market machine learning data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayMachineLearningResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getMarketCrosstest() {
        Button button = new Button("Get market crosstest data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayCrosstestResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getMarketFilter() {
        Button button = new Button("Get market filter data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayFilterResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getMarketAboveBelow() {
        Button button = new Button("Get market above below data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayAboveBelowResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getMarketDataset() {
        Button button = new Button("Get market dataset data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayDatasetResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getImproveAboveBelowMarket() {
        Button button = new Button("Run and get single above below data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayAboveBelowMarket();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getSingleMarket() {
        Button button = new Button("Run and get single market data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displaySingleMarket();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getSingleMarketLoop() {
        Button button = new Button("Run and get single market data loop");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displaySingleMarketLoop();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getImproveProfit() {
        Button button = new Button("Run and get single market improve data");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayImproveProfit();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private ListSelect getMarkets() {
        ListSelect ls = new ListSelect("Get market");
        Set<String> marketSet = null;
        try {
            List<String> markets = controlService.getMarkets();
            markets.remove(null);
            marketSet = new TreeSet<>(markets);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return ls;
        }
        log.info("languages " + marketSet);
        if (marketSet == null ) {
            return ls;
        }
        ls.setItems(marketSet);
        //ls.setNullSelectionAllowed(false);
        // Show 5 items and a scrollbar if there are more                       
        ls.setRows(5);
        ls.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String                 
                Set<String> values = (Set<String>) event.getValue();
                // TODO multi-valued?
                String value = values.iterator().next();
                // Do something with the value                              
                try {
                    controlService.getIclijConf().getConfigData().setMarket(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //ls.setImmediate(true);
        return ls;
    }

    private ListSelect getMLMarkets() {
        ListSelect ls = new ListSelect("Get ML market");
        Set<String> marketSet = null;
        try {
            List<String> markets = controlService.getMarkets();
            markets.remove(null);
            marketSet = new TreeSet<>(markets);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return ls;
        }
        log.info("languages " + marketSet);
        if (marketSet == null ) {
            return ls;
        }
        ls.setItems(marketSet);
        //ls.setNullSelectionAllowed(false);
        // Show 5 items and a scrollbar if there are more                       
        ls.setRows(5);
        ls.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String                 
                Set<String> values = (Set<String>) event.getValue();
                // TODO multi-valued?
                String value = values.iterator().next();
                // Do something with the value                              
                try {
                    controlService.getIclijConf().setMlmarket(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //ls.setImmediate(true);
        return ls;
    }

    private Button resetMLMarkets() {
        Button button = new Button("Reset ML market");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    controlService.getIclijConf().setMlmarket(null);
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
        ConfigTreeMap map2 = controlService.getIclijConf().getConfigData().getConfigTreeMap();
        componentMap = new HashMap<>();
        print(map2, horTree);

        tab.addComponent(horMisc);
        //tab.addComponent(horMACD);
        //tab.addComponent(horRSI);
        tab.addComponent(horStat);
        tab.addComponent(horDb);
        tab.addComponent(horTree);
        return tab;
    }

    Map<String, Component> componentMap ;
    private void print(ConfigTreeMap map2, HorizontalLayout tab) {
        Map<String, Object> map = controlService.getIclijConf().getConfigData().getConfigValueMap();
        String name = map2.getName();
        System.out.println("name " + name);
        Object object = map.get(name);
        Component o = null;
        String text = controlService.getIclijConf().getText().get(name);
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
        Map<String, ConfigTreeMap> map3 = map2.getConfigTreeMap();
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
        HorizontalLayout horMarkets = new HorizontalLayout();
        horMarkets.addComponent(getMarkets());
        horMarkets.addComponent(getMLMarkets());
        horMarkets.addComponent(resetMLMarkets());
        HorizontalLayout horDate = new HorizontalLayout();
        horDate.setHeight("20%");
        horDate.setWidth("90%");
        horDate.addComponent(getDate());
        horDate.addComponent(getResetDate());
        HorizontalLayout horGetAuto = new HorizontalLayout();
        //horStat.addComponent(getDays());
        HorizontalLayout horVerify = new HorizontalLayout();
        horVerify.addComponent(getVerify());
        horVerify.addComponent(getVerifyLoop());
        HorizontalLayout horOther = new HorizontalLayout();
        horOther.addComponent(getSingleMarket());
        horOther.addComponent(getSingleMarketLoop());
        horOther.addComponent(getImproveAboveBelowMarket());
        horOther.addComponent(getImproveProfit());
        HorizontalLayout horOther2 = new HorizontalLayout();
        horOther2.addComponent(getMarket());
        horOther2.addComponent(getMarketImprove());
        horOther2.addComponent(getMarketEvolve());
        horOther2.addComponent(getMarketMachineLearning());
        horOther2.addComponent(getMarketCrosstest());
        horOther2.addComponent(getMarketDataset());
        horOther2.addComponent(getMarketFilter());
        horOther2.addComponent(getMarketAboveBelow());
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
        tab.addComponent(horMarkets);
        tab.addComponent(horDate);
        tab.addComponent(horOther2);
        //tab.addComponent(horGetAuto);
        tab.addComponent(horVerify);
        tab.addComponent(horOther);
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
        //System.out.println("h0");
        controlService.getContent(this);
        //System.out.println("h1");
        /*
        log.info("listsize " + list.size());
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Results");
        displayResultListsTab(layout, list);
        //List listGraph = controlService.getContentGraph(guiSize);
        //displayResultListsTab(layout, listGraph);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");
        */
    }

    private void displayImproveResults() {
        controlService.getContentImprove(this);
    }
    
    private void displayEvolveResults() {
        controlService.getContentEvolve(this);
    }
    
    private void displayMachineLearningResults() {
        controlService.getContentMachineLearning(this);
    }
    
    private void displayDatasetResults() {
        controlService.getContentDataset(this);
    }
    
    private void displayCrosstestResults() {
        controlService.getContentCrosstest(this);
    }
    
    private void displayFilterResults() {
        controlService.getContentFilter(this);
    }
    
    private void displayAboveBelowResults() {
        controlService.getContentAboveBelow(this);
    }
    
    private void displayAboveBelowMarket() {
        //System.out.println("h0");
        controlService.getImproveAboveBelowMarket(this);
    }
    
    private void displaySingleMarket() {
        //System.out.println("h0");
        controlService.getSingleMarket(this);
    }
    
    private void displaySingleMarketLoop() {
        //System.out.println("h0");
        controlService.getSingleMarketLoop(this);
    }
    
    private void displayImproveProfit() {
        //System.out.println("h0");
        controlService.getImproveProfit(this);
    }
    
    private void displayVerify() {
        controlService.getVerify(this);
        /*
        log.info("listsize {}", list.size());
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Test");
        displayResultListsTab(layout, list);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");
        */
    }

    private void displayVerifyLoop() {
        controlService.getVerifyLoop(this);
        /*
        log.info("listsize {}", list.size());
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Test");
        displayResultListsTab(layout, list);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");
        */
    }

    public void displayResultListsTab(Layout tab, List<IclijServiceList> list) {

        final String table = (new ResultItemTable()).getClass().getName();
        final String text = (new ResultItemText()).getClass().getName();
        final String stream = (new ResultItemBytes()).getClass().getName();

        Path path = Paths.get("" + System.currentTimeMillis() + ".txt");
        BufferedWriter writer = null;
        VerticalLayout result = getResultTemplate();
        //addListTable(result, list);
            for (IclijServiceList item : list) {
                System.out.println("here");
                System.out.println("here1");
                addListTable(result, item);
                /*
                if (text.equals(item.getClass().getName())) {
                    addListText(result, (ResultItemText) item);
                }
                */
                /*
                if (stream.equals(item.getClass().getName())) {
                    addListStream(result, (ResultItemBytes) item);
                }
                */
            //}
        }
        tab.addComponent(result);
        tabsheet.addComponent(tab);
        tabsheet.getTab(tab).setClosable(true);
        Notification.show("New result available");
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

    void addListTable(VerticalLayout ts, IclijServiceList item) {
        List list = item.getList();
        ObjectMapper objectMapper = getObjectMapper();
        if (list == null || list.isEmpty()) {
            log.error("List null or empty");
            return;
        }
        if (((java.util.LinkedHashMap) list.get(0)).keySet().contains("category")) {
            List<MemoryItem> mylist = objectMapper.convertValue(list, new TypeReference<List<MemoryItem>>() { });
            Grid<MemoryItem> table = getGridFromList(item, mylist);
            ts.addComponent(table);
            return;
        }
        if (((java.util.LinkedHashMap) list.get(0)).keySet().contains("increase")) {
            List<IncDecItem> mylist = objectMapper.convertValue(list, new TypeReference<List<IncDecItem>>() { });
            Grid<IncDecItem> table = getGridFromList2(item, mylist);
            ts.addComponent(table);
            return;
        }
        if (((java.util.LinkedHashMap) list.get(0)).keySet().contains("mytime")) {
            List<TimingItem> mylist = objectMapper.convertValue(list, new TypeReference<List<TimingItem>>() { });
            Grid<TimingItem> table = getGridFromList4(item, mylist);
            ts.addComponent(table);
            return;
        }
        if (((java.util.LinkedHashMap) list.get(0)).keySet().contains("type")) {
            List<RelationItem> mylist = objectMapper.convertValue(list, new TypeReference<List<RelationItem>>() { });
            Grid<RelationItem> table = getGridFromList6(item, mylist);
            ts.addComponent(table);
            return;
        }
        if (((java.util.LinkedHashMap) list.get(0)).keySet().contains("localcomponent")) {
            List<MLMetricsItem> mylist = objectMapper.convertValue(list, new TypeReference<List<MLMetricsItem>>() { });
            Grid<MLMetricsItem> table = getGridFromList7(item, mylist);
            ts.addComponent(table);
            return;
        }
        if (((java.util.LinkedHashMap) list.get(0)).keySet().contains("id")) {
            List<ConfigItem> mylist = objectMapper.convertValue(list, new TypeReference<List<ConfigItem>>() { });
            Grid<ConfigItem> table = getGridFromList5(item, mylist);
            ts.addComponent(table);            
        } else {
            List<MapList> mylist = objectMapper.convertValue(list, new TypeReference<List<MapList>>() { });
            Grid<MapList> table = getGridFromList3(item, mylist);
            ts.addComponent(table);
        }
    }

    private Grid<MLMetricsItem> getGridFromList7(IclijServiceList item, List<MLMetricsItem> mylist) {
        Grid<MLMetricsItem> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(MLMetricsItem::getRecord).setCaption("Record");
        table.addColumn(MLMetricsItem::getDate).setCaption("Date");
        table.addColumn(MLMetricsItem::getMarket).setCaption("Market");
        table.addColumn(MLMetricsItem::getComponent).setCaption("Component");
        table.addColumn(MLMetricsItem::getSubcomponent).setCaption("Subcomponent");
        table.addColumn(MLMetricsItem::getLocalcomponent).setCaption("Local component");
        table.addColumn(MLMetricsItem::getTestAccuracy).setCaption("Test accuracy");
        table.addColumn(MLMetricsItem::getLoss).setCaption("Loss");
        table.addColumn(MLMetricsItem::getThreshold).setCaption("Threshold");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }
    
    private Grid<RelationItem> getGridFromList6(IclijServiceList item, List<RelationItem> mylist) {
        Grid<RelationItem> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(RelationItem::getRecord).setCaption("Record");
        table.addColumn(RelationItem::getMarket).setCaption("Market");
        table.addColumn(RelationItem::getId).setCaption("Id");
        table.addColumn(RelationItem::getAltId).setCaption("AltId");
        table.addColumn(RelationItem::getType).setCaption("Type");
        table.addColumn(RelationItem::getValue).setCaption("Value");
        table.addColumn(RelationItem::getOtherMarket).setCaption("OtherMarket");
        table.addColumn(RelationItem::getOtherId).setCaption("OtherId");
        table.addColumn(RelationItem::getOtherAltId).setCaption("OtherAltId");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }

    private Grid<ConfigItem> getGridFromList5(IclijServiceList item, List<ConfigItem> mylist) {
        Grid<ConfigItem> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(ConfigItem::getRecord).setCaption("Record");
        table.addColumn(ConfigItem::getDate).setCaption("Date");
        table.addColumn(ConfigItem::getMarket).setCaption("Market");
        table.addColumn(ConfigItem::getAction).setCaption("Action");
        table.addColumn(ConfigItem::getId).setCaption("Id");
        table.addColumn(ConfigItem::getValue).setCaption("Value");
        table.addColumn(ConfigItem::getComponent).setCaption("Component");
        table.addColumn(ConfigItem::getSubcomponent).setCaption("Subcomponent");
        table.addColumn(ConfigItem::getScore).setCaption("Score");
        table.addColumn(ConfigItem::getBuy).setCaption("Buy");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }

    private Grid<TimingItem> getGridFromList4(IclijServiceList item, List<TimingItem> mylist) {
        Grid<TimingItem> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(TimingItem::getRecord).setCaption("Record");
        table.addColumn(TimingItem::getDate).setCaption("Date");
        table.addColumn(TimingItem::getMarket).setCaption("Market");
        table.addColumn(TimingItem::getMlmarket).setCaption("ML market");
        table.addColumn(TimingItem::getAction).setCaption("Action");
        table.addColumn(TimingItem::isEvolve).setCaption("Evolve");
        table.addColumn(TimingItem::getComponent).setCaption("Component");
        table.addColumn(TimingItem::getSubcomponent).setCaption("Subcomponent");
        table.addColumn(TimingItem::getParameters).setCaption("Threshold");
        table.addColumn(TimingItem::getMytime).setCaption("Time");
        table.addColumn(TimingItem::getScore).setCaption("Score");
        table.addColumn(TimingItem::getBuy).setCaption("Buy");
        table.addColumn(TimingItem::getDescription).setCaption("Description");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }

    private Grid<MapList> getGridFromList3(IclijServiceList item, List<MapList> mylist) {
        Grid<MapList> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(MapList::getKey).setCaption("Key");
        table.addColumn(MapList::getValue).setCaption("Value");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }

    private Grid<IncDecItem> getGridFromList2(IclijServiceList item, List<IncDecItem> mylist) {
        Grid<IncDecItem> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(IncDecItem::getRecord).setCaption("Record");
        table.addColumn(IncDecItem::getDate).setCaption("Date");
        table.addColumn(IncDecItem::getMarket).setCaption("Market");
        table.addColumn(IncDecItem::isIncrease).setCaption("Inc");
        table.addColumn(IncDecItem::getId).setCaption("Id");
        table.addColumn(IncDecItem::getName).setCaption("Name");
        table.addColumn(IncDecItem::getScore).setCaption("Score");
        table.addColumn(IncDecItem::getDescription).setCaption("Description");
        table.addColumn(IncDecItem::getVerified).setCaption("Verified");
        table.addColumn(IncDecItem::getVerificationComment).setCaption("Comment");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }

    private Grid<MemoryItem> getGridFromList(IclijServiceList item, List<MemoryItem> mylist) {
        Grid<MemoryItem> table = new Grid<>();
        table.setCaption(item.getTitle());
        table.addColumn(MemoryItem::getRecord).setCaption("Record");
        table.addColumn(MemoryItem::getDate).setCaption("Date");
        table.addColumn(MemoryItem::getUsedsec).setCaption("Usedsec");
        table.addColumn(MemoryItem::getMarket).setCaption("Market");
        table.addColumn(MemoryItem::getTestaccuracy).setCaption("Testaccuracy");
        table.addColumn(MemoryItem::getTestloss).setCaption("Testloss");
        table.addColumn(MemoryItem::getConfidence).setCaption("Confidence");
        table.addColumn(MemoryItem::getLearnConfidence).setCaption("LearnConfidence");
        table.addColumn(MemoryItem::getCategory).setCaption("Category");
        table.addColumn(MemoryItem::getComponent).setCaption("Component");
        table.addColumn(MemoryItem::getSubcomponent).setCaption("Subcomponent");
        table.addColumn(MemoryItem::getDescription).setCaption("Description");
        table.addColumn(MemoryItem::getInfo).setCaption("Info");
        table.addColumn(MemoryItem::getFuturedays).setCaption("Futuredays");
        table.addColumn(MemoryItem::getFuturedate).setCaption("Futuredate");
        table.addColumn(MemoryItem::getPositives).setCaption("Positives");
        table.addColumn(MemoryItem::getSize).setCaption("Size");
        table.addColumn(MemoryItem::getParameters).setCaption("Threshold");
        table.addColumn(MemoryItem::getTp).setCaption("Tp");
        table.addColumn(MemoryItem::getTpSize).setCaption("TpSize");
        table.addColumn(MemoryItem::getTpConf).setCaption("TpConf");
        table.addColumn(MemoryItem::getTpProb).setCaption("TpProb");
        table.addColumn(MemoryItem::getTpProbConf).setCaption("TpProbConf");
        table.addColumn(MemoryItem::getTn).setCaption("Tn");
        table.addColumn(MemoryItem::getTnSize).setCaption("TnSize");
        table.addColumn(MemoryItem::getTnConf).setCaption("TnConf");
        table.addColumn(MemoryItem::getTnProb).setCaption("TnProb");
        table.addColumn(MemoryItem::getTnProbConf).setCaption("TnProbConf");
        table.addColumn(MemoryItem::getFp).setCaption("Fp");
        table.addColumn(MemoryItem::getFpSize).setCaption("FpSize");
        table.addColumn(MemoryItem::getFpConf).setCaption("FpConf");
        table.addColumn(MemoryItem::getFpProb).setCaption("FpProb");
        table.addColumn(MemoryItem::getFpProbConf).setCaption("FpProbConf");
        table.addColumn(MemoryItem::getFn).setCaption("Fn");
        table.addColumn(MemoryItem::getFnSize).setCaption("FnSize");
        table.addColumn(MemoryItem::getFnConf).setCaption("FnConf");
        table.addColumn(MemoryItem::getFnProb).setCaption("FnProb");
        table.addColumn(MemoryItem::getFnProbConf).setCaption("FnProbConf");
        table.addColumn(MemoryItem::getPosition).setCaption("Position");
        table.setWidth("90%");
        table.setItems(mylist);
        System.out.println("added");
        return table;
    }
    
    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //objectMapper.registerModule(new JSR310Module());
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    private InlineDateField getDate() {
        InlineDateField tf = new InlineDateField("Set comparison date");
        // Create a DateField with the default style                            
        // Set the date and time to present                                     
        LocalDate date = LocalDate.now();
        tf.setValue(date);

        // Handle changes in the value                                          
        tf.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(HasValue.ValueChangeEvent event) {
                // Assuming that the value type is a String                 
                LocalDate date = (LocalDate) event.getValue();
                try {
                    controlService.getIclijConf().setDate(date);
                    Notification.show("Request sent");
                    //displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus            
        //tf.setImmediate(true);
        return tf;
    }

    private Button getResetDate() {
        Button button = new Button("Reset date");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    controlService.getIclijConf().setDate(null);
                    Notification.show("Request sent");
                    //displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
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
                    controlService.getConf().setMarket(value);
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
    private CheckBox getCheckbox(String text, String configKey) {
        CheckBox cb = new CheckBox(text);
        Boolean origValue = (Boolean) controlService.getIclijConf().getConfigData().getConfigValueMap().get(configKey);
        cb.setValue(origValue);

        // Handle changes in the value
        cb.addValueChangeListener(new HasValue.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getValue();
                // Do something with the value
                try {
                    controlService.getIclijConf().getConfigValueMap().put(configKey, value );
                    // TODO handle hiding
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //cb.setImmediate(true);
        return cb;
    }

    private TextField getStringField(String text, String configKey) {
        TextField tf = new TextField(text);
        tf.setSizeFull();
        String origValue = (String) controlService.getIclijConf().getConfigData().getConfigValueMap().get(configKey);

        tf.setValue(origValue);

        // Handle changes in the value
        tf.addValueChangeListener(new HasValue.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getValue();
                // Do something with the value
                try {
                    controlService.getIclijConf().getConfigValueMap().put(configKey, value );
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //tf.setImmediate(true);
        return tf;
    }

    private TextField getIntegerField(String text, String configKey) {
        TextField tf = new TextField(text);
        Integer origValue = (Integer) controlService.getConfigData().getIclijConf().getConfigValueMap().get(configKey);

        tf.setValue("" + origValue);

        // Handle changes in the value
        tf.addValueChangeListener(new HasValue.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getValue();
                // Do something with the value
                try {
                    controlService.getIclijConf().getConfigValueMap().put(configKey, new Integer(value) );
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //tf.setImmediate(true);
        return tf;
    }
    private TextField getDoubleField(String text, String configKey) {
        TextField tf = new TextField(text);
        Double origValue = (Double) controlService.getIclijConf().getConfigData().getConfigValueMap().get(configKey);

        tf.setValue("" + origValue);

        // Handle changes in the value
        tf.addValueChangeListener(new HasValue.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getValue();
                // Do something with the value
                try {
                    controlService.getIclijConf().getConfigValueMap().put(configKey, new Double(value) );
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        //tf.setImmediate(true);
        return tf;
    }

}
