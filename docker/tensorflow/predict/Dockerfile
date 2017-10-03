FROM debian:stretch
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3-pip
RUN pip3 install pandas tensorflow numpy nameko Werkzeug Keras sklearn

ADD files/kemain.py /usr/local/bin/kemain.py
ADD files/config2.yaml /usr/local/bin/config2.yaml

EXPOSE 8001

WORKDIR /usr/local/bin

CMD nameko run --config config2.yaml kemain