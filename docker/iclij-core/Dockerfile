FROM openjdk:8-jdk
MAINTAINER <roart@nvg.ntnu.no>
ADD files/stockstat-iclij-core-0.5-SNAPSHOT.jar /usr/local/bin
ADD files/iclij.xml /usr/local/config
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java -jar /usr/local/bin/stockstat-iclij-core-0.5-SNAPSHOT.jar

