package edu.handong.csee.isel.patch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
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
	private ArrayList<String> issueHashList;
	private int conditionMax;
	private int conditionMin;
	private Pattern pattern = null;

	public ArrayList<CommitStatus> getCommitStatusList() {
		return commitStatusList;
	}

	public MyExecutor(String oldCommitHash, String newCommitHash, ArrayList<String> issueHashList, Git git,
			Repository repository, int conditionMax, int conditionMin, Pattern pattern) throws IOException {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
		this.issueHashList = issueHashList;
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

			boolean skip = true;
//
//			if (issueHashList != null) {
//				for (String issueHash : issueHashList) {
//					if (issueHash.trim().length() == 40) { // It mean commit hash length
//
//						if (newCommitHash.equals(issueHash)) {
//							skip = false;
//						}
//					} else {
//						
//						if (newCommit.getFullMessage().contains(issueHash)) {
//							skip = false;
//						}
//					}
//				}
//			} else if (pattern != null) {
//				Matcher matcher = pattern.matcher(newCommit.getFullMessage());
//				if (matcher.find()) {
//					skip = false;
//				}
//			}
			skip = false;
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
				author = newCommit.getAuthorIdent().getName();

				System.out.println("start~!");

				for (File diff : diffFiles.keySet()) {
					if (diff == null)
						continue;
					String patch = p.getStringFromFile(diff);
					if (patch.equals("") || (conditionMax!=0) && (conditionMin!=0) && this.isExceedcondition(patch, conditionMax, conditionMin))
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
		System.out.println("complete.");
	}

	/**
	 * if Lines (start with '+++' or '---') exceed conditionMax, return true
	 */
	private boolean isExceedcondition(String patch, int conditionMax, int conditionMin) {
		Parser parser = new Parser();
		if (parser.parseNumOfDiffLine(patch) > conditionMax || parser.parseNumOfDiffLine(patch) < conditionMin) {
			return true;
		}
		return false;
	}

}
