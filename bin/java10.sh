#!/bin/bash

if [ -z "$JAVA10" ]; then
    [ -f /opt/jdk10/bin/java ] && JAVA10=/opt/jdk10/bin/java
    [ -f /usr/lib/jvm/java-10-openjdk-amd64/bin/java ] && JAVA10=/usr/lib/jvm/java-10-openjdk-amd64/bin/java
fi


