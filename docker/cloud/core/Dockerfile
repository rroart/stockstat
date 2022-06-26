FROM debianjdk:latest
MAINTAINER <roart@nvg.ntnu.no>
ARG distlib
ARG distjar
ARG mywebport
ARG myconfig
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ARG mydebug
ARG jdk_java_options=
ENV DISTLIB ${distlib}
ENV DISTJAR ${distjar}
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}
ENV MYDEBUG ${mydebug}
ENV JDK_JAVA_OPTIONS ${jdk_java_options}
ADD $DISTLIB/$DISTJAR /usr/local/bin
RUN mkdir /usr/local/conf
ADD docker/cloud/core/$MYCONFIG /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
USER root
CMD SPARK_DRIVER_HOST=`hostname -i` java -Dconfig=$MYCONFIG $MYDEBUG --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/$DISTJAR --spring.profiles.active=docker

