package edu.handong.csee.java.isel;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class BugPatchCollector {

	public static void main(String[] args) {
		File directory = new File("/Users/imseongbin/documents/Java/BugPatchCollector");
		BugPatchCollector bc = new BugPatchCollector();

		try {

			Git git = Git.open(directory);
			Repository repository = git.getRepository();

			ArrayList<String> messages = bc.getCommitMessages(git);
			for (String message : messages) {
				System.out.println(message);
			}

			// bc.printGitDiff(git);

		} catch (Exception e) {
			System.out.println(e.fillInStackTrace());
		}
	}

	private ArrayList<String> getCommitMessages(Git git) throws Exception {
		ArrayList<String> commitMessages = new ArrayList<String>();

		Iterable<RevCommit> log = git.log().call();
		for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
			RevCommit rev = iterator.next();
			commitMessages.add(rev.getFullMessage());
		}

		return commitMessages;
	}

	private void printGitDiff(Git git) throws GitAPIException {
		git.diff().setOutputStream(System.out).call();
	}
}
