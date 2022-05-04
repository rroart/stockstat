FROM openjdk:17-jdk
MAINTAINER <roart@nvg.ntnu.no>
ARG distlib
ARG distjar
ARG myaserver
ARG myiserver
ARG myserver
ARG myport
ARG myaport
ARG myiport
ARG mywebport
ARG myconfig
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ARG mydebug
ENV DISTLIB ${distlib}
ENV DISTJAR ${distjar}
ENV MYASERVER ${myaserver}
ENV MYISERVER ${myiserver}
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYAPORT ${myaport}
ENV MYIPORT ${myiport}
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}
ENV MYDEBUG ${mydebug}
RUN microdnf install hostname
ADD $DISTLIB/$DISTJAR /usr/local/bin
RUN mkdir /usr/local/conf
ADD docker/cloud/iclij/$myconfig /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java -Dconfig=$MYCONFIG $MYDEBUG -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/$DISTJAR --spring.profiles.active=docker

