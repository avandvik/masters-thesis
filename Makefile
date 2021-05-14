sims=-g
export sims

test:
	mvn clean test -P local-simple

compile:
	mvn clean compile -P local-simple

package:
	mvn clean package -P local-complete

coverage-report:
	mvn clean test -P local-coverage

upload-jar:
	./shell/upload_jar.sh

upload-instances:
	./shell/upload_instances.sh

upload-infra:
	scp shell/run.sh anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/
	scp Makefile anderhva@solstorm-login.iot.ntnu.no:/home/anderhva/masters-thesis/

run:
	./run.sh $(sims)

get-results:
	scp -r anderhva@solstorm-login.iot.ntnu.no:/storage/users/anderhva/* /Users/andersvandvik/Repositories/masters-thesis/output/solstorm/

plot-results:
	./shell/plot_solution.sh