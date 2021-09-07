FROM openjdk:16-jdk
MAINTAINER <roart@nvg.ntnu.no>
ADD files/stockstat-eureka-0.6-SNAPSHOT.jar /usr/local/bin
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
EXPOSE 8761
CMD java -jar /usr/local/bin/stockstat-eureka-0.6-SNAPSHOT.jar

