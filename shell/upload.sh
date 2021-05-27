#!/bin/bash

# Upload instances
ssh -i ~/.ssh/id_rsa anderhva@solstorm-login.iot.ntnu.no "rm -rf /home/anderhva/masters-thesis/instances; exit;"
scp -prq src/main/resources/instances/. anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/instances/

# Upload constant
ssh -i ~/.ssh/id_rsa anderhva@solstorm-login.iot.ntnu.no "rm -rf /home/anderhva/masters-thesis/constant; exit;"
scp -prq src/main/resources/constant/. anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/constant/

# Upload infrastructure files
scp -q shell/run.sh anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/
scp -q Makefile anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/

# Make and upload jar
jar="$1"
export jar
cd ~/Repositories/masters-thesis || exit
mvn clean package -P local-complete
scp -q target/"$jar" anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis