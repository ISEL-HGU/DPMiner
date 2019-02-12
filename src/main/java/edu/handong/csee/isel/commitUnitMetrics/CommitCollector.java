package edu.handong.csee.isel.commitUnitMetrics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;






public class CommitCollector {
	private String inputPath;
	private String outputPath;
	private Git git;
	private Repository repo;
	
	public CommitCollector(String gitRepositoryPath,String resultDirectory) {
		this.inputPath = gitRepositoryPath;
		this.outputPath = resultDirectory;
	}
	
	void countCommitMetrics() {
		ArrayList<String> line = new ArrayList<String>();
		try {
			git = Git.open(new File(inputPath));		
			Iterable<RevCommit> initialCommits = git.log().call();

			repo = git.getRepository();
			int i = 1;
			for(RevCommit commit : initialCommits) {
				if(commit.getParentCount() == 0) continue;
				//System.out.println("PPPPPP "+commit.getParentCount());
				RevCommit parent = commit.getParent(0);
				if(parent == null) continue;
				
	            AbstractTreeIterator oldTreeParser = Utils.prepareTreeParser(repo, parent.getId().name().toString());
	            AbstractTreeIterator newTreeParser = Utils.prepareTreeParser(repo, commit.getId().name().toString());
				
	            List<DiffEntry> diff = git.diff()
	            		.setOldTree(oldTreeParser)
	            		.setNewTree(newTreeParser)
	            		//.setPathFilter(PathFilter.create("README.md")) //원하는 소스파일만 본다.
	            		.call();
	            
	            for(DiffEntry entry : diff) {
                    System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                    try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                        formatter.setRepository(repo);
                        
                        formatter.format(entry);
                        System.out.println(formatter);
                        //System.out.println(i);
                    }
	            }
	            
			}
			System.out.println(initialCommits);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		} catch (NoHeadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (GitAPIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		
	}
	
	void parser(String commit) {
		
	}
}
