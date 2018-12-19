package edu.handong.csee.isel.patch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import edu.handong.csee.isel.parsers.Parser;

public class MyExecutor extends Thread {
	private ArrayList<CommitStatus> commitStatusList;
	private String oldCommitHash;
	private String newCommitHash;
	private Git git;
	private Repository repository;
	private Pattern issuePattern;
	private int conditionMax = -1;
	private int conditionMin = -1;
	private Pattern pattern = null;
//	public static HashSet<String> authors = new HashSet<String>();

	public ArrayList<CommitStatus> getCommitStatusList() {
		return commitStatusList;
	}

	public MyExecutor(String oldCommitHash, String newCommitHash, Pattern issuePattern, Git git,
			Repository repository, int conditionMax, int conditionMin, Pattern pattern) throws IOException {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
		this.issuePattern = issuePattern;
		this.git = git;
		this.repository = repository;
		this.conditionMax = conditionMax;
		this.conditionMin = conditionMin;
		this.pattern = pattern;
	}

	@Override
	public void run() {
		CommitStatus newCommitStatus = null;
		commitStatusList = new ArrayList<CommitStatus>();
		try {
			RevWalk walk = new RevWalk(repository);
			RevCommit newCommit = walk.parseCommit(repository.resolve(newCommitHash));

//			authors.add(newCommit.getAuthorIdent().getName()+"<"+newCommit.getAuthorIdent().getEmailAddress()+">");
			boolean skip = true;

			
			if(issuePattern != null) {
//				Matcher m = issuePattern.matcher(newCommitHash);
				Matcher m = issuePattern.matcher(newCommit.getFullMessage());
				if(m.find()) {
					skip = false;
				}
			} else if (pattern != null) {
				Matcher matcher = pattern.matcher(newCommit.getFullMessage());
				if (matcher.find()) {
					skip = false;
				}
			}
			if (skip) {
				commitStatusList = null;
			} else {
				Patch p = new Patch(git, repository);
				HashMap<File, String> diffFiles = null;
				diffFiles = p.pullDiffs(oldCommitHash, newCommitHash);

				String project = "";
				String shortMessage = "";
				String commitHash = "";
				int date = 0;
				String author = "";

				project = repository.getDirectory().getParentFile().getName();
				shortMessage = newCommit.getShortMessage();
				commitHash = newCommitHash;
				date = newCommit.getCommitTime();
				author = newCommit.getAuthorIdent().getName()+"<"+newCommit.getAuthorIdent().getEmailAddress()+">";

				for (File diff : diffFiles.keySet()) {
					if (diff == null)
						continue;
					String patch = p.getStringFromFile(diff);
					if (patch.equals("") || (conditionMax != -1) && (conditionMin != -1)
							&& this.isExceedcondition(patch, conditionMax, conditionMin))
						continue;
					String path = diffFiles.get(diff);
					newCommitStatus = null;
					newCommitStatus = new CommitStatus(project, shortMessage, commitHash, date, author, path, patch);
					commitStatusList.add(newCommitStatus);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(newCommitStatus.toString());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * if Lines (start with '+++' or '---') exceed conditionMax, return true
	 */
	private boolean isExceedcondition(String patch, int conditionMax, int conditionMin) {
		Parser parser = new Parser();
		int line_count = parser.parseNumOfDiffLine(patch);
		if (line_count > conditionMax || line_count < conditionMin) {
			return true;
		}
		return false;
	}

}
