
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
import edu.handong.csee.isel.bic.collector.szzBICCollector;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.processor.CSVMaker;
import edu.handong.csee.isel.data.processor.input.InputConverter;
import edu.handong.csee.isel.data.processor.input.converter.CLIConverter;
import edu.handong.csee.isel.metric.MetricCollector;
import edu.handong.csee.isel.metric.collector.CMetricCollector;
import edu.handong.csee.isel.metric.collector.DeveloperHistory;
import edu.handong.csee.isel.metric.metadata.Utils;
import edu.handong.csee.isel.patch.PatchCollector;
import edu.handong.csee.isel.patch.collector.CPatchCollector;

//import edu.handong.csee.isel.bic.collector.szzBICCollector;

public class Main {

	public static void main(String[] args)
			throws NoHeadException, IOException, GitAPIException, InvalidProjectKeyException, InvalidDomainException {

		// 1. Input
		InputConverter inputConverter = new CLIConverter();
		Input input = inputConverter.getInputFrom(args);

		// 2. get all commits from GIT directory
		List<RevCommit> commitList;
		File gitDirectory = null;
		if (isCloned(input) && isValidRepository(input)) {
			gitDirectory = getGitDirectory(input);
		} else if (isCloned(input) && (!isValidRepository(input))) {
			File directory = getGitDirectory(input);
			directory.delete();
			gitDirectory = GitClone(input);
		} else {
			gitDirectory = GitClone(input);
		}
		commitList = getCommitListFrom(gitDirectory);

		// 3. collect Bug-Fix-Commit
		List<String> bfcList = null;
		List<String> bicList = null;//메트릭스에서 쓰임.
		MetricCollector metricCollector = null;
		BFCCollector bfcCollector = null;
		List<CSVInfo> csvInfoLst = null;


		switch (input.taskType) {
		case Patch:
			bfcList=Making_bfcCollector(input,bfcList,commitList,bfcCollector);
		
			PatchCollector patchCollector = new CPatchCollector(input);
			patchCollector.setBFC(bfcList);
			csvInfoLst = patchCollector.collectFrom(commitList);
			
			Print_CSV(input, csvInfoLst);

			break;

		case BIC:
			//아 여기서 만들어 준넨엥에엥 
			bfcList=Making_bfcCollector(input,bfcList,commitList,bfcCollector);
			
			BICCollector bicCollector = new szzBICCollector(input);
//			bicCollector = new SZZRunner(getGitDirectory(input).getAbsolutePath());
			bicCollector.setBFC(bfcList);
			csvInfoLst = bicCollector.collectFrom(commitList);
//			Print_CSV(input, csvInfoLst);//이게 최종 BIC프린트 해주는 메소드-> 손델것은 없다. 알아서 하는 메소드.

//			위에걸 해줘야 apacheJUDDIIssueKeys.csv가 나오기 때문에 위에를 일단 실행시킨다. 
//			BICCollector bicCollector = new szzBICCollector(input);
//		    파일 저장해주는 것까지 szzBICCollector에서 처리해주기. 
//			Phase 3: Utils.storeOutputFile(GIT_URL, BILines); 
			
			break;

		case Metric:
			//BIC 파일 읽기
			bicList= Read_BICcsv(input);			
			
			metricCollector = new CMetricCollector(input,false);
			metricCollector.setBIC(bicList);
			File arff = metricCollector.collectFrom(commitList);
			System.out.println("Metric was saved in " + arff.getAbsolutePath());

			break;
			
		case Develop_Metirc:
			//BIC 파일 읽기
			bicList=Read_BICcsv(input);
			
			DeveloperHistory developerHistory = new DeveloperHistory(input);
			String midDate = developerHistory.findDeveloperDate();
			System.out.println("MidDate : "+midDate);
			
			metricCollector = new CMetricCollector(input,true);
			metricCollector.setMidDate(midDate);
			metricCollector.setBIC(bicList);
			metricCollector.collectFrom(commitList);
			
			break;
		} 
	}
	
	
	
	public static List<String> Read_BICcsv(Input input){
		File BIC = new File(input.BICpath);
		if (!BIC.isFile()) {
			System.out.println("There is no BIC file");
			System.exit(1);
		}
		 List<String> bicList = Utils.readBICCsvFile(input.BICpath);
		
		return bicList;
		
	}
	
	public static void Print_CSV( Input input, List<CSVInfo> csvInfoLst)  throws IOException {

		if (csvInfoLst.size() < 1) {
			System.out.println("why is it not working?");
			return;
		}
		System.out.println("Really?");
		CSVMaker printer = new CSVMaker();
		printer.setDataType(csvInfoLst);
		printer.setPath(input);
		printer.print(csvInfoLst);
		
	}
	
	public static List<String> Making_bfcCollector (Input input, List<String> bfcList, List<RevCommit> commitList, BFCCollector bfcCollector)
			throws IOException,InvalidProjectKeyException, InvalidDomainException{
		
		switch (input.mode) {  //CLIConverter에서 각각 옵션 모드를 설정해 주었다. 
		case Jira:
			bfcCollector = new BFCJiraCollector();
			bfcCollector.setJiraURL(input.jiraURL);
			bfcCollector.setJiraProjectKey(input.jiraProjectKey);
			bfcCollector.setOutPath(input.outPath);
			bfcList = bfcCollector.collectFrom(commitList);
			break;
			
		case KeyWord:
			bfcCollector = new BFCKeywordCollector();
			bfcList = bfcCollector.collectFrom(commitList);
			break;
			
		case GitHub:
			bfcCollector = new BFCGitHubCollector();
			bfcCollector.setGitHubURL(input.gitURL);
			bfcCollector.setGitHubLabel(input.label);
			bfcList = bfcCollector.collectFrom(commitList);
			break;
		
		}
		
		return bfcList;
	}

	

	private static boolean isValidRepository(Input input) {
		File directory = getGitDirectory(input);
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

	public static String getReferencePath(Input input) {
		return input.outPath + File.separator + "reference";
	}

	public static File getGitDirectory(Input input) {
		String referencePath = getReferencePath(input);
		File clonedDirectory = new File(
				referencePath + File.separator + "repositories" + File.separator + input.projectName);
		return clonedDirectory;
	}

	private static File GitClone(Input input) throws InvalidRemoteException, TransportException, GitAPIException {
		String remoteURI = input.gitRemoteURI;
		String projectName = input.projectName;
		File clonedDirectory = getGitDirectory(input);
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(remoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		System.out.println("done");
		return git.getRepository().getDirectory();
	}

	private static boolean isCloned(Input input) {
		File clonedDirectory = getGitDirectory(input);
		return clonedDirectory.exists();
	}
}
