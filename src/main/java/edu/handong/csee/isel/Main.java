
package edu.handong.csee.isel;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.BFCCollectable;
import edu.handong.csee.isel.bfc.collector.BFCGitHubCollector;
import edu.handong.csee.isel.bfc.collector.BFCJiraCollector;
import edu.handong.csee.isel.bfc.collector.BFCKeywordCollector;
import edu.handong.csee.isel.bfc.collector.jira.InvalidDomainException;
import edu.handong.csee.isel.bfc.collector.jira.InvalidProjectKeyException;
import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.bic.collector.AGSZZBICCollector;
import edu.handong.csee.isel.bic.collector.CBICCollector;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.Input.Mode;
import edu.handong.csee.isel.data.csv.CSVMaker;
import edu.handong.csee.isel.data.processor.input.command.Task;
import edu.handong.csee.isel.metric.MetricCollector;
import edu.handong.csee.isel.metric.collector.CMetricCollector;
import edu.handong.csee.isel.metric.metadata.Utils;
import edu.handong.csee.isel.patch.PatchCollector;
import edu.handong.csee.isel.patch.collector.CPatchCollector;
import edu.handong.csee.isel.repo.collector.RepoCollector;
import edu.handong.csee.isel.repo.collector.RepoCommitCollector;
import picocli.CommandLine;


public class Main {
	public static void main(String[] args)
			throws NoHeadException, IOException, GitAPIException, InvalidProjectKeyException, InvalidDomainException, InterruptedException {

		// 1. Input
		Task task = new Task();
		CommandLine cmd = new CommandLine(task);
		int exitCode = cmd.execute(args);
		if(exitCode != 0)
			System.exit(exitCode);	

		// 3. collect Bug-Fix-Commit
		List<RevCommit> commitList = null;
		List<String> bfcList = null;
		List<String> bicList = null;//메트릭스에서 쓰임.
		MetricCollector metricCollector = null;
		BFCCollectable bfcCollector = null;
		List<CSVInfo> csvInfoLst = null;
		HashSet<String> repoResult= null;
		HashSet<String> repoCommitResult= null;

		switch (Input.taskType) {
		case FINDREPO:
			RepoCollector searchRepo = new RepoCollector(Input.authToken, Input.findRepoOpt); //git token 받아오기 아아 얘가 리파지토리 찾아오는 
//			searchRepo.setOption(Input.languageType, Input.forkNum, Input.createDate, Input.recentDate);
			repoResult = searchRepo.collectFrom();
			
			if(Input.commitCountBase != null) {
				RepoCommitCollector searchCommit = new RepoCommitCollector(repoResult, Input.authToken ,Input.commitCountBase);
				repoCommitResult = searchCommit.collectFrom();
			}
			
			break;
		case PATCH:
			GitFunctions test = new GitFunctions(Input.projectName, Input.outPath, Input.gitURL);
			commitList = test.getAllCommitList();
			bfcList = makeBFCCollector(bfcList,commitList,bfcCollector, Input.mode);	
			
			PatchCollector patchCollector = new CPatchCollector(Input.projectName, Input.outPath, Input.gitURL);
			patchCollector.setBFC(bfcList);
			csvInfoLst = patchCollector.collectFrom(commitList);
			
			printCSV(csvInfoLst);

			break;

		case BIC:
			commitList = getAllCommitList();
			bfcList = makeBFCCollector(bfcList,commitList,bfcCollector, Input.mode);
			BICCollector bicCollector;
			
			switch (Input.szzMode) {
			case BSZZ:
				bicCollector = new CBICCollector(Input.outPath, Input.projectName, Input.gitURL);
				bicCollector.setBFC(bfcList);
				csvInfoLst = bicCollector.collectFrom(commitList);
				printCSV(csvInfoLst);
				break;
				
			case AGSZZ:
				bicCollector = new AGSZZBICCollector(Input.outPath, Input.projectName, Input.gitURL);
				bicCollector.setBFC(bfcList);
				bicCollector.collectFrom(commitList);
				break;
			}
			
			break;

		case METRIC:
			//BIC 파일 읽기
			commitList = getAllCommitList();
			bicList= readBICcsv(Input.BICpath);			
			metricCollector = new CMetricCollector(false, Input.outPath, Input.projectName, Input.gitURL, Input.startDate, Input.endDate);
			metricCollector.setBIC(bicList);
			File arff = metricCollector.collectFrom(commitList);
			System.out.println("Metric was saved in " + arff.getAbsolutePath());

			break;
			
		}
	}
	
	private static List<RevCommit> getAllCommitList() throws InvalidRemoteException, TransportException, GitAPIException, IOException{
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

	
	public static List<String> readBICcsv(String BICpath){
		File BIC = new File(BICpath);
		if (!BIC.isFile()) {
			System.out.println("There is no BIC file");
			System.exit(1);
		}
		 List<String> bicList = Utils.readBICCsvFile(BICpath);
		
		return bicList;
		
	}
	
	
	public static void printCSV(List<CSVInfo> csvInfoLst)  throws IOException {

		if (csvInfoLst.size() < 1) {
			return;
		}
		CSVMaker printer = new CSVMaker();
		printer.setDataType(csvInfoLst);
		printer.setPath(Input.outPath, Input.projectName);
		printer.print(csvInfoLst);
		
	}
	
	public static List<String> makeBFCCollector (List<String> bfcList, List<RevCommit> commitList, BFCCollectable bfcCollector, Mode InputMode)
			throws IOException,InvalidProjectKeyException, InvalidDomainException{
		
		switch (InputMode) {  //CLIConverter에서 각각 옵션 모드를 설정해 주었다. 
		case JIRA:
//			System.out.println("Main Jira part!");
			bfcCollector = new BFCJiraCollector(Input.jiraURL, Input.jiraProjectKey, Input.outPath);
			bfcList = bfcCollector.collectFrom(commitList);
			break;
			
		case KEYWORD:
//			System.out.println("Main KeyWord part!");
			bfcCollector = new BFCKeywordCollector(Input.issueKeyWord);
			bfcList = bfcCollector.collectFrom(commitList);
			break;
			
		case GITHUB:
//			System.out.println("Main GitHub part!");
			bfcCollector = new BFCGitHubCollector(Input.gitURL, Input.label);
			bfcList = bfcCollector.collectFrom(commitList);
			break;
		
		}
		
		return bfcList;
	}

	

	private static boolean isValidRepository() {
		File directory = getGitDirectory();
		try {
			Git git = Git.open(directory);  //여기가 쓰이는데 왜안쓰인다고 뜨는지 모르겠다.
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static List<RevCommit> getCommitListFrom(File gitDir) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(gitDir);
		Iterable<RevCommit> walk = git.log().all().call();
		List<RevCommit> commitList = IterableUtils.toList(walk);

		return commitList;
	}

	public static String getReferencePath() {
		return Input.outPath + File.separator + "reference";
	}

	public static File getGitDirectory() {
		String referencePath = getReferencePath();
		File clonedDirectory = new File(
				referencePath + File.separator + "repositories" + File.separator + Input.projectName);
		return clonedDirectory;
	}

	private static File GitClone() throws InvalidRemoteException, TransportException, GitAPIException {
		String remoteURI = Input.gitRemoteURI;
		String projectName = Input.projectName;
		File clonedDirectory = getGitDirectory();
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(remoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		System.out.println("done");
		return git.getRepository().getDirectory();
	}

	private static boolean isCloned() {
		File clonedDirectory = getGitDirectory();
		return clonedDirectory.exists();
	}
}
