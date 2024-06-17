#!/bin/bash
set -xv

host=${1:-localhost}
port=${2:-10000}
min_memory=${3:-8000m}
max_memory=${4:-16000m}
lower_bound=${5:-0}
upper_bound=${6:-1000000}
size=${7:-1000000}
runtime_in_seconds=${8:-60}
target=${9:-100000}
rest_target=${10:-10000}
machine=${11:-localhost}

while [[ ! -f "STOP.file" && ! -f "hs_err_*.log" ]]
do
    java \
    --enable-preview \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.port=${port} \
    -Djava.rmi.server.hostname=${host} \
    -Xms${min_memory} \
    -Xmx${max_memory} \
    -DtestType= \
    -DconcurrencyUtility= \
    -DlowerBound=${lower_bound} \
    -DupperBound=${upper_bound} \
    -Dsize=${size} \
    -DruntimeInSeconds=${runtime_in_seconds} \
    -Dtarget=${target} \
    -DrestTarget=${rest_target} \
    -Dmachine=${machine} \
    -jar performance-test-0.0.1-RELEASE.jar
done

sudo shutdown -h
