#!/bin/bash

cd ~/Repositories/masters-thesis || exit
mvn clean package -P local-complete
scp target/masters-thesis.jar anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis