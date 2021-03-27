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