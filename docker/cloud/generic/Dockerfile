FROM openjdk:17-jdk
MAINTAINER <roart@nvg.ntnu.no>
ARG distlib
ARG distjar
ARG mywebport
ARG myconfig
ARG mydebug
ARG extras
ARG lang
ARG appid
ENV DISTLIB ${distlib}
ENV DISTJAR ${distjar}
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYDEBUG ${mydebug}
ENV EXTRAS ${extras}
ENV LANG ${lang}
ENV APPID ${appid}
RUN microdnf install hostname
ADD $DISTLIB/$DISTJAR /usr/local/bin
RUN mkdir /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java $EXTRAS $MYDEBUG -Dserver.port=$MYWEBPORT -Deureka.client.enabled=false -jar /usr/local/bin/$DISTJAR --spring.profiles.active=docker
