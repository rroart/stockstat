package roart.client;

import roart.config.ConfigConstants;
import roart.config.MyPropertyConfig;
import roart.model.GUISize;
import roart.model.ResultItemBytes;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.ResultItemText;
import roart.model.ResultItem;
import roart.util.Constants;
import roart.service.ControlService;

import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.vaadin.server.VaadinService;
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

    public static GUISize guiSize = new GUISize();
    
    ControlService controlService = null;
    
    @Override
    protected void init(VaadinRequest request) {
        
        controlService = new ControlService();
        final VerticalLayout layout = new VerticalLayout();
        guiSize.x = com.vaadin.server.Page.getCurrent().getBrowserWindowWidth();
        guiSize.y = com.vaadin.server.Page.getCurrent().getBrowserWindowHeight();
        VerticalLayout searchTab;
        VerticalLayout controlPanelTab;
        VerticalLayout configTab;
        
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
        //bottomLine.setComponentAlignment(licenseLabel, Alignment.BOTTOM_RIGHT);
        layout.addComponent(bottomLine);
    }

    private VerticalLayout getControlPanelTab() {
    	boolean isProductionMode = VaadinService.getCurrent()
    		    .getDeploymentConfiguration().isProductionMode();
        VerticalLayout tab = new VerticalLayout();
        tab.setCaption("Control Panel");
        HorizontalLayout horMACD = new HorizontalLayout();
        horMACD.setHeight("20%");
        horMACD.setWidth("90%");
        horMACD.addComponent(getMACD());
        horMACD.addComponent(getMACDDelta());
        horMACD.addComponent(getMACDHistogramDelta());
        horMACD.addComponent(getMove());
        horMACD.addComponent(getRSI());
        horMACD.addComponent(getRSIdelta());
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

        tab.addComponent(horMACD);
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

    private VerticalLayout getConfigTab() {
    	boolean isProductionMode = VaadinService.getCurrent()
    		    .getDeploymentConfiguration().isProductionMode();
        String DELIMITER = " = ";
        
    VerticalLayout tab = new VerticalLayout();
    tab.setCaption("Configuration");
    HorizontalLayout hor = new HorizontalLayout();
    hor.setHeight("20%");
    hor.setWidth("90%");
   if (!isProductionMode) {
    hor.addComponent(getDbEngine());
    }
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
        horStat.addComponent(getDate());
        horStat.addComponent(getResetDate());
        horStat.addComponent(getReload());
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
        horDb2.addComponent(getMACDDeltaDays());
        horDb2.addComponent(getMACDHistogramDeltaDays());
        horDb2.addComponent(getRSIdiffDays());
        HorizontalLayout horDb3 = new HorizontalLayout();
        horDb3.setHeight("20%");
        horDb3.setWidth("60%");
        horDb3.addComponent(getTableMoveIntervalDays());
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
                long time = date.getTime();
                // get rid of millis garbage
                time = time / 1000;
                time = time * 1000;
                date = new Date(time);
                try {
                    controlService.conf.setdate(date);
                    Notification.show("Request sent");
                    displayResults();
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
                try {
                    controlService.conf.setdate(null);
                    Notification.show("Request sent");
                    //displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        return button;
    }

    private Button getReload() {
        Button button = new Button("Reload");
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

    private Button getStat() {
        Button button = new Button("Get stats");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    Notification.show("Request sent");
                    displayResultsStat();
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
                    displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        ls.setImmediate(true);
        return ls;
    }

    Set<Pair<String, String>> chosen = new HashSet<>();
    
    private Button getChooseGraph(VerticalLayout ver) {
        Button button = new Button("Choose graph");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    
                    Notification.show("Request sent");
                    displayResultsGraph(chosen);
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
        Set<String> stockSet = null;
        System.out.println("m " + market);
        final Map<String, String> stockMap = controlService.getStocks(market);
        log.info("stocks " + stockSet);
        if (stockSet == null ) {
            return ls;
        }
        ls.addItems(stockSet);
        ls.setNullSelectionAllowed(false);
        // Show 5 items and a scrollbar if there are more                       
        ls.setRows(5);
        ls.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String                 
                String value = (String) event.getProperty().getValue();
                // Do something with the value                              
                try {
                    String id = null;
                    for (String stockid : stockMap.keySet()) {
                    	String stockname = stockMap.get(stockid);
                        if (value.equals(stockname)) {
                            id = stockid;
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
        tf.setValue("" + controlService.conf.getDays());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

   private TextField getMACDHistogramDeltaDays() {
       TextField tf = new TextField("MACD histogram delta days");
       tf.setValue("" + controlService.conf.getMACDHistogramDeltaDays());

       // Handle changes in the value
       tf.addValueChangeListener(new Property.ValueChangeListener() {
           public void valueChange(ValueChangeEvent event) {
               // Assuming that the value type is a String
               String value = (String) event.getProperty().getValue();
               // Do something with the value
               try {
                   controlService.conf.setMACDHistogramDeltaDays(new Integer(value));
                   Notification.show("Request sent");
                   displayResults();
               } catch (Exception e) {
                   log.error(Constants.EXCEPTION, e);
               }
           }
       });
       // Fire value changes immediately when the field loses focus
       tf.setImmediate(true);
       return tf;
   }

   private TextField getMACDDeltaDays() {
       TextField tf = new TextField("MACD delta days");
       tf.setValue("" + controlService.conf.getMACDDeltaDays());

       // Handle changes in the value
       tf.addValueChangeListener(new Property.ValueChangeListener() {
           public void valueChange(ValueChangeEvent event) {
               // Assuming that the value type is a String
               String value = (String) event.getProperty().getValue();
               // Do something with the value
               try {
                   controlService.conf.setMACDDeltaDays(new Integer(value));
                   Notification.show("Request sent");
                   displayResults();
               } catch (Exception e) {
                   log.error(Constants.EXCEPTION, e);
               }
           }
       });
       // Fire value changes immediately when the field loses focus
       tf.setImmediate(true);
       return tf;
   }

   private TextField getRSIdiffDays() {
       TextField tf = new TextField("RSI diff days");
       tf.setValue("" + controlService.conf.getRSIdiffDays());

       // Handle changes in the value
       tf.addValueChangeListener(new Property.ValueChangeListener() {
           public void valueChange(ValueChangeEvent event) {
               // Assuming that the value type is a String
               String value = (String) event.getProperty().getValue();
               // Do something with the value
               try {
                   controlService.conf.setRSIdiffDays(new Integer(value));
                   Notification.show("Request sent");
                   displayResults();
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
        tf.setValue("" + controlService.conf.getTableIntervalDays());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setTableIntervalDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

    private TextField getTableMoveIntervalDays() {
        TextField tf = new TextField("Table move interval days");
        tf.setValue("" + controlService.conf.getTableMoveIntervalDays());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setTableMoveIntervalDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults();
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
        tf.setValue("" + controlService.conf.getTableDays());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setTableDays(new Integer(value));
                    Notification.show("Request sent");
                    displayResults();
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
        tf.setValue("" + controlService.conf.getTopBottom());

        // Handle changes in the value
        tf.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                String value = (String) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setTopBottom(new Integer(value));
                    Notification.show("Request sent");
                    displayResults();
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }

        });
        // Fire value changes immediately when the field loses focus
        tf.setImmediate(true);
        return tf;
    }

    private void displayResults() {
        List<ResultItem> list = controlService.getContent();
        log.info("listsize " + list.size());
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Results");
        displayResultListsTab(layout, list);
        List listGraph = controlService.getContentGraph(guiSize);
        displayResultListsTab(layout, listGraph);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");
     }


    private void displayResultsStat() {
        List list = controlService.getContentStat();
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Results");
        displayResultListsTab(layout, list);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");
   }

    private void displayResultsGraph(Set<Pair<String, String>> ids) {
        VerticalLayout layout = new VerticalLayout();
        layout.setCaption("Graph results");
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        List listGraph = controlService.getContentGraph(ids, guiSize);
        displayResultListsTab(layout, listGraph);
        tabsheet.addComponent(layout);
        tabsheet.getTab(layout).setClosable(true);
        Notification.show("New result available");       
    }
    
    private StreamResource getStreamResource(byte[] bytes) {
        //System.out.println("bytes " + bytes.length + " "+ new String(bytes));
        //System.out.println("size " + (300 + 10 * xsize) + " " + (400 + 10 * ysize));
       StreamResource resource = new StreamResource(new StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    return new ByteArrayInputStream(bytes);
                } catch (Exception e) {
                    //log.error(Constants.EXCEPTION, e);
                    return null;
                }
            }
        }, "/tmp/svg3.svg");
       return resource;
    }

    protected void addListStream(Layout layout, ResultItemBytes item) {
    	byte[] bytes = item.bytes;
            //SvgUtil. bla3(layout, resource);
            //SvgUtil. bla5(layout, resource);
            //if (true) continue;
        	StreamResource resource = getStreamResource(bytes);
            Image image = new Image ("Image", resource);
            //Embedded image = new Embedded("1", img);
            int xsize = 100 + 300 + 10 * controlService.conf.getTableDays();
            int ysize = 200 + 400 + 10 * controlService.conf.getTopBottom();
            //System.out.println("xys1 " + xsize + " " + ysize);
            if (xsize + 100 > guiSize.x) {
                xsize = guiSize.x - 100;
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

    private CheckBox getEqualize() {
        CheckBox cb = new CheckBox("Equalize sample sets");
        cb.setValue(controlService.conf.isEqualize());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setEqualize(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getMove() {
        CheckBox cb = new CheckBox("Enable chart move");
        cb.setValue(controlService.conf.isMoveEnabled());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setMoveEnabled(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getMACD() {
        CheckBox cb = new CheckBox("Enable MACD");
        cb.setValue(controlService.conf.isMACDEnabled());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setMACDEnabled(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getMACDDelta() {
        CheckBox cb = new CheckBox("Enable MACD delta");
        cb.setValue(controlService.conf.isMACDDeltaEnabled());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setMACDDeltaEnabled(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getMACDHistogramDelta() {
        CheckBox cb = new CheckBox("Enable MACD histogram delta");
        cb.setValue(controlService.conf.isMACDHistogramDeltaEnabled());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setMACDHistogramDeltaEnabled(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getRSIdelta() {
        CheckBox cb = new CheckBox("Enable RSI delta");
        cb.setValue(controlService.conf.isRSIDeltaEnabled());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setRSIDeltaEnabled(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private CheckBox getRSI() {
        CheckBox cb = new CheckBox("Enable RSI");
        cb.setValue(controlService.conf.isRSIenabled());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setRSIenabled(value);
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
        cb.setValue(controlService.conf.isGraphEqualize());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setGraphEqualize(value);
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
        cb.setValue(controlService.conf.isGraphEqUnify());

        // Handle changes in the value
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                // Assuming that the value type is a String
                boolean value = (Boolean) event.getProperty().getValue();
                // Do something with the value
                try {
                    controlService.conf.setGraphEqUnify(value);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
            }
        });
        // Fire value changes immediately when the field loses focus
        cb.setImmediate(true);
        return cb;
    }

    private ListSelect getDbEngine() {
    	ListSelect ls = new ListSelect("Select search engine");
    	String[] engines = MyPropertyConfig.dbvalues;
    	ls.addItems(engines);
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
    			maininst.dbengine(value.equals(ConfigConstants.SPARK));
    			Notification.show("Request sent");
    		    } catch (Exception e) {
    			log.error(Constants.EXCEPTION, e);
    		    }
    		}
    	    });
    	// Fire value changes immediately when the field loses focus
    	ls.setImmediate(true);
    	return ls;
    }
    
   void addListText(VerticalLayout ts, ResultItemText str) {
            ts.addComponent(new Label(str.text));
     }

    void addListTable(VerticalLayout ts, ResultItemTable strarr) {
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
                System.out.println("column differs " + columns + " found " + strarr.get(i).get().size() + " " + i + " : " + strarr.get(i).get().get(1) + " " +strarr.get(i).get() );
                break;
            }
        }
        try {
        	log.info("arr " + 0 + " " + strarr.get(0).getarr().length + " " + Arrays.toString(strarr.get(0).getarr()));
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
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);        	
        }
        log.info("strarr size " + strarr.size());
        for (int i = 1; i < strarr.size(); i++) {
        	log.info("str arr " + i);
            ResultItemTableRow str = strarr.get(i);
            try {
                if (strarr.get(0).get().get(0).equals(Constants.IMG)) {
                    String id = (String) str.get().get(0);
                    str.get().set(0, getImage(id));
                    //Object myid = table.addItem(str.getarr(), i); 
                    //log.info("myid " + str.getarr().length + " " + myid);
                }
                Object myid = table.addItem(str.getarr(), i);
                if (myid == null) {
                	log.error("addItem failed for" + Arrays.toString(str.getarr()));
                }
            } catch (Exception e) {
                log.error("i " + i + " " + str.get().get(0));
                log.error(Constants.EXCEPTION, e);
            }
        }
        //table.setPageLength(table.size());
        log.info("tabledata " + table.getColumnHeaders().length + " " + table.size() + " " + Arrays.toString(table.getColumnHeaders()));
        ts.addComponent(table);
    }

    /*
    void addListTable(VerticalLayout ts, ResultItemTable resulttable) {
    	log.info("t " + resulttable.size());
    	if (resulttable.size() <= 1) {
            return;
        }

        Table table = new Table("Table");
        table.setWidth("90%");
        int columns = resulttable.get(0).get().size();
        int mdcolumn = 0;
        for (int i=0; i<resulttable.size(); i++) {
            if (resulttable.get(i).get().size() != columns) {
                log.error("column differs " + columns + " found " + resulttable.get(i).get().size());
                System.out.println("column differs " + columns + " found " + resulttable.get(i).get().size() + " " + i + " : " + resulttable.get(i).get().get(1) + " " +resulttable.get(i).get() );
                break;
            }
        }
        for (int i = 0; i < columns; i++) {
            Object object = null;
            for (int j = 1; j < resulttable.size(); j++) {
                object = resulttable.get(j).get().get(i);
                if (object != null) {
                    break;
                }
            }
            //Object object = strarr.get(1).get().get(i);
            if (object == null) {
                table.addContainerProperty(resulttable.get(0).get().get(i), String.class, null);
                continue;
            }
            switch (object.getClass().getName()) {
            case "java.lang.String":
                if (i == 0 && resulttable.get(0).get().get(0).equals(Constants.IMG)) {
                    table.addContainerProperty(resulttable.get(0).get().get(i), Button.class, null);
                } else {
                    table.addContainerProperty(resulttable.get(0).get().get(i), String.class, null);
                }
                break;
            case "java.lang.Integer":
                table.addContainerProperty(resulttable.get(0).get().get(i), Integer.class, null);
                break;
            case "java.lang.Double":
                table.addContainerProperty(resulttable.get(0).get().get(i), Double.class, null);
                break;
            case "java.util.Date":
                table.addContainerProperty(resulttable.get(0).get().get(i), Date.class, null);
                break;
            case "java.sql.Timestamp":
                table.addContainerProperty(resulttable.get(0).get().get(i), Timestamp.class, null);
                break;
            default:
                log.error("not found" + resulttable.get(0).get().get(i).getClass().getName() + "|" + object.getClass().getName());
                System.out.println("not found" + resulttable.get(0).get().get(i).getClass().getName() + "|" + object.getClass().getName());
                break;
            }
            //table.addContainerProperty(strarr.get(0).get().get(i), String.class, null);
        }
        for (int i = 1; i < resulttable.size(); i++) {
            ResultItemTableRow str = resulttable.get(i);
            try {
                if (resulttable.get(0).get().get(0).equals(Constants.IMG)) {
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
*/
    
    private Button getImage(final String id) {
        Button button = new Button("Img");
        button.setHtmlContentAllowed(true);
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                String idarr[] = id.split(",");
                Set<Pair<String, String>> ids = new HashSet<>();
                for (String id : idarr) {
                    ids.add(new Pair(controlService.conf.getMarket(), id));
                }
                displayResultsGraph(ids);
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
    public void displayResultListsTab(Layout tab, List<ResultItem>/*<List>*/ lists) {

        final String table = (new ResultItemTable()).getClass().getName();
        final String text = (new ResultItemText()).getClass().getName();
        final String stream = (new ResultItemBytes()).getClass().getName();
        
        VerticalLayout result = getResultTemplate();
        if (lists != null) {
            for (ResultItem item : lists) {
            	if (table.equals(item.getClass().getName())) {
            		addListTable(result, (ResultItemTable) item);
            	}
            	if (text.equals(item.getClass().getName())) {
            		addListText(result, (ResultItemText) item);
            	}
            	if (stream.equals(item.getClass().getName())) {
            		addListStream(result, (ResultItemBytes) item);
            	}
            }
       }
        tab.addComponent(result);
   }

    public void notify(String text) {
        Notification.show(text);
    }

}
