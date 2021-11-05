sims=-g
export sims
jar=-g
export jar

test:
	mvn clean test -P local-simple

compile:
	mvn clean compile -P local-simple

package:
	mvn clean package -P local-complete

coverage-report:
	mvn clean test -P local-coverage

upload:
	./shell/upload.sh $(jar)

upload-instances:
	./shell/upload_instances.sh

run:
	./run.sh $(sims) $(jar)

get-results:
	scp -r anderhva@solstorm-login.iot.ntnu.no:/storage/users/anderhva/* /Users/andersvandvik/Repositories/masters-thesis/output/new_solstorm/

plot-results:
	./shell/plot_solution.sh