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

/**
 * This is a class that contains several methods necessary to collect commits in Github.
 */
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

	/**
	 * After checking whether to clone the repository, if it is not cloned, clone the repository.
	 * Then collect all commtis in that repository.
	 * @return commitList
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 * @throws IOException
	 */
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

	private boolean isValidRepository() { //진짜 깃허브에 존재하고, 이게 퍼블릭인지 아닌지를 체크하는 것.
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

	/**
	 * This method returns the path of the reference folder.
	 * @return
	 */
	public String getReferencePath() {
		return outPath + File.separator + "reference";
	}

	/**
	 * This method returns the location where the file to be cloned will be saved.
	 * @return clonedDirectory
	 */
	public File getGitDirectory() {
		String referencePath = getReferencePath();
		File clonedDirectory = new File(
				referencePath + File.separator + "repositories" + File.separator + projectName);
		return clonedDirectory;
	}

	/**
	 * This method is used to clone the repository from Github.
	 * @return
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	public File GitClone() throws InvalidRemoteException, TransportException, GitAPIException {
		File clonedDirectory = getGitDirectory();
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(gitRemoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		System.out.println("done");
		return git.getRepository().getDirectory();
	}

	private boolean isCloned() { //이미 클론이 받아져있는지 아닌지를 체크하는 메소드
		File clonedDirectory = getGitDirectory();
		return clonedDirectory.exists();
	}
	
}
