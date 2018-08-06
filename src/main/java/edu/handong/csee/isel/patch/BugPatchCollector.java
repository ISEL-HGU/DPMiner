package edu.handong.csee.isel.patch;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jgit.api.errors.GitAPIException;

import edu.handong.csee.isel.csvProcessors.CSVgetter;
import edu.handong.csee.isel.csvProcessors.CSVsetter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class BugPatchCollector {
	String gitRepositoryPath;
	String resultDirectory;
	String csvFile;
	int conditionMax;
	boolean verbose;
	boolean help;

	public static void main(String[] args) {
		BugPatchCollector bc = new BugPatchCollector();
		bc.run(args);
	}

	public void run(String[] args) {
		Options options = createOptions();

		if (parseOptions(options, args)) {
			if (help) {
				printHelp(options);
				return;
			}

			/* Start Main */

			try {

				int numOfCoresInMyCPU = Runtime.getRuntime().availableProcessors();
				System.out.println("Thread n: " + numOfCoresInMyCPU);
				ExecutorService executor = Executors.newFixedThreadPool(numOfCoresInMyCPU);
//				ExecutorService executor = Executors.newFixedThreadPool(1);

				Thread.sleep(3000);

				// csvFile 을 넣어서 ArrayList<String> issueHashes 로 받는다.
				// (1)
				CSVgetter getter = new CSVgetter(csvFile);
				ArrayList<String> issueHashList = getter.getColumn(1);
				// for(String issue : issueHashList)
				// System.out.println(issue);

				// (2)
				Patch p = new Patch(gitRepositoryPath);
				ArrayList<TwoCommit> commitHashes = p.analyze();
				
				
				
				// (3) apply Thread pool

				/*
				 * MyExecutor 클래스를 만들고 extends Thread 한 개의 commit Hash를 받아드려. return은
				 * CommitStatus.
				 */
				
				//Thread.sleep(10000);
				
				ArrayList<MyExecutor> myExecutors = new ArrayList<MyExecutor>();
//				int count = 0;
//				int total = commitHashes.size();
				for (TwoCommit commitHash : commitHashes) {
//					MyExecutor myTemp = new MyExecutor(gitRepositoryPath, commitHash.getOldCommitHash(),
//							commitHash.getNewCommitHash(), issueHashList, p.getGit(), p.getRepository());
					Runnable worker = new MyExecutor(commitHash.getOldCommitHash(),
							commitHash.getNewCommitHash(), issueHashList, p.getGit(), p.getRepository(),conditionMax);
					executor.execute(worker);
					myExecutors.add((MyExecutor) worker);
				}
				executor.shutdown();
				while (!executor.isTerminated()) {
				
				}
				
				System.out.println("100%!!");
				Thread.sleep(3000);
				
				ArrayList<CommitStatus> commitIncludedInIssueHashList = new ArrayList<CommitStatus>();
				ArrayList<CommitStatus> temp = null;
				for (MyExecutor my : myExecutors) {
					if ((temp = my.getCommitStatusList()) != null)
						commitIncludedInIssueHashList.addAll(temp);
				}

				// (4)
				File newFile = new File(resultDirectory + "/result.csv");
				CSVsetter setter = new CSVsetter(newFile);
				setter.makeCSVfromCommits(commitIncludedInIssueHashList);

				System.out.println("saved patches in \"" + resultDirectory + "\"");

			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			/* until here */

			if (verbose) {
				System.out.println("******The program help making Patch from all commit");
				System.out.println("******input git-repository-path ");
				System.out.println("******input result-File-Name");
				System.out.println("****************************");
				System.out.println("******SB Made it******");
				System.out.println("****************************");
			}
		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			csvFile = cmd.getOptionValue("c");
			gitRepositoryPath = cmd.getOptionValue("g");
			resultDirectory = cmd.getOptionValue("r");
			conditionMax = Integer.parseInt(cmd.getOptionValue("M"));
			
			verbose = cmd.hasOption("v");
			help = cmd.hasOption("h");
			
		} catch (Exception e) {
			printHelp(options);
			return false;
		}

		return true;
	}

	// Definition Stage
	private Options createOptions() {
		Options options = new Options();

		// add options by using OptionBuilder
		options.addOption(Option.builder("c").longOpt("csv").desc("Set a path of CSV reference relative to commits")
				.hasArg().argName("CSV File").required().build());

		options.addOption(Option.builder("g").longOpt("gitRepositoryPath").desc("Set a path of a git-repository")
				.hasArg().argName("Git-repository path name").required().build());

		options.addOption(Option.builder("r").longOpt("resultDirectory")
				.desc("Set a directory to have result files(all patch files and a summary file).").hasArg()
				.argName("Path name to construct result files").required().build());
		
		options.addOption(Option.builder("M").longOpt("Maxline")
				.desc("Set a Max lines of each result patch. Only count '+++' and '---' lines.").hasArg()
				.argName("Max lines of patch").build());


		// add options by using OptionBuilder
		options.addOption(Option.builder("v").longOpt("verbose").desc("Display detailed messages!")
				// .hasArg() // this option is intended not to have an option value but just an
				// option
				.argName("verbose option")
				// .required() // this is an optional option. So disabled required().
				.build());

		// add options by using OptionBuilder
		options.addOption(Option.builder("h").longOpt("help").desc("Help").build());

		return options;
	}

	private void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		String header = "Collecting bug-patch program";
		String footer = "\nPlease report issues at https://github.com/HGUISEL/BugPatchCollector/issues";
		formatter.printHelp("BugPatchCollector", header, options, footer, true);
	}

	public void printCommitHashList(Patch p, HashMap<String, ArrayList<String>> commitHashList) throws IOException {
		Set<Entry<String, ArrayList<String>>> set = commitHashList.entrySet();
		Iterator<Entry<String, ArrayList<String>>> it = set.iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
			System.out.println("branch: " + e.getKey());
			ArrayList<String> pathList = new ArrayList<String>();
			for (String commitHash : e.getValue()) {
				System.out.println("	commitHash: " + commitHash);
				pathList = p.getPathList(commitHash);
				if (pathList.isEmpty())
					continue;
			}
		}
	}

	public void ShowdiffFromTwoCommits(Patch p, HashMap<String, ArrayList<String>> commitHashList)
			throws IOException, GitAPIException {
		Set<Entry<String, ArrayList<String>>> set = commitHashList.entrySet();
		Iterator<Entry<String, ArrayList<String>>> it = set.iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
			System.out.print("@@@@@@@@@@@@@@@@@@@@@@@@ Branch: " + e.getKey());
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@\n");
			String[] hashList = p.makeArrayStringFromArrayListOfString(e.getValue());
			for (int i = 0; i < hashList.length - 1; i++) {
				p.showFileDiff(hashList[i], hashList[i + 1]);
			}
		}
	}
}
