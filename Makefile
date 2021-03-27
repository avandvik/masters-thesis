test:
	mvn clean test -P local-simple

package:
	mvn clean package -P local-complete

coverage-report:
	mvn clean test -P local-coverage

upload-project:
	scp target/masters-thesis.jar anderhva@solstorm-login.iot.ntnu.no:/home/anderhva