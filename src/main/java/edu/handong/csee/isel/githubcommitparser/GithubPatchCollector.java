package edu.handong.csee.isel.githubcommitparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import edu.handong.csee.isel.patch.LocalGitRepositoryPatchCollector;

public class GithubPatchCollector {
	private String address = null;
	private String output = null;
	private String file = null;
	private String conditionMax = null;
	private String conditionMin = null;
	private String label = null;
	private boolean isThread;

	public GithubPatchCollector(String address, String output, String file, String conditionMax, String conditionMin, String label, boolean isThread) {
		this.address = address;
		this.output = output;
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
				iss.parseIssueAddress(oneAddress,label);
				if (iss.issueAddress.size() == 0) {

					System.out.println("There is not issue-space in" + oneAddress);
					System.out.println("Do you want to git-clone and parsing it?(y,n)");
					String yesOrNo = in.nextLine();
					if (yesOrNo.equalsIgnoreCase("y")) {

						System.out.println("git cloning..");
						String temp[] = oneAddress.split("/");
						String gitRepositoryPath = GitCloneFromURI(oneAddress + ".git", ".", temp[temp.length - 1]);

						new LocalGitRepositoryPatchCollector(gitRepositoryPath, output, null,
								Integer.parseInt(conditionMax), Integer.parseInt(conditionMin),isThread).run();
					} else {

						System.out.println("Fail to pull the data in " + oneAddress);
						failToAccess.add(oneAddress);
					}
					continue;

				}
				co.parseCommitAddress(oneAddress);
				co.parseAndPrintCommiContents(oneAddress, output, conditionMax, conditionMin);

				iss.issueAddress.clear();
				co.commitAddress.clear();
				co.commitLine.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (String failAddress : failToAccess) {
			System.out.println("Fail to access to " + failAddress);
		}
		System.out.println("You provided \"" + address + "\" as the value of the option a");

	}

	private String GitCloneFromURI(String URI, String importPass, String project) throws Exception {

		System.out.println("importPass: " + importPass);
		System.out.println("Project: " + project);
		String[] command = { "git", "clone", URI };
		executeCmd(command, importPass);

		return importPass + File.separator + project;
	}

	private void executeCmd(String[] cmd, String pathOfExcetue) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(pathOfExcetue));
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;
		while ((line = stdOut.readLine()) != null) {
			System.out.println(line);
		}
		while ((line = stdError.readLine()) != null)
			System.err.println("error: " + line);
		process.waitFor();

	}
}
