#!/bin/bash

if [ -z "$JAVA11" ]; then
    [ -f /opt/jdk11/bin/java ] && JAVA11=/opt/jdk11/bin/java
    [ -f /usr/lib/jvm/java-11-openjdk-amd64/bin/java ] && JAVA11=/usr/lib/jvm/java-11-openjdk-amd64/bin/java
fi


