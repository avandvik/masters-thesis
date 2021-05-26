#!/bin/bash

# Upload instances
ssh -i ~/.ssh/id_rsa anderhva@solstorm-login.iot.ntnu.no "rm -rf /home/anderhva/masters-thesis/instances; exit;"
scp -prq src/main/resources/instances/. anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/instances/