FROM node:12
MAINTAINER <rroart@gmail.com>
ARG myserver
ARG myport
ARG mywebport
ENV NPM_CONFIG_LOGLEVEL warn
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYWEBPORT ${mywebport}
RUN mkdir /app
WORKDIR /app
COPY webr/ webr
WORKDIR /app/webr
RUN npm install -g http-server
RUN npm install
RUN npm run build
WORKDIR /app/webr/docroot

CMD http-server -p $MYWEBPORT
