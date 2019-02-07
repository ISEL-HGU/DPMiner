package edu.handong.csee.isel.Runner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class Test {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {

		Git git = Git.open(new File("/Users/imseongbin/java/Temp"));
		Repository repo = git.getRepository();

		Iterable<RevCommit> logs = git.log().call();

		for (RevCommit rev : logs) {
			RevCommit parent = rev.getParent(0);

			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
			df.setRepository(repo);
			df.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS));
			df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
			df.setDetectRenames(true);

			List<DiffEntry> diffs;

			try {

				diffs = df.scan(parent.getTree(), rev.getTree());

				for (DiffEntry diff : diffs) {
					String oldPath = diff.getOldPath();
					String newPath = diff.getNewPath();
					System.out.println(rev.getFullMessage());
					System.out.println(rev.getId().name());
					System.out.println(Utils.fetchBlob(repo, rev.getId().getName(),newPath));
					
				}
			} catch (Exception e) {
			}
		}
	}

}
