package edu.handong.csee.isel.bic;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.utils.Utils;

public class Blamer {
	BlameCommand blamer;
	ObjectId commitID;
	BlameResult blame;
	String path;
		
	public Blamer(Repository repo,ObjectId id, String filePath) throws GitAPIException {
		this.commitID = id;
		this.path = filePath;
		blamer = new BlameCommand(repo);
		commitID = id;
		blamer.setStartCommit(commitID);
		blamer.setFilePath(path);
		blame = blamer.setDiffAlgorithm(Utils.diffAlgorithm).setTextComparator(Utils.diffComparator).setFollowFileRenames(true).call();
	}
	
	public static class OneLine {
		public OneLine(RevCommit commit, String path, int numLine) {
			this.commit = commit;
			this.path = path;
			this.num = numLine;
		}
		public RevCommit commit;
		public String path;
		public int num;
	}
	
	public OneLine blameOneLine(int numLine) {
		RevCommit commit = blame.getSourceCommit(numLine);
		String path = blame.getSourcePath(numLine);
		int numNewLine = blame.getSourceLine(numLine);
		return new OneLine(commit,path,numNewLine);
	}
}
