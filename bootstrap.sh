#!/bin/sh
  
# Start the server process and put it in the background
/server.sh localhost 7000 3 localhost,localhost 7001,7002 2 2 &
/server.sh localhost 7001 3 localhost,localhost 7000,7002 2 2 &
/server.sh localhost 7002 3 localhost,localhost 7001,7000 2 2 &
#/server.sh localhost 7000 5 localhost,localhost,localhost,localhost 7001,7002,7003,7004 &
#/server.sh localhost 7001 5 localhost,localhost,localhost,localhost 7000,7002,7003,7004 &
#/server.sh localhost 7002 5 localhost,localhost,localhost,localhost 7001,7000,7003,7004 &
#/server.sh localhost 7003 5 localhost,localhost,localhost,localhost 7001,7002,7000,7004 &
#/server.sh localhost 7004 5 localhost,localhost,localhost,localhost 7001,7002,7003,7000 &
sleep 5

# Start the clients process
java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7000 -update testData,10
java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7001 -read testData
sleep 2

# concurrently update value from same key
#java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7002 -update testData,5 &
#java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7003 -update testData,4
#java -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar -server_ip localhost -port 7001 -read testData

