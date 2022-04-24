FROM alpine
MAINTAINER <roart@nvg.ntnu.no>

ARG mydata

RUN mkdir -p /data/spark
ADD docker/cloud/sparkdata/${mydata}/ /data/spark/
