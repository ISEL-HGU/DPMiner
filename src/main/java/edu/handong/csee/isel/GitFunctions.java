package edu.handong.csee.isel;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;


public class GitFunctions {

	private String projectName;
	private String outPath;
	private String gitRemoteURI;
	private static boolean isAGSZZ = false;
	
	public GitFunctions(String projectName, String outPath, String gitURL, boolean isAGSZZ) {
		this.projectName = projectName;
		this.outPath = outPath;
		this.gitRemoteURI = gitURL + ".git";
		this.isAGSZZ = isAGSZZ;
	}
	
	public List<RevCommit> getAllCommitList() throws InvalidRemoteException, TransportException, GitAPIException, IOException{
		File gitDirectory = null;
		if (isCloned() && isValidRepository()) {
			gitDirectory = getGitDirectory();
		} else if (isCloned() && (!isValidRepository())) {
			File directory = getGitDirectory();
			directory.delete();
			gitDirectory = GitClone();
		} else {
			gitDirectory = GitClone();
		}
		return getCommitListFrom(gitDirectory);
		
	}
	
	
	private boolean isValidRepository() {
		File directory = getGitDirectory();
		try {
			Git git = Git.open(directory);  //여기가 쓰이는데 왜안쓰인다고 뜨는지 모르겠다.
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private static List<RevCommit> getCommitListFrom(File gitDir) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(gitDir);
		Iterable<RevCommit> walk;
		if(isAGSZZ) {
			walk = git.log().call();
		} else {
			walk = git.log().all().call();
		}
		List<RevCommit> commitList = IterableUtils.toList(walk);

		return commitList;
	}

	public String getReferencePath() {
		return outPath + File.separator + "reference";
	}

	public File getGitDirectory() {
		String referencePath = getReferencePath();
		File clonedDirectory = new File(
				referencePath + File.separator + "repositories" + File.separator + projectName);
		return clonedDirectory;
	}

	public File GitClone() throws InvalidRemoteException, TransportException, GitAPIException {
		File clonedDirectory = getGitDirectory();
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(gitRemoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		System.out.println("done");
		return git.getRepository().getDirectory();
	}

	private boolean isCloned() {
		File clonedDirectory = getGitDirectory();
		return clonedDirectory.exists();
	}
	
}
