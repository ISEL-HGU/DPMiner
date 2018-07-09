package edu.handong.csee.isel.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;

public class BugPatchCollector {

	public static void main(String[] args) {
		String directoryPath = "/Users/imseongbin/documents/Java/BugPatchCollector";
		BugPatchCollector bc = new BugPatchCollector();

		String outPath = directoryPath;
		ArrayList<String> branchList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> commitHashList = new HashMap<String, ArrayList<String>>(); // <branch, commits>

		try {
			
			Patch p = new Patch(directoryPath);
			
			branchList = p.getBranchList();
//			System.out.println(branchList);
//			System.out.println("Successe");

			commitHashList = p.getCommitHashs();
//			bc.printCommitHashList(p,commitHashList);		
//			System.out.println("Successe");
			
//			bc.ShowdiffFromTwoCommits(p,commitHashList);
//			System.out.println("Successe");
			
			String patchsDirectory = (outPath + "/patchs");
			
//			File directory = new File(patchsDirectory) ;
//	        if(!directory.exists()) {
//	        	System.out.println("폴더가 없어유!!");
//	        	directory.mkdirs();
//	        }
	        
			p.makePatchsFromCommitsByBranchType(p, patchsDirectory);
			
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
	
	public void ShowdiffFromTwoCommits(Patch p,HashMap<String, ArrayList<String>> commitHashList) throws IOException, GitAPIException {
		Set<Entry<String, ArrayList<String>>> set = commitHashList.entrySet();
		Iterator<Entry<String, ArrayList<String>>> it = set.iterator();
		while (it.hasNext()) {
			Map.Entry<String, ArrayList<String>> e = (Map.Entry<String, ArrayList<String>>) it.next();
			System.out.print("@@@@@@@@@@@@@@@@@@@@@@@@ Branch: " + e.getKey());
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@\n");
			String[] hashList = p.makeArrayStringFromArrayListOfString(e.getValue());
			for(int i = 0;i<hashList.length-1;i++) {
				p.showFileDiff(hashList[i], hashList[i+1]);
			}
		}
	}
}
