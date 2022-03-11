FROM ubuntu:18.04
LABEL MAINTAINER="Yi Tao"
USER root 

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV PATH $PATH:$JAVA_HOME/bin

# install java
RUN apt-get update && apt-get install -y \
    ssh \
    rsync \
    vim \
    openjdk-8-jdk

ADD ./Client/target/Client-1.0-SNAPSHOT-jar-with-dependencies.jar .
ADD ./Server/target/Server-1.0-SNAPSHOT-jar-with-dependencies.jar .
COPY bootstrap.sh /
RUN chmod +x /bootstrap.sh
COPY server.sh /
RUN chmod +x /server.sh

# start server and clients
CMD ["/bootstrap.sh"]
EXPOSE 8080