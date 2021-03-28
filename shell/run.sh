#!/bin/bash

current_time=$(date +"%d%m%y-%H%M%S")
echo "Results will be stored in $current_time"
export current_time
cd /storage/users/anderhva || exit
mkdir "$current_time"
cd /home/anderhva/masters-thesis || exit

module load Java/11.0.2
module load gurobi/9.1
java -jar masters-thesis.jar "$current_time"