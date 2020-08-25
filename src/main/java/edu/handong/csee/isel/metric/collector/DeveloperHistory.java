package edu.handong.csee.isel.metric.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.Main;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.Utils;

public class DeveloperHistory {
	private Git git;
	private Repository repo;
	String startDate;
	String endDate;
	String midDate;
	double percent;
	
	public DeveloperHistory(){
		try {
			this.git = Git.open(Main.getGitDirectory());
			this.repo = git.getRepository();
			if(Input.startDate == null) startDate = "0000-00-00 00:00:00";
			else this.startDate = Input.startDate;
			
			if(Input.endDate == null) endDate = "9999-99-99 99:99:99";
			else this.endDate = Input.endDate;
			
			this.percent = (double)Input.percent;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String findDeveloperDate() {
		midDate = null;
		ArrayList<String> newDeveloper = new ArrayList<String>();
		ArrayList<String> dateOfCameIn = new ArrayList<String>();
		ArrayList<RevCommit> commits = new ArrayList<RevCommit>();
		
		Iterable<RevCommit> initialCommits = null;
		try {
			initialCommits = git.log().call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;
		
		for (RevCommit initialCommit : initialCommits) {
			commits.add(count,initialCommit);
			count++;
		}
		
		for (int commitIndex = commits.size()-1; commitIndex > -1; commitIndex--) {
			RevCommit commit = commits.get(commitIndex);
			
			String commitTime = Utils.getStringDateTimeFromCommitTime(commit.getCommitTime());//커밋 날짜 yyyy-MM-dd HH:mm:ss
			if(!(startDate.compareTo(commitTime)<=0 && endDate.compareTo(commitTime)>=0))
				continue;
			
			if (commit.getParentCount() == 0) continue;
			RevCommit parent = commit.getParent(0);
			if (parent == null)
				continue;

			//source
			List<DiffEntry> diffs = Utils.diff(parent, commit, repo);
			boolean istherejavafile = false;
			
			for (DiffEntry diff : diffs) {
	
				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();
				
				if (oldPath.equals("/dev/null") || newPath.indexOf("Test") >= 0 || !newPath.endsWith(".java"))
					continue;
				istherejavafile = true;
			}
			
			if(istherejavafile == false) continue;
			
			String authorId = Utils.parseAuthorID(commit.getAuthorIdent().toString());
			
			if(!newDeveloper.contains(authorId)) {
				newDeveloper.add(authorId);
				dateOfCameIn.add(commitTime);
			}
		}
		percent = (double) (percent * 0.1); // 0.2~0.9 (20% ~ 90%)
		double halfDeveloper = Math.ceil((double)newDeveloper.size() * percent);
		return midDate = dateOfCameIn.get((int)halfDeveloper);
	}

	public String getMidDate() {
		return midDate;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}
	
	

}
