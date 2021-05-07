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

run:
	./run.sh

get-results:
	scp -r anderhva@solstorm-login.iot.ntnu.no:/storage/users/anderhva/* /Users/andersvandvik/Repositories/masters-thesis/output/solstorm/

plot-results:
	./shell/plot_solution.sh