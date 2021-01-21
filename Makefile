build:
	gradle build
	unzip ./build/distributions/DPMiner.zip -d run                                                                                                                  
  
unzip: 
	rm -rf run
	unzip ./build/distributions/DPMiner.zip
  
run:
	./BugPatchCollector/bin/DPMiner $(args) 
  
clean:
	rm -rf run
	rm -rf build
