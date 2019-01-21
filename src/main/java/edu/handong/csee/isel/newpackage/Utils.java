package edu.handong.csee.isel.newpackage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import edu.handong.csee.isel.githubcommitparser.CommitParser;
import edu.handong.csee.isel.githubcommitparser.IssueLinkParser;

public class Utils {
	public static Git gitClone(String REMOTE_URI)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		File repositoriesDir = new File("repositories" + File.separator + getProjectName(REMOTE_URI));

		if (repositoriesDir.exists()) {
			return Git.open(repositoriesDir);
		}

		repositoriesDir.mkdirs();
		return Git.cloneRepository().setURI(REMOTE_URI).setDirectory(repositoriesDir)
//				  .setBranch("refs/heads/master") // only master
				.setCloneAllBranches(true).call();
	}

	public static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		// from the commit we can build the tree which allows us to construct the
		// TreeParser
		// noinspection Duplicates
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(repository.resolve(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}

	public static boolean isExceedcondition(String patch, int conditionMax, int conditionMin) {
		int line_count = parseNumOfDiffLine(patch);
		if (line_count > conditionMax || line_count < conditionMin) {
			return true;
		}
		return false;
	}

	private static boolean isStartWithPlus(String str) {
		if (str.startsWith("+")) {
			if (str.startsWith("+++"))
				return false;
			return true;
		}
		return false;
	}

	private static boolean isStartWithMinus(String str) {
		if (str.startsWith("-")) {
			if (str.startsWith("---"))
				return false;
			return true;
		}
		return false;
	}

	private static int parseNumOfDiffLine(String inStr) {
		int count = 0;
		String[] newStrings = inStr.split("\n");
		for (String str : newStrings) {
			if (isStartWithMinus(str) || isStartWithPlus(str)) {
				count++;
			}
		}

		return count;
	}

	public static HashSet<String> parseReference(String reference) throws IOException {
		HashSet<String> keywords = new HashSet<String>();
		File CSV = new File(reference);
		Reader reader = new FileReader(CSV);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
		boolean first = true;

		for (CSVRecord record : records) {
			if (first) {
				first = false;
				continue;
			}
			keywords.add(record.get(1));
		}
		return keywords;
	}

	public static HashSet<String> parseGithubIssues(String URL, String label) throws NoIssuePagesException {
		IssueLinkParser iss = new IssueLinkParser();
		CommitParser co = new CommitParser();

		try {
			iss.parseIssueAddress(URL, label);
			if (IssueLinkParser.issueAddress.size() == 0) {
				throw new Exception("");
			}
			// CommitParser돌림
			co.parseCommitAddress(URL);
		} catch (Exception e) {
			throw new NoIssuePagesException("There is not issue-space in " + URL);
		}

		return co.getCommitAddress();
	}

	public static String getProjectName(String URI) {
		Pattern p = Pattern.compile(".*/(\\w+)\\.git");
		Matcher m = p.matcher(URI);
		m.find();
		return m.group(1);

	}
}
