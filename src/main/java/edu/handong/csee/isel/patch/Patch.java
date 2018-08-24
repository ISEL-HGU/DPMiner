package edu.handong.csee.isel.patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
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

public class Patch {

	final String[] header = new String[] { "Project", "ShortMessage", "CommitHash", "Date", "Author", "Diff" };
	ArrayList<Map<String, Object>> commits = new ArrayList<Map<String, Object>>();
	String directoryPath;
	File directory;
	Git git;
	Repository repository;
	HashMap<String, ArrayList<String>> commitHashs; // <branch, commitHash>
	HashMap<String, ArrayList<String>> allPathList = new HashMap<String, ArrayList<String>>(); // <commitHash, pathList>
	ArrayList<String> branchList = new ArrayList<String>();

	public Git getGit() {
		return git;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setCommitHashs(String branch) throws RevisionSyntaxException, NoHeadException, MissingObjectException,
			IncorrectObjectTypeException, AmbiguousObjectException, GitAPIException, IOException {
		if (branchList.isEmpty() || !branchList.contains(branch)) {
//			System.out.println("branch is not!");
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

	public Patch(String directoryPath) throws IOException, GitAPIException {
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.directoryPath = directoryPath;
		this.directory = new File(directoryPath);
		this.git = Git.open(new File(directoryPath));
		this.repository = this.git.getRepository();
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.setBranchList();
	}

	public Patch(Git git, Repository repository) throws IOException, GitAPIException {
		this.commitHashs = new HashMap<String, ArrayList<String>>();
		this.directoryPath = repository.getDirectory().toString();
		this.directory = new File(directoryPath);
		this.git = git;
		this.repository = repository;
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

	private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
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

	public ArrayList<TwoCommit> analyze() throws IOException, GitAPIException {

		Set<Entry<String, ArrayList<String>>> set = this.commitHashs.entrySet();
		Iterator<Entry<String, ArrayList<String>>> it = set.iterator();
		int count = 0;
		ArrayList<TwoCommit> sumCommitHash = new ArrayList<TwoCommit>();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
			ArrayList<String> hashList = e.getValue();
			for (int i = 0; i < hashList.size() - 1; i++) {
				sumCommitHash.add(new TwoCommit(hashList.get(i + 1), hashList.get(i)));
			}
		}
		HashSet<TwoCommit> mySet = new HashSet<TwoCommit>(sumCommitHash);
		ArrayList<TwoCommit> refinedSumCommitHash = new ArrayList<TwoCommit>(mySet);
		return refinedSumCommitHash;

	}

	public ArrayList<String> getStringFromFiles(ArrayList<File> files) throws FileNotFoundException {

		ArrayList<String> stringList = new ArrayList<String>();
		for (File file : files) {
			String temp = this.getStringFromFile(file);
			if (!temp.trim().equals(""))
				stringList.add(temp);
		}
		return stringList;
	}

	public String getStringFromFile(File file) throws FileNotFoundException {

		String newString = "";

		FileReader reader = new FileReader(file);
		BufferedReader br = new BufferedReader(reader);
		String line = "";
		try {
			while ((line = br.readLine()) != null) {
				newString += (line + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newString;
	}

	public HashMap<File, String> pullDiffs(String oldCommitHash, String newCommitHash)
			throws IOException, GitAPIException, InterruptedException {

		File dir = new File("temp");
		if (!dir.exists()) {
			if (dir.mkdirs()) {
				// System.out.println("successfull to make temp!");
			} else {
				// System.out.println("fail to make temp..");
			}
		}

		RevWalk walk = new RevWalk(repository);
		ObjectId id = repository.resolve(newCommitHash);
		RevCommit commit = walk.parseCommit(id);

		ArrayList<String> pathesOfOldCommit = this.getPathList(oldCommitHash);
		ArrayList<String> pathesOfNewCommit = this.getPathList(newCommitHash);

		int i;
		int recurN = pathesOfOldCommit.size();

		for (i = 0; i < recurN; i++) {
			try {

				String oldPath = pathesOfOldCommit.get(i);

				if (oldPath.equals("/dev/null")) {
					pathesOfOldCommit.remove(i);
					recurN--;
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		recurN = pathesOfNewCommit.size();

		for (i = 0; i < recurN; i++) {
			try {
				String newPath = pathesOfNewCommit.get(i);

				if (newPath.contains("Test") || !newPath.endsWith(".java")) {
					pathesOfNewCommit.remove(i);
					recurN--;
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(100000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		HashSet<String> paths = new HashSet<String>(pathesOfOldCommit);
		paths.addAll(pathesOfNewCommit);

		ArrayList<String> pathList = new ArrayList<String>(paths);

		HashMap<File, String> diffFilesAndPath = new HashMap<File, String>();

		i = 1;
		for (String filePath : pathList) {
			AbstractTreeIterator oldTreeParser = this.prepareTreeParser(repository, oldCommitHash);
			AbstractTreeIterator newTreeParser = this.prepareTreeParser(repository, newCommitHash);

			List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
					.setPathFilter(PathFilter.create(filePath)).call();

			File newFile = new File("temp" + File.separator + commit.getId().name() + "-" + String.valueOf(i) + ".txt");
			OutputStream fw = new FileOutputStream(newFile);

			try (DiffFormatter formatter = new DiffFormatter(fw)) {
				formatter.setRepository(repository);
				formatter.format(diff);
			}
			fw.flush();
			fw.close();
			i++;
			diffFilesAndPath.put(newFile, filePath);
		}
		return diffFilesAndPath;
	}
}