package edu.handong.csee.isel.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jgit.api.Git;
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

public class Patch {
	String directoryPath;
	File directory;
	Git git;
	Repository repository;
	ArrayList<String> commitHashList = new ArrayList<String>();
	ArrayList<String> branchList = new ArrayList<String>();

	public void setBranchList() throws GitAPIException {
		List<Ref> call = git.branchList().call();
		for (Ref ref : call) {
			branchList.add(ref.getName());
		}
	}

	public ArrayList<String> getBranchList() throws GitAPIException {
		return branchList;
	}

	public ArrayList<String> getAllPathList() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<String> getCommitHashList() {
		// TODO Auto-generated method stub
		return null;
	}

	public Patch(String directoryPath) throws IOException {
		this.directoryPath = directoryPath;
		this.directory = new File(directoryPath);
		this.git = Git.open(new File(directoryPath + "/.git"));
		this.repository = this.git.getRepository();
	}

	public void reset() {
		this.directoryPath = null;
		this.directory = null;
		this.git = null;
		this.repository = null;
	}

	public void set(String directoryPath) throws IOException {
		this.directoryPath = directoryPath;
		this.directory = new File(directoryPath);
		this.git = Git.open(new File(directoryPath + "/.git"));
		this.repository = this.git.getRepository();
	}

}
