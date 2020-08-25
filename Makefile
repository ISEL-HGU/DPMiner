build:
	gradle build
	unzip ./build/distributions/BugPatchCollector.zip -d run                                                                                                                  
  
unzip: 
	rm -rf run
	unzip ./build/distributions/BugPatchCollector.zip
  
run:
	./BugPatchCollector/bin/BugPatchCollector $(args) 
  
clean:
	rm -rf run
	rm -rf build
