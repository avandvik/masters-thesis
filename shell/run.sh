#!/bin/bash

current_time=$(date +"%d%m%y-%H%M%S")
echo "Results will be stored in $current_time"
export current_time
cd /storage/users/anderhva || exit
mkdir "$current_time"
cd /home/anderhva/masters-thesis || exit

module load Java/11.0.2
module load gurobi/9.1

nbr_sims="$1"
export nbr_sims
cool_down=40
export cool_down

for file_path in ./instances/*
do
  for i in $(seq "$nbr_sims" $END)
  do
    file_name="$(basename -- "$file_path")"
    echo "Running $file_name ($i/$nbr_sims)"
    java -Xmx384g -jar masters-thesis.jar "$current_time" "$file_name"
    echo "Sleeping for $cool_down seconds to cool down..."
    sleep "$cool_down"
  done
done