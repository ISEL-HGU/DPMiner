package edu.handong.csee.isel.java;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;

public class BugPatchCollector {

	public static void main(String[] args) {
		String directoryPath = "/Users/imseongbin/documents/Java/BugPatchCollector";
		// BugPatchCollector bc = new BugPatchCollector();

		String outPath = directoryPath;

		try {

			Patch p = new Patch(directoryPath);

			// ArrayList<String> branchList = new ArrayList<String>();
			// branchList = p.getBranchList();
			// System.out.println(branchList);
			// System.out.println("Successe");

			// HashMap<String, ArrayList<String>> commitHashList = new HashMap<String,
			// ArrayList<String>>(); // <branch, commits>
			// commitHashList = p.getCommitHashs();
			// bc.printCommitHashList(p,commitHashList);
			// System.out.println("Successe");

			// bc.ShowdiffFromTwoCommits(p,commitHashList);
			// System.out.println("Successe");

			String patchsDirectory = (outPath + "/patchs");
			p.makePatchsFromCommitsByBranchType(p, patchsDirectory);
			// System.out.println("Successe");

		} catch (Exception e) {
			e.printStackTrace();
		}
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
