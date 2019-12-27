
package newpackage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;

import newpackage.bfc.BFCCollector;
import newpackage.bfc.collector.BFCGitHubCollector;
import newpackage.bfc.collector.BFCJiraCollector;
import newpackage.bfc.collector.BFCKeywordCollector;
import newpackage.bic.BICCollector;
import newpackage.bic.collector.CBICCollector;
import newpackage.data.CSVInfo;
import newpackage.data.Input;
import newpackage.data.processor.CLIConverter;
import newpackage.data.processor.CSVPrinter;
import newpackage.data.processor.InputConverter;
import newpackage.metric.MetricCollector;
import newpackage.metric.collector.CMetricCollector;
import newpackage.patch.PatchCollector;
import newpackage.patch.collector.CPatchCollector;

public class Main {
	static String[] bugKeywords = { "bug", "fix" };

	public static void main(String[] args) throws NoHeadException, IOException, GitAPIException {

		// 1. Input
		InputConverter inputConverter = new CLIConverter();
		Input input = inputConverter.getInputFrom(args); // TODO: input process

		// 2. get commit list
		File gitDir = Utils.Gitclone(input.gitRemoteURI);
		List<RevCommit> commitList = Utils.getCommitList(gitDir);

		// 3. collect Bug-Fix-Commit
		List<String> bfcList = null;
		BFCCollector bfcCollector = null;

		switch (input.referecneType) {
		case JIRA:
			List<String> bugIssueKeys = null; // TODO:
			bfcCollector = new BFCJiraCollector(bugIssueKeys);
			bfcList = bfcCollector.collectFrom(commitList);

			break;

		case GITHUB:
			List<String> bugIDs = null; // TODO:
			bfcCollector = new BFCGitHubCollector(bugIDs);
			bfcList = bfcCollector.collectFrom(commitList);

			break;

		case KEYWORD:
			List<String> bugKeywords = Arrays.asList(Main.bugKeywords);
			bfcCollector = new BFCKeywordCollector(bugKeywords);
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
			patchCollector = new CPatchCollector();
			csvInfoLst = patchCollector.collectFrom(bfcList);

			break;
		case BIC:
			bicCollector = new CBICCollector();
			csvInfoLst = bicCollector.collectFrom(bfcList);

			break;
		case METRIC:
			metricCollector = new CMetricCollector();
			csvInfoLst = metricCollector.collectFrom(bfcList);

			break;
		}

		// 5. Print CSV
		CSVPrinter printer = new CSVPrinter();
		printer.setPath(input.outPath);
		printer.print(csvInfoLst);

	}
}
