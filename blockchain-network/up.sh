#!/bin/sh
export FABRIC_CFG_PATH=${PWD}
export COMPOSE_PROJECT_NAME=sun

# scripts/setup-certificate.sh
# sleep 2
echo "--- Bringing network down ---"
docker-compose -f docker-compose.yaml down
docker kill $(docker ps -aq)
sleep 2
docker rm $(dokcer ps -aq)
sleep 2
docker rmi -f $(docker images | grep dev-)
sleep 2
echo "--- Bringing network up ---"
docker-compose -f docker-compose.yaml up -d
sleep 20
# docker exec cli ./scripts/setup-channel.sh
# sleep 2
# docker exec cli ./scripts/setup-chaincode.sh
java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 client-0.0.1-SNAPSHOT.jar
exit 0