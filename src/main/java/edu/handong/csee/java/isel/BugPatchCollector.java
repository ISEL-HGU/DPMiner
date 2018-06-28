package edu.handong.csee.java.isel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BugPatchCollector {

	public static void main(String[] args) {
		File directory = new File("/Users/imseongbin/documents/Java/BugPatchCollector");
		BugPatchCollector bc = new BugPatchCollector();
		
		
		try {

			Git git = Git.open(directory);
			Repository repository = git.getRepository();
			
			String[] messages = bc.getCommitMessages(git);
			bc.printGitDiff(git);
			

		} catch (Exception e) {
			System.out.println(e.fillInStackTrace());
		}
	}

	private String[] getCommitMessages(Git git) throws NoHeadException, GitAPIException {
		String[] commitMessages = null;

		Iterable<RevCommit> log = git.log().call();
		for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
			RevCommit rev = iterator.next();
			System.out.println(rev.getFullMessage());
		}

		return commitMessages;
	}
	
	private void printGitDiff(Git git) throws GitAPIException {
		git.diff().setOutputStream( System.out ).call();
	}
}
