
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

import edu.handong.csee.isel.bfc.BFCCollector;
import edu.handong.csee.isel.bfc.collector.BFCGitHubCollector;
import edu.handong.csee.isel.bfc.collector.BFCJiraCollector;
import edu.handong.csee.isel.bfc.collector.BFCKeywordCollector;
import edu.handong.csee.isel.bfc.collector.jira.InvalidDomainException;
import edu.handong.csee.isel.bfc.collector.jira.InvalidProjectKeyException;
import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.bic.collector.CBICCollector;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.processor.CSVMaker;
import edu.handong.csee.isel.data.processor.input.InputConverter;
import edu.handong.csee.isel.data.processor.input.command.Task;
import edu.handong.csee.isel.data.processor.input.converter.CLIConverter;
import edu.handong.csee.isel.metric.MetricCollector;
import edu.handong.csee.isel.metric.collector.CMetricCollector;
import edu.handong.csee.isel.metric.collector.DeveloperHistory;
import edu.handong.csee.isel.metric.metadata.Utils;
import edu.handong.csee.isel.patch.PatchCollector;
import edu.handong.csee.isel.patch.collector.CPatchCollector;
import picocli.CommandLine;

public class Main {

	public static void main(String[] args)
			throws NoHeadException, IOException, GitAPIException, InvalidProjectKeyException, InvalidDomainException {

		// 1. Input
		Task task = new Task();
		CommandLine cmd = new CommandLine(task);
		int exitCode = cmd.execute(args);
		if(exitCode != 0)
			System.exit(exitCode);	

		// 2. get all commits from GIT directory
		List<RevCommit> commitList;
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
		commitList = getCommitListFrom(gitDirectory);

		// 3. collect Bug-Fix-Commit
		List<String> bfcList = null;
		List<String> bicList = null;//메트릭스에서 쓰임.
		MetricCollector metricCollector = null;
		BFCCollector bfcCollector = null;
		List<CSVInfo> csvInfoLst = null;


		switch (Input.taskType) {
		case PATCH:
			bfcList=makeBFCCollector(bfcList,commitList,bfcCollector);	
			
			PatchCollector patchCollector = new CPatchCollector();
			patchCollector.setBFC(bfcList);
			csvInfoLst = patchCollector.collectFrom(commitList);
			
			printCSV(csvInfoLst);

			break;

		case BIC:
			bfcList=makeBFCCollector(bfcList,commitList,bfcCollector);
			
			BICCollector bicCollector = new CBICCollector();
//			bicCollector = new SZZRunner(getGitDirectory(input).getAbsolutePath());
			bicCollector.setBFC(bfcList);
			csvInfoLst = bicCollector.collectFrom(commitList);
			printCSV(csvInfoLst);//이게 최종 BIC프린트 해주는 메소드-> 손델것은 없다. 알아서 하는 메소드.
			break;

		case METRIC:
			//BIC 파일 읽기
			bicList= readBICcsv();			
			metricCollector = new CMetricCollector(false);
			metricCollector.setBIC(bicList);
			File arff = metricCollector.collectFrom(commitList);
			System.out.println("Metric was saved in " + arff.getAbsolutePath());

			break;
			
		case DEVELOPERMETRIC:
			//BIC 파일 읽기
			bicList=readBICcsv();
			
			DeveloperHistory developerHistory = new DeveloperHistory();
			String midDate = developerHistory.findDeveloperDate();
			System.out.println("MidDate : "+midDate);
			
			metricCollector = new CMetricCollector(true);
			metricCollector.setMidDate(midDate);
			metricCollector.setBIC(bicList);
			metricCollector.collectFrom(commitList);
			
			break;
		}
	}

	
	public static List<String> readBICcsv(){
		File BIC = new File(Input.BICpath);
		if (!BIC.isFile()) {
			System.out.println("There is no BIC file");
			System.exit(1);
		}
		 List<String> bicList = Utils.readBICCsvFile(Input.BICpath);
		
		return bicList;
		
	}
	
	public static void printCSV(List<CSVInfo> csvInfoLst)  throws IOException {

		if (csvInfoLst.size() < 1) {
//			System.out.println("why is it not working?");
			return;
		}
//		System.out.println("Really?");
		CSVMaker printer = new CSVMaker();
		printer.setDataType(csvInfoLst);
		printer.setPath();
		printer.print(csvInfoLst);
		
	}
	
	public static List<String> makeBFCCollector (List<String> bfcList, List<RevCommit> commitList, BFCCollector bfcCollector)
			throws IOException,InvalidProjectKeyException, InvalidDomainException{
		
		switch (Input.mode) {  //CLIConverter에서 각각 옵션 모드를 설정해 주었다. 
		case JIRA:
//			System.out.println("Main Jira part!");
			bfcCollector = new BFCJiraCollector();
			bfcCollector.setJiraURL(Input.jiraURL);
			bfcCollector.setJiraProjectKey(Input.jiraProjectKey);
			bfcCollector.setOutPath(Input.outPath);
			bfcList = bfcCollector.collectFrom(commitList);
			break;
			
		case KEYWORD:
//			System.out.println("Main KeyWord part!");
			bfcCollector = new BFCKeywordCollector();
			bfcList = bfcCollector.collectFrom(commitList);
			break;
			
		case GITHUB:
//			System.out.println("Main GitHub part!");
			bfcCollector = new BFCGitHubCollector();
			bfcCollector.setGitHubURL(Input.gitURL);
			bfcCollector.setGitHubLabel(Input.label);
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
		Iterable<RevCommit> walk = git.log().call();
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
