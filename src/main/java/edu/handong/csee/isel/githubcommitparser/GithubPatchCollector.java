package edu.handong.csee.isel.githubcommitparser;

import edu.handong.csee.isel.patch.LocalGitRepositoryPatchCollector;

public class GithubPatchCollector {
	private String address = null;
	private String output = null;
	private String file = null;
	private String conditionMax = null;
	private String conditionMin= null;

	public GithubPatchCollector(String address, String output, String file, String conditionMax, String conditionMin) {
		this.address = address;
		this.output = output;
		this.file = file;
		this.conditionMax = conditionMax;
		this.conditionMin = conditionMin;
		
	}

	public void run() {

		FileReader fr = new FileReader();

		fr.githubAddress.add(address);

		if (address == null && file == null) {
			return;
		}

		if (file != null) {
			fr.githubAddress.clear();
			fr.readGithubAddressFile(file);
		}

		IssueLinkParser iss = new IssueLinkParser();
		CommitParser co = new CommitParser();

		for (int i = 0; i < fr.githubAddress.size(); i++) {
			String oneAddress = fr.githubAddress.get(i);

			try {
				iss.parseIssueAddress(oneAddress);
				if(iss.issueAddress.size() == 0) {
					System.out.println("git cloning..");
					
					String gitRepositoryPath = GitCloneFromURL();
					
					new LocalGitRepositoryPatchCollector(gitRepositoryPath, output, null, conditionMax, conditionMin).run();
					return;
				}
				co.parseCommitAddress(oneAddress);
				co.parseAndPrintCommiContents(oneAddress, output, conditionMax,conditionMin);

				iss.issueAddress.clear();
				co.commitAddress.clear();
				co.commitLine.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("You provided \"" + address + "\" as the value of the option a");

	}

	private String GitCloneFromURL() {
		// TODO Auto-generated method stub
		return null;
	}
}
