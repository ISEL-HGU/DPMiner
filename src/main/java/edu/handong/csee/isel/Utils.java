package edu.handong.csee.isel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.FollowFilter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.bfc.collector.github.CommitParser;
import edu.handong.csee.isel.bfc.collector.github.IssueLinkParser;
import edu.handong.csee.isel.bfc.collector.github.NoIssuePagesException;
import edu.handong.csee.isel.bic.szz.data.BICInfo;
import edu.handong.csee.isel.data.Input;

public class Utils {

	static public DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS);
	static public RawTextComparator diffComparator = RawTextComparator.WS_IGNORE_ALL;

	static public EditList getEditListFromDiff(Git git, String oldSha1, String newSha1, String path) {

		Repository repo = git.getRepository();

		ObjectId oldId;
		try {
			oldId = repo.resolve(oldSha1 + "^{tree}:");
			ObjectId newId = repo.resolve(newSha1 + "^{tree}");

			ObjectReader reader = repo.newObjectReader();

			// setting for renamed or copied path
			Config config = new Config();
			config.setBoolean("diff", null, "renames", true);
			DiffConfig diffConfig = config.get(DiffConfig.KEY);

			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, oldId);
			CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
			newTreeIter.reset(reader, newId);

			List<DiffEntry> diffs = git.diff().setPathFilter(FollowFilter.create(path, diffConfig))
					.setNewTree(newTreeIter).setOldTree(oldTreeIter).call();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			df.setDiffAlgorithm(diffAlgorithm);
			df.setDiffComparator(diffComparator);
			df.setRepository(repo);

			for (DiffEntry entry : diffs) {

				df.format(entry);
				FileHeader fileHeader = df.toFileHeader(entry);
				if (!fileHeader.getNewPath().equals(path))
					continue;

				df.close();
				return fileHeader.toEditList();
			}

			df.close();

		} catch (IndexOutOfBoundsException e) {

		} catch (RevisionSyntaxException | IOException | GitAPIException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String removeComments(String code) {

		JavaASTParser codeAST = new JavaASTParser(code);
		@SuppressWarnings("unchecked")
		List<Comment> lstComments = codeAST.cUnit.getCommentList();
		for (Comment comment : lstComments) {
			code = replaceCommentsWithWS(code, comment.getStartPosition(), comment.getLength());
		}

		return code;
	}

	private static String replaceCommentsWithWS(String code, int startPosition, int length) {

		String pre = code.substring(0, startPosition);
		String post = code.substring(startPosition + length, code.length());

		String comments = code.substring(startPosition, startPosition + length);

		comments = comments.replaceAll("\\S", " ");

		code = pre + comments + post;

		return code;
	}

	static public EditList getEditListFromDiff(String file1, String file2) {
		RawText rt1 = new RawText(file1.getBytes());
		RawText rt2 = new RawText(file2.getBytes());
		EditList diffList = new EditList();

		diffList.addAll(diffAlgorithm.diff(diffComparator, rt1, rt2));
		return diffList;
	}

	static public String fetchBlob(Repository repo, String revSpec, String path) {

		try {
			// Resolve the revision specification
			final ObjectId id = repo.resolve(revSpec);

			// Makes it simpler to release the allocated resources in one go
			ObjectReader reader = repo.newObjectReader();

			// Get the commit object for that revision
			RevWalk walk = new RevWalk(reader);
			RevCommit commit = walk.parseCommit(id);
			walk.close();

			// Get the revision's file tree
			RevTree tree = commit.getTree();
			// .. and narrow it down to the single file's path
			TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);
			if (treewalk != null) {
				// use the blob id to read the file's data
				byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
				reader.close();
				return new String(data, "utf-8");
			} else {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Git gitClone(String REMOTE_URI)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		File repositoriesDir = new File("repositories" + File.separator + getProjectName(REMOTE_URI));
		Git git = null;
		if (repositoriesDir.exists()) {
			try {
				git = Git.open(repositoriesDir);
			} catch (RepositoryNotFoundException e) {
				if (repositoriesDir.delete()) {
					return gitClone(REMOTE_URI);
				}
			}
		} else {
			repositoriesDir.mkdirs();
			System.out.println("cloning..");
			git = Git.cloneRepository().setURI(REMOTE_URI).setDirectory(repositoriesDir)
//				  .setBranch("refs/heads/master") // only master
					.setCloneAllBranches(true).call();
		}
		return git;
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

	public static boolean isExceededcondition(String patch, int conditionMax, int conditionMin) {
		int line_count = parseNumOfDiffLine(patch);
		if (line_count < conditionMax && line_count > conditionMin) {
			return false;
		}
		return true;
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

	public static int parseNumOfDiffLine(String inStr) {
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

		for (CSVRecord record : records) {
			keywords.add(record.get(0));
//			System.out.println(record.get(0));
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
			// run CommitParser
			co.parseCommitAddress(URL);
		} catch (Exception e) {
			throw new NoIssuePagesException("There is not issue-space in " + URL);
		}

		return co.getCommitAddress();
	}

	public static String getProjectName(String URI) {

		Pattern p = Pattern.compile(".*/(.+)\\.git");
		Matcher m = p.matcher(URI);
		m.find();
//		System.out.println(m.group(1));
		return m.group(1);

	}

	public static String getStringDateTimeFromCommit(RevCommit commit) {

		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date commitDate = commit.getAuthorIdent().getWhen();

		TimeZone GMT = commit.getCommitterIdent().getTimeZone();
		ft.setTimeZone(GMT);

		return ft.format(commitDate);
	}

	public static List<DiffEntry> diff(RevCommit parent, RevCommit commit, Repository repo) {

		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repo);
		df.setDiffAlgorithm(diffAlgorithm);
		df.setDiffComparator(diffComparator);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = null;
		try {
			diffs = df.scan(parent.getTree(), commit.getTree());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return diffs;
	}

	public static String getKeyName(String commitName, String newPath) {

		if (newPath.contains("\\")) {
			newPath = newPath.replace("\\", "-");
		}
		if (newPath.contains("/")) {
			newPath = newPath.replace("/", "-");
		}

		return commitName + "-" + newPath;
	}

	public static boolean isBFC(RevCommit commit, List<String> bfcList) {

		return bfcList.contains(commit.getId().getName());
	}
	
	public static String getStringDateTimeFromCommitTime(int commitTime){
		SimpleDateFormat ft =  new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		Date commitDate = new Date(commitTime* 1000L);

		TimeZone GMT = TimeZone.getTimeZone("GMT");
		ft.setTimeZone(GMT);

		return ft.format(commitDate);
	}
	
	public static String parseAuthorID(String authorId) {
		Pattern pattern = Pattern.compile(".+\\[.+,(.+),.+\\]");
		Matcher matcher = pattern.matcher(authorId);
		while(matcher.find()) {
			authorId = matcher.group(1);
		}
		return authorId;
	}
	
	// added part because of ag-szz
	public static void storeOutputFile(String outPath, String GIT_URL, List<BICInfo> BICLines) throws IOException {
		// Set file name
		String[] arr = GIT_URL.split("/");
		String projName = arr[arr.length - 1];
		
		//System.getProperty("user.dir") : 현재위치 반환 해줌. 
//		String fName = System.getProperty("user.dir") + File.separator + "results" + File.separator + projName + ".csv";
//		String fName = outPath + File.separator + "AGSZZ_"+ Input.projectName + ".csv";	
		String fName = outPath + File.separator + "BIC_AGSZZ_" + Input.projectName + ".csv";
		
		File savedFile = new File(fName);
		savedFile.getParentFile().mkdirs();

		FileWriter writer = new FileWriter(savedFile);

		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("BISha1", "BIPath", "FixSha1",
				"BIDate", "FixDate", "biLineIdx", "BIContent", "Commiter", "Author"));

		for (BICInfo BICInfo : BICLines) {
			csvPrinter.printRecord(BICInfo.getBISha1(), BICInfo.getBiPath(), BICInfo.getFixSha1(), BICInfo.getBIDate(),
					BICInfo.getFixDate(), BICInfo.getBiLineIdx(), BICInfo.getBIContent(), BICInfo.getCommiter(),
					BICInfo.getAuthor());
		}

		csvPrinter.close();
	}
}
