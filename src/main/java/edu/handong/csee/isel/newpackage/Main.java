package edu.handong.csee.isel.newpackage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Main {

	// TODO: change reference instruction
	public static void main(String[] args) throws Exception {
		// 1. Jira on
		// 2. Github issuePages
		// 3. commit name

		boolean jira = false;
		boolean issuePages = true;
		boolean commitName = false;

		/* input */
		final String URL = "https://github.com/apache/zookeeper";
//		final String URL = "https://github.com/zxing/zxing";
		final String REMOTE_URI = URL + ".git";
		final String projectName = Utils.getProjectName(REMOTE_URI);
		final String reference = "/Users/imseongbin/Desktop/zookeeperhelp.csv";
		final String label = null;
		final String[] headers = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author", "patch" };
		String outPath = "/Users/imseongbin/Desktop";

		if (!outPath.endsWith(File.separator))
			outPath += File.separator;
		final int min = -1;
		final int max = -1;

		/* settings */
		HashSet<String> keywords = null;
		if (jira)
			keywords = Utils.parseReference(reference);
		HashSet<String> keyHashes = null;

		try {
			if (issuePages)
				keyHashes = Utils.parseGithubIssues(URL, label);
		} catch (NoIssuePagesException e) {
			issuePages = false;
			commitName = true;
		}

		Git git = Utils.gitClone(REMOTE_URI);
		Repository repo = git.getRepository();
		RevWalk walk = new RevWalk(repo);
		CSVmaker printer = new CSVmaker(new File(outPath + projectName + ".csv"), headers);

		for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
			if (entry.getKey().contains("refs/heads/master")) { // only master
				Ref ref = entry.getValue();
				RevCommit commit = walk.parseCommit(ref.getObjectId());
				walk.markStart(commit);
			}
		}

		Pattern keyPattern = Pattern.compile("\\[?(\\w+\\-\\d+)\\]?");
		Pattern bugMessagePattern = Pattern.compile("fix|bug|resolved|solved|solution");

		/* start */
		int count = 0;
		for (RevCommit commit : walk) {
			try {
				RevCommit parent = commit.getParent(0);

				if (jira) {
					Matcher m = null;
					if (parent.getShortMessage().length() > 20)
						m = keyPattern.matcher(parent.getShortMessage().substring(0, 20)); // check if have keyword in
																							// Short message
					else
						m = keyPattern.matcher(parent.getShortMessage()); // check if have keyword in Short message
					if (!m.find())
						continue;
					String key = m.group(1);
					if (!keywords.contains(key))
						continue;
				} else if (issuePages) {
					if (!keyHashes.contains(parent.getId().name()))
						continue;
				} else if (commitName) {
					Matcher m = bugMessagePattern.matcher(parent.name());
					if (!m.find())
						continue;
				}

				/* TODO: remove indentations of diffs */
				final List<DiffEntry> diffs = git.diff()
						.setOldTree(Utils.prepareTreeParser(repo, commit.getId().name()))
						.setNewTree(Utils.prepareTreeParser(repo, parent.getId().name())).call();

				for (DiffEntry diff : diffs) {

					String patch = null;
					if ((patch = passConditions(diff, repo, min, max)) == null) // if cannot pass on conditions
						continue;
					Data data = new Data(projectName, parent.name(), parent.getShortMessage(),
							parent.getAuthorIdent().getWhen(), parent.getAuthorIdent().getName(), patch);
					printer.write(data);
					count++;

				}

			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
//		System.out.println(count);
	}

	/**
	 * 
	 * @param diff
	 * @param repository
	 * @param min
	 * @param max
	 * @return if cannot pass conditions, return null. else, return patch
	 * @throws IOException
	 */
	public static String passConditions(DiffEntry diff, Repository repository, int min, int max) throws IOException {

		String patch = null;
		switch (diff.getChangeType().ordinal()) {
		case 0: // ADD
			break;
		case 1: // MODIFY
			if (!diff.getNewPath().endsWith(".java")) // only .java format
				break;

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try (DiffFormatter formatter = new DiffFormatter(output)) {
				formatter.setRepository(repository);
				formatter.format(diff);
			}
			output.flush();
			output.close();
			patch = output.toString("UTF-8");
			if (patch.equals("") || (max != -1) && (min != -1) && Utils.isExceedcondition(patch, max, min))
				return null;

		case 2: // DELETE
			break;
		}

		return patch;
	}
}
