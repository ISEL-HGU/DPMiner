package edu.handong.csee.isel.githubcommitparser;

import java.util.ArrayList;
import java.util.Scanner;

public class GithubPatchCollector {
	private String address = null;
	private String resultDirectory = null;
	private String file = null;
	private String conditionMax = null;
	private String conditionMin = null;
	private String label = null;
	private boolean isThread;

	public GithubPatchCollector(String address, String output, String file, String conditionMax, String conditionMin,
			String label, boolean isThread) {
		this.address = address;
		this.resultDirectory = output;
		this.file = file;
		this.conditionMax = conditionMax;
		this.conditionMin = conditionMin;
		this.label = label;
		this.isThread = isThread;
	}

	public void run() {

		if (address == null && file == null) {
			return;
		}

		FileReader fr = new FileReader();

		if (file != null) {
			fr.readGithubAddressFile(file);
		} else {
			fr.githubAddress.add(address);
		}

		IssueLinkParser iss = new IssueLinkParser();
		CommitParser co = new CommitParser();
		Scanner in = new Scanner(System.in);
		ArrayList<String> failToAccess = new ArrayList<String>();

		for (int i = 0; i < fr.githubAddress.size(); i++) {
			String oneAddress = fr.githubAddress.get(i);

			try {
				iss.parseIssueAddress(oneAddress, label);
				if (iss.issueAddress.size() == 0) {

					failToAccess.add(oneAddress);
					continue;

				}
				co.parseCommitAddress(oneAddress);
				co.parseAndPrintCommiContents(oneAddress, resultDirectory, conditionMax, conditionMin);

				iss.issueAddress.clear();
				co.commitAddress.clear();
				co.commitLine.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (String failAddress : failToAccess) {
			System.out.println("There is not issue-space in" + failAddress);
		}
		System.out.println("saved patches in \"" + resultDirectory + "\"");

	}
}
