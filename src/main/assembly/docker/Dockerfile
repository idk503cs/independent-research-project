FROM ubuntu:22.04

# Repo used to store performance test data
ARG repo

RUN apt update
RUN DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre git-all openssh-server ssh
WORKDIR /root
RUN mkdir /.ssh
COPY keys .ssh
COPY performance-test-0.0.1-RELEASE-jar-with-dependencies.jar performance-test-0.0.1-RELEASE-jar-with-dependencies.jar
COPY performance-test-0.0.1-RELEASE.jar performance-test-0.0.1-RELEASE.jar
COPY run_client.sh run_client.sh
COPY run_performance_test.sh run_performance_test.sh
RUN chmod 400 .ssh/*
RUN ssh-keyscan github.com >> .ssh/known_hosts
RUN git clone $repo
