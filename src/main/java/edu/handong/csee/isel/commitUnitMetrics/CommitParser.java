package edu.handong.csee.isel.commitUnitMetrics;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

public class CommitParser {
	String inputPath;
	String outputPath;
	
	public CommitParser(String gitRepositoryPath,String resultDirectory) {
		this.inputPath = gitRepositoryPath;
		this.outputPath = resultDirectory;
	}
	
	
	//커밋 저장
	//커밋에 해당하는 매트릭 카운트  (소스파일이 여러개 포함되어 있는데 어떻게?)
	//카운트한 값을 arff형식에 맞춰 프린트 
	
	void countCommitMetrics() {
		try {
			Repository  rep = new FileRepository(inputPath + "/.git");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
