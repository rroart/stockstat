FROM alpine
MAINTAINER <roart@nvg.ntnu.no>

ARG mydata

RUN mkdir -p /data/pytorch
ADD docker/cloud/pytorchdata/${mydata}/ /data/pytorch/
