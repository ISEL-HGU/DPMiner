package edu.handong.csee.isel.patch;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import edu.handong.csee.isel.csvProcessors.CSVgetter;
import edu.handong.csee.isel.csvProcessors.CSVsetter;

public class LocalGitRepositoryPatchCollector {
	String gitRepositoryPath = null;
	String resultDirectory = null;
	String reference = null;
	int conditionMax;
	int conditionMin;
	boolean isThread;

	public LocalGitRepositoryPatchCollector(String gitRepositoryPath, String resultDirectory, String reference,
			int conditionMax, int conditionMin, boolean isThread) {
		this.gitRepositoryPath = gitRepositoryPath;
		this.resultDirectory = resultDirectory;
		this.reference = reference;
		this.conditionMax = conditionMax;
		this.conditionMin = conditionMin;
		this.isThread = isThread;
	}

	public void run() {
		try {

			ExecutorService executor = null;

			if (isThread) {
				int numOfCoresInMyCPU = Runtime.getRuntime().availableProcessors();
				executor = Executors.newFixedThreadPool(numOfCoresInMyCPU);
			} else {
				executor = Executors.newFixedThreadPool(1);
			}

			// (1) put csvFile and return issueHashes type of ArrayList<String>
			ArrayList<String> issueHashList = null;
			Pattern pattern = null;
			Pattern issuePattern = null;
			StringBuffer sbIssues = new StringBuffer();
			if (reference != null) {
				CSVgetter getter = new CSVgetter(reference);
				issueHashList = getter.getColumn(0);
				for(String issue : issueHashList) {
//					System.out.println(issue);
					sbIssues.append(issue+"|");
				}
				sbIssues.deleteCharAt(sbIssues.length()-1);
//				System.out.println(sbIssues);
				issuePattern = Pattern.compile(sbIssues.toString());
				
			} else {
				pattern = Pattern.compile("fix|bug", Pattern.CASE_INSENSITIVE);
			}

			// (2)
			Patch p = new Patch(gitRepositoryPath);
			ArrayList<TwoCommit> commitHashes = p.analyze();

			// (3) apply Thread pool
			ArrayList<MyExecutor> myExecutors = new ArrayList<MyExecutor>();
			for (TwoCommit commitHash : commitHashes) {
				Runnable worker = new MyExecutor(commitHash.getOldCommitHash(), commitHash.getNewCommitHash(),
						issuePattern, p.getGit(), p.getRepository(), conditionMax, conditionMin, pattern);
				executor.execute(worker);
				myExecutors.add((MyExecutor) worker);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {

			}

			ArrayList<CommitStatus> commitIncludedInIssueHashList = new ArrayList<CommitStatus>();
			ArrayList<CommitStatus> temp = null;
			for (MyExecutor my : myExecutors) {
				if ((temp = my.getCommitStatusList()) != null)
					commitIncludedInIssueHashList.addAll(temp);
			}

			// (4)
			String newFileName = resultDirectory;
			if (!newFileName.endsWith("/")) {
				newFileName += ("/");
			}
			newFileName += (new File(gitRepositoryPath).getName() + ".csv");
			File newFile = new File(newFileName);
			CSVsetter setter = new CSVsetter(newFile);
			String[] headers = { "Project", "ShortMessage", "Commit Hash", "Date", "Author", "Path", "Patch" };
			setter.makeCSVfromCommits(commitIncludedInIssueHashList, headers);

			System.out.println("saved patches in \"" + resultDirectory + "\"");

		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}
