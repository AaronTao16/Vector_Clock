#!/bin/sh

java -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar -host $1 -port $2 -N $3 -hosts $4 -ports $5 -Nr $6 -Nw $7
# java -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar -host localhost -port 7001 -N 3 -hosts localhost,localhost -ports 7000,7002 -Nr 2 -Nw 2
