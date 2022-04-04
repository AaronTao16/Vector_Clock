#!/bin/sh

# Start the server process and put it in the background
java -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar -host localhost -port 7000 -N 3 -hosts localhost,localhost -ports 7001,7002 -Nr 2 -Nw 2 &
S_ID=$!
sleep 5

/server.sh localhost 7001 3 localhost,localhost 7000,7002 2 2 &
sleep 5 

# Start the clients process
/server.sh localhost 7002 3 localhost,localhost 7001,7000 2 2 &
sleep 5

# stop localhost 7000 (leader)
echo S_ID=$S_ID
kill $S_ID

java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7001 -update testData,-1

java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7001 -read testData
