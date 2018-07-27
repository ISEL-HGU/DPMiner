package edu.handong.csee.isel.patch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;

public class Patch {

	final String[] header = new String[] { "Project", "ShortMessage", "CommitHash", "Date", "Author",
			"Diff" };
	ArrayList<Map<String, Object>> commits = new ArrayList<Map<String, Object>>();
	String directoryPath;
	File directory;
	Git git;
	Repository repository;
	HashMap<String, ArrayList<String>> commitHashs; // <branch, commitHash>
	HashMap<String, ArrayList<String>> allPathList = new HashMap<String, ArrayList<String>>(); // <commitHash, pathList>
	ArrayList<String> branchList = new ArrayList<String>();

	public HashMap<String, ArrayList<String>> getCommitHashs() {
		return commitHashs;
	}

	public void setCommitHashs(String branch) throws RevisionSyntaxException, NoHeadException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException, GitAPIException, IOException {
		if (branchList.isEmpty() || !branchList.contains(branch)) {
			System.out.println("branch is not!");
			return;
		}
		ArrayList<String> commitHashList = new ArrayList<String>();

		/**/
		Iterable<RevCommit> logs = git.log().call();
		logs = git.log().add(repository.resolve(branch)) // this decide branch in result
				.call();
		for (RevCommit rev : logs) {

			commitHashList.add(rev.getId().getName());

		}
		this.commitHashs.put(branch, commitHashList);

	}

	public void setBranchList() throws GitAPIException, RevisionSyntaxException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException, IOException {
		List<Ref> call = git.branchList().call();
		for (Ref ref : call) {
			branchList.add(ref.getName());
			this.setCommitHashs(ref.getName());
		}
	}

	public ArrayList<String> getBranchList() throws GitAPIException {
		return branchList;
	}

	public HashMap<String, ArrayList<String>> getAllPathList() { // parameter: commitHash
		// TODO Auto-generated method stub
		return allPathList;
	}

	public Patch(String directoryPath) throws IOException, GitAPIException {
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.directoryPath = directoryPath;
		this.directory = new File(directoryPath);
		this.git = Git.open(new File(directoryPath + "/.git"));
		this.repository = this.git.getRepository();
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.setBranchList();
	}

	public void reset() {
		this.directoryPath = null;
		this.directory = null;
		this.git = null;
		this.repository = null;
		this.branchList = null;
		this.commitHashs = null;
	}

	public void set(String directoryPath) throws IOException, GitAPIException {
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.directoryPath = directoryPath;
		this.directory = new File(directoryPath);
		this.git = Git.open(new File(directoryPath + "/.git"));
		this.repository = this.git.getRepository();
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.setBranchList();
	}

	public ArrayList<String> getPathList(String commitHash) throws IOException {
		ArrayList<String> pathList = new ArrayList<String>();

		RevWalk walk = new RevWalk(repository);
		ObjectId id = repository.resolve(commitHash);
		RevCommit commit = walk.parseCommit(id);

		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		while (treeWalk.next()) {
			pathList.add(treeWalk.getPathString());
			// System.out.println(" found: " + treeWalk.getPathString());
		}

		walk.dispose();

		return pathList;
	}

	public void showFileDiff(String oldCommitHash, String newCommitHash) // filePath 지울예정.
			throws IOException, GitAPIException {

		ArrayList<String> pathsOfOldCommit = this.getPathList(oldCommitHash);
		ArrayList<String> pathsOfNewCommit = this.getPathList(newCommitHash);

		HashSet<String> paths = new HashSet<String>(pathsOfOldCommit);
		paths.addAll(pathsOfNewCommit);

		ArrayList<String> pathList = new ArrayList<String>(paths);

		for (String filePath : pathList) {
			// the diff works on TreeIterators, we prepare two for the two branches
			AbstractTreeIterator oldTreeParser = this.prepareTreeParser(repository, oldCommitHash);
			AbstractTreeIterator newTreeParser = this.prepareTreeParser(repository, newCommitHash);

			// then the porcelain diff-command returns a list of diff entries
			List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
					.setPathFilter(PathFilter.create(filePath)).
					// to filter on Suffix use the following instead
					// setPathFilter(PathSuffixFilter.create(".java")).
					call();
			for (DiffEntry entry : diff) {
				System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
				try (DiffFormatter formatter = new DiffFormatter(System.out)) {
					formatter.setRepository(repository);
					formatter.format(entry);
				}
			}
		}
	}

