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
////입력받은 깃허브 주소나 파일 주소가 있는지확인 
		if (address == null && file == null) {
			return;
		}

		FileReader fr = new FileReader();
//만약 파일 주소면 파일을 읽는다.
		if (file != null) {
			fr.readGithubAddressFile(file);
		} else {
			fr.githubAddress.add(address);//파일을 읽어 주소를 githubAddress에 저장 
		}

		IssueLinkParser iss = new IssueLinkParser();
		CommitParser co = new CommitParser();
	
		ArrayList<String> failToAccess = new ArrayList<String>();

		//깃허브 주소 하나를 가지고 issuelinkparser와 commitparser을 돌림 -> (만약 파일이 입력값이라 여러 주소가 있을 경우)다음 주소 접근 
		for (int i = 0; i < fr.githubAddress.size(); i++) {
			String oneAddress = fr.githubAddress.get(i);

			try { //issueLinkParser 돌림  
				iss.parseIssueAddress(oneAddress, label);
				if (iss.issueAddress.size() == 0) {

					failToAccess.add(oneAddress);
					continue;

				}
				//CommitParser돌림  
				co.parseCommitAddress(oneAddress);
				//co.parseAndPrintCommiContents(oneAddress, resultDirectory, conditionMax, conditionMin); 이부분은 commitHash출력하는데 필요 없음!

				iss.issueAddress.clear();
				co.commitAddress.clear();
				co.commitLine.clear();
				
				//arrayList초기화 
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
