dir_name=-g
export dir_name

test:
	mvn clean test -P local-simple

package:
	mvn clean package -P local-complete

coverage-report:
	mvn clean test -P local-coverage

upload-jar:
	./shell/upload.sh

run:
	./run.sh

get-results:
	scp -r anderhva@solstorm-login.iot.ntnu.no:/storage/users/anderhva/$(dir_name) /Users/andersvandvik/Repositories/masters-thesis/output/solstorm/$(dir_name)

activate-venv:
	source ./venv/bin/activate