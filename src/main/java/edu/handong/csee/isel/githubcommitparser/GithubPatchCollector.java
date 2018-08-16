package edu.handong.csee.isel.githubcommitparser;

public class GithubPatchCollector {
	static String address = null;
	static String output = null;
	static String file = null;
	static String printNumber = null;

	public GithubPatchCollector(String address, String output, String file, String printNumber) {

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
				co.parseCommitAddress(oneAddress);
				co.parseAndPrintCommiContents(oneAddress, output, printNumber);

				iss.issueAddress.clear();
				co.commitAddress.clear();
				co.commitLine.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("You provided \"" + address + "\" as the value of the option a");

	}
}
