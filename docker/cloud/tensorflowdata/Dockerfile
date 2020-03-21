FROM alpine
MAINTAINER <roart@nvg.ntnu.no>

ARG mydata

RUN mkdir -p /data/tensorflow
ADD docker/cloud/tensorflowdata/${mydata}/ /data/tensorflow/
