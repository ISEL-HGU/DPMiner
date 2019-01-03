package edu.handong.csee.isel.commitUnitMetrics;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class CommitCollector {
	String inputPath;
	String outputPath;
	
	public CommitCollector(String gitRepositoryPath,String resultDirectory) {
		this.inputPath = gitRepositoryPath;
		this.outputPath = resultDirectory;
	}
	
	void countCommitMetrics() {
	
	}
}
