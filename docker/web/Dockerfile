FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>
ENV EUREKA_SERVER_URI http://stockstat-eureka:8761/eureka
ADD files/stockstat-web-0.5-SNAPSHOT.war /usr/local/bin
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
EXPOSE 8180
CMD java -Deureka.serviceUrl.default=http://stockstat-eureka:8761/eureka -jar /usr/local/bin/stockstat-web-0.5-SNAPSHOT.jar
