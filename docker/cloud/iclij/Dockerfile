FROM debianjdk:latest
MAINTAINER <roart@nvg.ntnu.no>
ARG distlib
ARG distjar
ARG myaserver
ARG myiserver
ARG mymserver
ARG myserver
ARG myport
ARG myaport
ARG myiport
ARG mymport
ARG mywebport
ARG myconfig
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ARG springdataurl
ARG springdatausername
ARG springdatapassword
ARG springdatadriver
ARG mydebug
ARG jdk_java_options=
ENV DISTLIB ${distlib}
ENV DISTJAR ${distjar}
ENV MYASERVER ${myaserver}
ENV MYISERVER ${myiserver}
ENV MYMSERVER ${mymserver}
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYAPORT ${myaport}
ENV MYIPORT ${myiport}
ENV MYMPORT ${mymport}
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}
ENV SPRINGDATAURL ${springdataurl}
ENV SPRINGDATAUSERNAME ${springdatausername}
ENV SPRINGDATAPASSWORD ${springdatapassword}
ENV SPRINGDATADRIVER ${springdatadriver}
ENV MYDEBUG ${mydebug}
ENV JDK_JAVA_OPTIONS ${jdk_java_options}
ADD $DISTLIB/$DISTJAR /usr/local/bin
RUN mkdir /usr/local/conf
ADD docker/cloud/iclij/$myconfig /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java -Dconfig=$MYCONFIG $MYDEBUG -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/$DISTJAR --spring.profiles.active=docker

