package edu.handong.csee.isel.commitUnitMetrics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class CommitCollector {
	String inputPath;
	String outputPath;
	
	private Git git;
	
	public CommitCollector(String gitRepositoryPath,String resultDirectory) {
		this.inputPath = gitRepositoryPath;
		this.outputPath = resultDirectory;
	}
	
	void countCommitMetrics() {
		
		try {
			git = Git.open(new File(inputPath));
			
			Iterable<RevCommit> initialCommits = git.log().call();
			
			
			for(RevCommit commit : initialCommits) {
				System.out.println(commit.getShortMessage());
				break;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
