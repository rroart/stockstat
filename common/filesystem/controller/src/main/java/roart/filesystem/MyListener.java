package roart.filesystem;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("listener")
public class MyListener implements ApplicationListener<ServletWebServerInitializedEvent> {

    private int port;
    
    public int getPort() {
        System.out.println("MyPort2 " + this.port);
        return port;
    }
    
    @Override
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
        this.port = event.getWebServer().getPort();
        System.out.println("MyPort " + event.getSource().getPort());
        System.out.println("MyPort " + this.port);
    }
}