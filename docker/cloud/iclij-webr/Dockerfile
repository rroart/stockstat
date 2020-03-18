FROM node:12
MAINTAINER <rroart@gmail.com>
ARG myserver
ARG myport
ARG myiserver
ARG myiport
ARG mywebport
ENV NPM_CONFIG_LOGLEVEL warn
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYISERVER ${myiserver}
ENV MYIPORT ${myiport}
ENV MYWEBPORT ${mywebport}
RUN mkdir /app
WORKDIR /app
COPY iclij/iclij-webr/ iclij-webr
WORKDIR /app/iclij-webr
RUN npm install -g http-server
RUN npm install
RUN npm run build
WORKDIR /app/iclij-webr/docroot

CMD http-server -p $MYWEBPORT
