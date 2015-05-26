compile:
	mkdir -p build
	$$JAVA/bin/javac -d build $$(ls *.java)
run: compile
	$$JAVA/bin/java -Dprism.order=sw -cp build/ Main

.PHONY: compile run
