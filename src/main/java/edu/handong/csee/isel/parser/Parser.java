package edu.handong.csee.isel.parser;

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

import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.NoIssuePagesException;
import edu.handong.csee.isel.utils.Utils;

public class Parser {

	final String URL;
	final String REMOTE_URI;
	final String projectName;
	final String reference;
	final String label;
	final String outPath;
	final String[] headers = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author", "patch" };

	final int min;
	final int max;
	ParseType type;

	public Parser(String URL, String outPath, String reference, ParseType type, int min, int max, String label) {
		this.URL = URL;
		this.REMOTE_URI = URL + ".git";
		if (!outPath.endsWith(File.separator))
			outPath += File.separator;
		this.outPath = outPath;
		this.reference = reference;
		this.type = type;
		this.min = min;
		this.max = max;
		this.label = label;
		this.projectName = Utils.getProjectName(REMOTE_URI);
	}

	Parser(String URL, String outPath, String reference, ParseType type, int min, int max) {
		this(URL, outPath, reference, type, min, max, null);
	}

	Parser(String URL, String outPath, String reference, ParseType type, String label) {
		this(URL, outPath, reference, type, -1, -1, label);
	}

	Parser(String URL, String outPath, String reference, ParseType type) {
		this(URL, outPath, reference, type, -1, -1, null);
	}

	// TODO: change reference instruction
	public void parse() throws Exception {

		/* settings */
		HashSet<String> keywords = null;
		HashSet<String> keyHashes = null;

		Git git = Utils.gitClone(REMOTE_URI);
		Repository repo = git.getRepository();
		RevWalk walk = new RevWalk(repo);
		CSVmaker writer = new CSVmaker(new File(outPath + projectName + ".csv"), headers);

		for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
			if (entry.getKey().contains("refs/heads/master")) { // only master
				Ref ref = entry.getValue();
				RevCommit commit = walk.parseCommit(ref.getObjectId());
				walk.markStart(commit);
			}
		}

		switch (type) {
		case Jira:
			keywords = Utils.parseReference(reference);
			break;
		case GitHub:
			try {
				keyHashes = Utils.parseGithubIssues(URL, label);
			} catch (NoIssuePagesException e) {
				type = ParseType.Keywords;
			}
			break;
		default:
			break;
		}

		Pattern keyPattern = Pattern.compile("\\[?(\\w+\\-\\d+)\\]?");
		Pattern bugMessagePattern = Pattern.compile("fix|bug|resolved|solved", Pattern.CASE_INSENSITIVE);

		/* start */
		int count = 0;
		for (RevCommit commit : walk) {
			try {
				RevCommit parent = commit.getParent(0);

				switch (type) {
				case Jira:
					Matcher m = null;
					if (commit.getShortMessage().length() > 20)
						m = keyPattern.matcher(commit.getShortMessage().substring(0, 20)); // check if have keyword in
																							// Short message
					else
						m = keyPattern.matcher(commit.getShortMessage()); // check if have keyword in Short message
					if (!m.find())
						continue;
					String key = m.group(1);
					if (!keywords.contains(key))
						continue;
					break;

				case GitHub:
					if (!keyHashes.contains(commit.getId().name()))
						continue;
					break;

				case Keywords:
					m = bugMessagePattern.matcher(commit.getFullMessage());
					if (!m.find())
						continue;
					break;
				}

				final List<DiffEntry> diffs = git.diff()
						.setOldTree(Utils.prepareTreeParser(repo, parent.getId().name()))
						.setNewTree(Utils.prepareTreeParser(repo, commit.getId().name())).call();

				for (DiffEntry diff : diffs) {

					String patch = null;
					if ((patch = passConditions(diff, repo, min, max)) == null) // if cannot pass on conditions
						continue;
					Data data = new Data(projectName, commit.name(), commit.getShortMessage(),
							commit.getAuthorIdent().getWhen(), commit.getAuthorIdent().getName(), patch);
					writer.write(data);
//					count++;

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
			if (patch.equals("") || (max != -1) && (min != -1) && Utils.isExceededcondition(patch, max, min))
				return null;

		case 2: // DELETE
			break;
		}

		return patch;
	}
}
