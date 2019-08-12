package edu.handong.csee.isel.patch.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import edu.handong.csee.isel.patch.parser.githubparser.NoIssuePagesException;
import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.Utils;

public class PatchCollector {

	final String URL;
	final String REMOTE_URI;
	final String projectName;
	final String reference;
	final String label;
	final String outPath;
	final static String[] headers = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author", "patch" };

	final int min;
	final int max;
	PatchParseType type;
	final boolean isPatchSize;

	public PatchCollector(String URL, String outPath, String reference, PatchParseType type, int min, int max,
			String label) {
		this.URL = URL;
		this.REMOTE_URI = URL + ".git";
		if (!outPath.endsWith(File.separator))
			outPath += File.separator;
		this.outPath = outPath;
		this.reference = reference;
		this.type = type;
		this.min = min;
		this.max = max;
		this.isPatchSize = (min != -1 && max != -1);
		this.label = label;
		this.projectName = Utils.getProjectName(REMOTE_URI);
	}

	PatchCollector(String URL, String outPath, String reference, PatchParseType type, int min, int max) {
		this(URL, outPath, reference, type, min, max, null);
	}

	PatchCollector(String URL, String outPath, String reference, PatchParseType type, String label) {
		this(URL, outPath, reference, type, -1, -1, label);
	}

	PatchCollector(String URL, String outPath, String reference, PatchParseType type) {
		this(URL, outPath, reference, type, -1, -1, null);
	}

	// TODO: change reference instruction
	public void collect() throws Exception {

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
				type = PatchParseType.Keywords;
			}
			break;
		default:
			break;
		}

		Pattern keyPattern = Pattern.compile("\\[?(\\w+\\-\\d+)\\]?");
		Pattern bugMessagePattern = Pattern.compile("fix|bug|resolved", Pattern.CASE_INSENSITIVE);

		/* start */
//		int count = 0;
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
				int patchSize = 0;
				ArrayList<String> patches = new ArrayList<String>();

				for (DiffEntry diff : diffs) {
					String patch = getPatch(diff, repo);
					if (patch == null)
						continue;
					patches.add(patch);
					int numLines = Utils.parseNumOfDiffLine(patch);
					patchSize += numLines;
					if (isPatchSize && patchSize > max) // for speed
						break;
				}

				if (isPatchSize && (patchSize < min || patchSize > max))
					continue;

				for (String patch : patches) {

					Patch data = new Patch(projectName, commit.name(), commit.getShortMessage(),
							commit.getAuthorIdent().getWhen(), commit.getAuthorIdent().getName(), patch);
					writer.write(data);
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
		}
	}

	public static String getPatch(DiffEntry diff, Repository repository) throws IOException {

		String patch = null;
		if (!diff.getNewPath().endsWith(".java")) // only .java format
			return null;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (DiffFormatter formatter = new DiffFormatter(output)) {
			formatter.setRepository(repository);
			formatter.format(diff);
		}
		output.flush();
		output.close();
		patch = output.toString("UTF-8");

		return patch;
	}
}
