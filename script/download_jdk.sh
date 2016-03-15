#!/bin/bash

JDK_VERSION=8u74-b02
JDK_FILENAME=jdk-8u74-linux-x64

DEFAULT_TYPE=tar.gz
if [ -n "$1" ]; then
    DEFAULT_TYPE=$1
fi

URL="http://download.oracle.com/otn-pub/java/jdk/${JDK_VERSION}/${JDK_FILENAME}.${DEFAULT_TYPE}

wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "${URL}" 

if [ "DEFAULT_TYPE" = "rpm" ]; then
    rpm -i ${JDK_FILENAME}.${DEFAULT_TYPE}
fi