	private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		// from the commit we can build the tree which allows us to construct the
		// TreeParser
		// noinspection Duplicates
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}

	public String[] makeArrayStringFromArrayListOfString(ArrayList<String> array1) {
		String[] array2 = new String[array1.size()];

		int i = 0;
		for (String content : array1) {
			array2[i++] = content;
		}
		return array2;

	}

	public void makePatchsFromCommitsByBranchType(Patch p, String patchesDirectory) throws IOException, GitAPIException {
		
		File folder = new File(patchesDirectory);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		Set<Entry<String, ArrayList<String>>> set = this.commitHashs.entrySet();
		Iterator<Entry<String, ArrayList<String>>> it = set.iterator();
		List<DiffEntry> diffs = null;
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
			String[] hashList = p.makeArrayStringFromArrayListOfString(e.getValue());
			for (int i = 0; i < hashList.length - 1; i++) {
				diffs = p.pullDiffs(hashList[i + 1], hashList[i]);
			}
		}
		
		
		ICsvMapWriter mapWriter = null;
		mapWriter = new CsvMapWriter(new FileWriter(patchesDirectory+"/reulst.csv"), // 여기 수정해야함.
				CsvPreference.STANDARD_PREFERENCE);

		final CellProcessor[] processors = this.getProcessors();
		// write the header
		mapWriter.writeHeader(header);

		// write the customer maps
		for (Map<String, Object> commit : commits) {
			mapWriter.write(commit, header, processors);
		}
		if (mapWriter != null) {
			mapWriter.close();
		}

	}

	public List<DiffEntry> pullDiffs(String oldCommitHash, String newCommitHash)
			throws IOException, GitAPIException {

		RevWalk walk = new RevWalk(repository);
		ObjectId id = repository.resolve(newCommitHash);
		RevCommit commit = walk.parseCommit(id);
		
		/* go to */
//		this.addContentsToCommitList(String.valueOf(count), commit.getShortMessage(), newCommitHash, filename,
//				commit.getCommitTime(), commit.getAuthorIdent().getName(), commit.getFullMessage());

		ArrayList<String> pathsOfOldCommit = this.getPathList(oldCommitHash);
		ArrayList<String> pathsOfNewCommit = this.getPathList(newCommitHash);

		HashSet<String> paths = new HashSet<String>(pathsOfOldCommit);
		paths.addAll(pathsOfNewCommit);

		ArrayList<String> pathList = new ArrayList<String>(paths);
		List<DiffEntry> diffs = null;
		
		for (String filePath : pathList) {
			// the diff works on TreeIterators, we prepare two for the two branches
			AbstractTreeIterator oldTreeParser = this.prepareTreeParser(repository, oldCommitHash);
			AbstractTreeIterator newTreeParser = this.prepareTreeParser(repository, newCommitHash);

			// then the porcelain diff-command returns a list of diff entries
			List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
					.setPathFilter(PathFilter.create(filePath)).
					// to filter on Suffix use the following instead
					// setPathFilter(PathSuffixFilter.create(".java")).
					call();
			diffs.addAll(diff);
//			try (DiffFormatter formatter = new DiffFormatter(fw)) {
//				formatter.setRepository(repository);
//				formatter.format(diff);
//			}
		}
		return diffs;
//		fw.flush();
//		fw.close();
	}
	
	private void addContentsToCommitList(String project, String shortMessage, String commitHash, int date, String Author, String diff) {

		Map<String, Object> temp = new HashMap<String, Object>();
		
		temp.put(header[0], project);
		temp.put(header[1], shortMessage);
		temp.put(header[2], commitHash);
		temp.put(header[3], date);
		temp.put(header[4], Author);
		temp.put(header[5], diff);

		commits.add(temp);

	}
	
	/* 여기도 수정해야함. */
//	private void addContentsToCommitList(String num, String shortMessage, String newCommitHash, String filename,
//			int commitTime, String name, String fullMessage) {
//
//		Map<String, Object> temp = new HashMap<String, Object>();
//
//		temp.put(header[0], num);
//		temp.put(header[1], shortMessage);
//		temp.put(header[2], newCommitHash);
//		temp.put(header[3], filename);
//		temp.put(header[4], String.valueOf(commitTime));
//		temp.put(header[5], name);
//		temp.put(header[6], fullMessage);
//
//		commits.add(temp);
//
//	}

	/* 여기도 수정해야함. */
	private CellProcessor[] getProcessors() {

		
		CellProcessor[] processors = new CellProcessor[] { 
				new Optional(), new Optional(),
				new Optional(), new Optional(),
				new Optional(), new Optional(),};
		
//		CellProcessor[] processors = new CellProcessor[] { new UniqueHashCode(), // customerNo (must be unique)
//				new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), };  

		return processors;
	}
}
