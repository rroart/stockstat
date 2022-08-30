FROM openjdk:17-jdk
MAINTAINER <roart@nvg.ntnu.no>
ARG mywebport
ARG myconfig
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ARG mydebug
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}
ENV MYDEBUG ${mydebug}
RUN microdnf install hostname
ADD files/stockstat-iclij-webcore-0.6-SNAPSHOT.jar /usr/local/bin
RUN mkdir /usr/local/conf
ADD iclij.xml /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java $MYDEBUG -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/stockstat-iclij-webcore-0.6-SNAPSHOT.jar --spring.profiles.active=docker

