FROM postgres:14-bullseye
MAINTAINER <roart@nvg.ntnu.no>

ARG mydump
ARG mydbserver
ENV MYDBSERVER ${mydbserver}

#RUN apt-get update && apt-get upgrade -y
#RUN apt-get install -y postgresql-client-11
COPY docker/cloud/dbclone/${mydump} /tmp/${mydump}
RUN (zcat /tmp/${mydump} || cat /tmp/${mydump}) > /usr/local/bin/stockstat.sql
#PGPASSWORD='password' createdb -U stockstat -W -h postgresql-12-centos7 stockstat; 
CMD sleep 30; PGPASSWORD='password' psql -U stockstat -h ${MYDBSERVER} -d stockstat -f /usr/local/bin/stockstat.sql; while true; do sleep 3600; done 
