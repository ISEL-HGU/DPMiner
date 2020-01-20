
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
import edu.handong.csee.isel.data.processor.input.converter.CLIConverter;
import edu.handong.csee.isel.metric.MetricCollector;
import edu.handong.csee.isel.metric.collector.CMetricCollector;
import edu.handong.csee.isel.patch.PatchCollector;
import edu.handong.csee.isel.patch.collector.CPatchCollector;

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
		} else {
			// TODO: add exception when isCloned() && !isValidRepository()
			gitDirectory = GitClone(input);
		}
		commitList = getCommitListFrom(gitDirectory);

		// 3. collect Bug-Fix-Commit
		List<String> bfcList = null;
		BFCCollector bfcCollector = null;

		switch (input.referecneType) {
		case JIRA:
			bfcCollector = new BFCJiraCollector();
			bfcCollector.setJiraURL(input.jiraURL);
			bfcCollector.setJiraProjectKey(input.jiraProjectKey);
			bfcCollector.setOutPath(input.outPath);
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
//			bicCollector = new SZZRunner(getGitDirectory(input).getAbsolutePath());
			bicCollector.setBFC(bfcList);
			csvInfoLst = bicCollector.collectFrom(commitList);

			break;
		case METRIC: // TODO:
			metricCollector = new CMetricCollector(input);
			metricCollector.setBFC(bfcList);
			File arff = metricCollector.collectFrom(commitList);
			System.out.println("Metric was saved in " + arff.getAbsolutePath());

			return;
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

	private static boolean isValidRepository(Input input) {
		File directory = getGitDirectory(input);
		try {
			Git git = Git.open(directory);
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
