#!/bin/sh
  
# Start the server process and put it in the background
/server.sh localhost 7000 3 localhost,localhost 7001,7002 2 2 &
sleep 5

# Start the clients process 
# UPDATE
java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7000 -update testData,10
sleep 2