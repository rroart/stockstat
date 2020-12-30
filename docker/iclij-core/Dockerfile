FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>
ENV EUREKA_SERVER_URI http://stockstat-eureka:8761/eureka
ENV EUREKA_PREFER_IPADDRESS true
ADD files/stockstat-iclij-core-0.6-SNAPSHOT.jar /usr/local/bin
RUN mkdir /usr/local/conf
ADD files/iclij.xml /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java -Deureka.serviceUrl.default=http://stockstat-eureka:8761/eureka -Dconnection.url=jdbc:postgresql://postgresql-10-centos7:5432/stockstat -jar /usr/local/bin/stockstat-iclij-core-0.6-SNAPSHOT.jar

