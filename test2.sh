#!/bin/sh
  
# Start the server process and put it in the background
/server.sh localhost 7000 3 localhost,localhost 7001,7002 2 2 &
sleep 5
/server.sh localhost 7001 3 localhost,localhost 7000,7002 2 2 &
sleep 5 

# Start the clients process
java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7001 -update testData,10 &
sleep 5

java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7001 -read testData &
sleep 3

/server.sh localhost 7002 3 localhost,localhost 7001,7000 2 2 &
sleep 5

java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7002 -read testData

