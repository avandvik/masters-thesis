test:
	mvn clean test -P local-simple

coverage-report:
	mvn clean test -P local-coverage

upload-jar:
	./shell/upload.sh