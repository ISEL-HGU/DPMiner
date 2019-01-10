package edu.handong.csee.isel.commitUnitMetrics;

import java.io.File;
import java.io.IOException;
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
		int i = 0;
		try {
			git = Git.open(new File(inputPath));		
			Iterable<RevCommit> initialCommits = git.log().call();
			
			repo = git.getRepository();
			
			for(RevCommit commit : initialCommits) {
				RevCommit parent = commit.getParent(0);
				if(parent == null) continue;
				
	            AbstractTreeIterator oldTreeParser = Utils.prepareTreeParser(repo, parent.getId().name().toString());
	            AbstractTreeIterator newTreeParser = Utils.prepareTreeParser(repo, commit.getId().name().toString());
				
	            List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).setPathFilter(PathFilter.create("README.md")).call();
	            
	            for(DiffEntry entry : diff) {
                    System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                    try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                        formatter.setRepository(repo);
                        formatter.format(entry);
                    }
	            }
				i++;
				if( i == 2) break;
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
	
	void parser(String commit) {
		
	}
}
