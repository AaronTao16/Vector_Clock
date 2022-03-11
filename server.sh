#!/bin/sh

java -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar -host $1 -port $2 -N $3 -hosts $4 -ports $5
# java -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar -host localhost -port 7000 -N 2 -hosts localhost -ports 7001
