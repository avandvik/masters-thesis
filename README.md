# An Adaptive Large Neighborhood Search for the TDVRPSO
### Karl Petter Ulsrud and Anders Vandvik

This repository contains the code for the ALNS algorithm outlined in our master's thesis conducted at NTNU during the spring of 2021. The ALNS is adopted to solve the Time-Dependent Vessel Routing Problem with Speed Optimization (TDVRPSO) described in the thesis. It is extended with a local search and a set-partitioning problem to improve solution quality. The thesis contains a study of relevant literature, descriptions of the problem and algorithm implementation and a computational study outlining results. A mathematical formulation of the problem is also provided. The pdf of the thesis is provided in the 'thesis' folder.

## Problem Instances
All problem instances used to generate the results outlined in the thesis can be found in 'src/resources'. The 'constant' folder contains data about installations, vessels and weather. The 'managerial_insights', 'performance' and 'tuning' folders contain specific instance data. The 'instance' folder is the folder the ALNS algorithm looks for instances to solve. To run a specific instance, copy it from 'managerial_insights', 'performance' or 'tuning' and place it in this folder. The 'test' folder contains instances used for the JUnit tests.

## Problem Output
All output files from both the ALNS and the exact Gurobi solver can be found in 'output/solstorm'. A specific ALNS run outputs two files: 'history' and 'solution'. The first contains information about the history of the search, like how the objective evolves through the iterations. The second contains information about the best found solution. 

## Installation
To run the ALNS algorithm locally, ensure that Java is installed. We used the Amazon Coretto JDK 11 Java [distribution](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html). The ALNS can then be run from the Main file in 'src/main/java/alns'. There is also a Makefile with different commands for running tests, building a jar, and so on. Some of the commands, like 'run' is tailored for our use on the computing cluster Solstorm, but other will work more generally, like 'test'. 

To run the exact solver implemented with the Python Gurobi interface, install Gurobi as described on their website and ensure you have a license. 
