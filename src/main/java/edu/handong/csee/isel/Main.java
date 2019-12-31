
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
import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.bic.collector.CBICCollector;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.processor.CSVMaker;
import edu.handong.csee.isel.data.processor.input.InputConverter;
import edu.handong.csee.isel.data.processor.input.converter.CLIConverter;
import edu.handong.csee.isel.metric.MetricCollector;
import edu.handong.csee.isel.metric.collector.CMetricCollector;
import edu.handong.csee.isel.patch.PatchCollector;
import edu.handong.csee.isel.patch.collector.CPatchCollector;

public class Main {

	public static void main(String[] args) throws NoHeadException, IOException, GitAPIException {

		// 1. Input
		InputConverter inputConverter = new CLIConverter();
		Input input = inputConverter.getInputFrom(args);

		// 2. get all commits from GIT directory
		File gitDirectory = null;
		if (isCloned(input.projectName)) {
			gitDirectory = getGitDirectory(input.projectName);
		} else {
			gitDirectory = GitClone(input.gitRemoteURI);
		}
		List<RevCommit> commitList = getCommitListFrom(gitDirectory);

		// 3. collect Bug-Fix-Commit
		List<String> bfcList = null;
		BFCCollector bfcCollector = null;

		switch (input.referecneType) {
		case JIRA:
			bfcCollector = new BFCJiraCollector();
			bfcCollector.setJiraURL(input.jiraURL);
			bfcCollector.setJiraProjectKey(input.jiraProjectKey);
			bfcList = bfcCollector.collectFrom(commitList);

			break;

		case GITHUB:
			bfcCollector = new BFCGitHubCollector();
			bfcCollector.setGitHubURL(input.gitURL);
			bfcCollector.setGitHubLabel(input.label);
			bfcList = bfcCollector.collectFrom(commitList);

			break;

		case KEYWORD:
			bfcCollector = new BFCKeywordCollector();
			bfcList = bfcCollector.collectFrom(commitList);

			break;
		}

		// 4. Patch, BIC, Metric
		List<CSVInfo> csvInfoLst = null;

		PatchCollector patchCollector = null;
		BICCollector bicCollector = null;
		MetricCollector metricCollector = null;

		switch (input.mode) {
		case PATCH:
			patchCollector = new CPatchCollector(input);
			patchCollector.setBFC(bfcList);
			csvInfoLst = patchCollector.collectFrom(commitList);

			break;
		case BIC:
			bicCollector = new CBICCollector(input);
			bicCollector.setBFC(bfcList);
			csvInfoLst = bicCollector.collectFrom(commitList);

			break;
		case METRIC:
			metricCollector = new CMetricCollector(input);
			metricCollector.setBFC(bfcList);
			csvInfoLst = metricCollector.collectFrom(commitList);

			break;
		}

		// 5. Print CSV
		if (csvInfoLst.size() < 1) {
			return;
		}

		CSVMaker printer = new CSVMaker();
		printer.setDataType(csvInfoLst);
		printer.setPath(input);
		printer.print(csvInfoLst);

	}

	public static List<RevCommit> getCommitListFrom(File gitDir) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(gitDir);
		Iterable<RevCommit> walk = git.log().call();
		List<RevCommit> commitList = IterableUtils.toList(walk);

		return commitList;
	}

	public static File getGitDirectory(String project) {
		File clonedDirectory = new File("repositories" + File.separator + project);
		return clonedDirectory;
	}

	private static File GitClone(String remoteURI) throws InvalidRemoteException, TransportException, GitAPIException {
		String projectName = CLIConverter.getProjectName(remoteURI);
		File clonedDirectory = new File("repositories" + File.separator + projectName);
		clonedDirectory.mkdirs();
		System.out.println("cloning " + projectName + "...");
		Git git = Git.cloneRepository().setURI(remoteURI).setDirectory(clonedDirectory).setCloneAllBranches(true)
				.call();
		return git.getRepository().getDirectory();
	}

	private static boolean isCloned(String project) {
		File clonedDirectory = new File("repositories" + File.separator + project);
		return clonedDirectory.exists();
	}
}
