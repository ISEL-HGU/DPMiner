
package newpackage;

import java.util.Arrays;
import java.util.List;

import newpackage.bfs.BFCGitHub;
import newpackage.bfs.BFCJira;
import newpackage.bfs.BFCKeyword;
import newpackage.bic.CBICCollector;
import newpackage.data.CSVInfo;
import newpackage.data.Input;
import newpackage.patch.CPatchCollector;

public class Main {
	static String[] bugKeywords = { "bug", "fix" };

	public static void main(String[] args) {

		// 1. Input
		InputConverter inputConverter = new CInputConverter();
		Input input = inputConverter.getInputFrom(args);

		// TODO: input process

		// 2. get commit list
		List<RevCommit> commitList = null;
		
		// 3. collect Bug-Fix-Commit
		List<String> bfcList = null;
		BFCCollector bfcCollector = null;

		List<String> bugIssueKeys = null; // TODO:
		List<String> bugKeywords = null;
		List<String> bugIDs = null; // TODO:

		switch (input.referecneType) {
		case JIRA:
			// TODO: collect bugIssueKeys
			bfcCollector = new BFCJira(bugIssueKeys);
//			bfcList = bfcCollector.collectFrom();

			break;

		case GITHUB:
			// TODO: collect bugIDs
			bfcCollector = new BFCGitHub(bugIDs);
//			bfcList = bfcCollector.collectFrom();

			break;

		case KEYWORD:
			bugKeywords = Arrays.asList(Main.bugKeywords);
			bfcCollector = new BFCKeyword(bugKeywords);
//			bfcList = bfcCollector.collectFrom();

			break;
		}

		// 4. Patch, BIC, Metric
		CSVInfo csvInfo = null;

		PatchCollector patchCollector = null;
		BICCollector bicCollector = null;
		MetricCollector metricCollector = null;

		switch (input.mode) {
		case PATCH:
			patchCollector = new CPatchCollector();
			csvInfo = patchCollector.collectFrom(bfcList);

			break;
		case BIC:
			bicCollector = new CBICCollector();
			csvInfo = bicCollector.collectFrom(bfcList);

			break;
		case METRIC:
			bicCollector = new CBICCollector();
			csvInfo = metricCollector.collectFrom(bfcList);

			break;
		}

		// 4. Result
		CSVPrinter printer = new CSVPrinter();
		printer.setPath("path");
		printer.print(csvInfo);

	}
}
