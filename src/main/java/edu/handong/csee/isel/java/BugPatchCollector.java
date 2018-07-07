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
		File directory = new File(directoryPath);
		BugPatchCollector bc = new BugPatchCollector();

		/* 필요한게 Branch List, All path List, Commit-hash List, */

		ArrayList<String> branchList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> commitHashList = new HashMap<String, ArrayList<String>>(); // <branch, commits>
		HashMap<String, ArrayList<String>> allPathList = new HashMap<String, ArrayList<String>>(); // <commit, paths>

		try {

			Git git = Git.open(new File(directoryPath + "/.git"));
			Repository repository = git.getRepository();

			Patch p = new Patch(directoryPath);
			branchList = p.getBranchList();
			allPathList = p.getAllPathList();

			for (String branch : branchList) {
				p.setCommitHashs(branch);
			}

			System.out.println("Successe");

			commitHashList = p.getCommitHashs();
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

					allPathList.put(commitHash, pathList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
