#!/bin/bash

if [ -z "$JAVA10" ]; then
    [ -f /usr/lib/jvm/java-10-openjdk-amd64/bin/java ] && JAVA10=/usr/lib/jvm/java-10-openjdk-amd64/bin/java
fi


