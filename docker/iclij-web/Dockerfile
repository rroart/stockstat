FROM openjdk:8-jdk
MAINTAINER <roart@nvg.ntnu.no>
ENV EUREKA_SERVER_URI http://stockstat-eureka:8761/eureka
ADD files/stockstat-iclij-web-0.5-SNAPSHOT.war /usr/local/bin
ADD files/jetty-runner-9.4.10.RC1.jar /usr/local/bin
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
EXPOSE 8181
CMD java -Deureka.serviceUrl.default=http://stockstat-eureka:8761/eureka -jar jetty-runner-9.4.10.RC1.jar --port 8181 /usr/local/bin/stockstat-iclij-web-0.5-SNAPSHOT.war
