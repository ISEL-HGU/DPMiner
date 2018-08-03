package edu.handong.csee.isel.patch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class MyExecutor extends Thread {
	private ArrayList<CommitStatus> commitStatusList;
	private String oldCommitHash;
	private String newCommitHash;
	private Git git;
	private Repository repository;
	private ArrayList<String> issueHashList;

	public ArrayList<CommitStatus> getCommitStatusList() {
		return commitStatusList;
	}

	public MyExecutor(String oldCommitHash, String newCommitHash, ArrayList<String> issueHashList, Git git,
			Repository repository) throws IOException {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;
		this.issueHashList = issueHashList;
		this.git = git;
		this.repository = repository;
	}

	@Override
	public void run() {
		CommitStatus newCommitStatus = null;
		commitStatusList = new ArrayList<CommitStatus>();
		try {
			RevWalk walk = new RevWalk(repository);
			ObjectId id = repository.resolve(newCommitHash);
			RevCommit commit = walk.parseCommit(id);

			// HashList에 있는 커밋인지 확인하는 중.
			boolean con = true;
//			for (String issueHash : issueHashList) {
//
//				if (commit.getShortMessage().contains(issueHash)) {
////					System.out.println("issue: " + issueHash + "\nshortMessage: " + commit.getShortMessage());
//					con = false;
//				}
//			}
			con = false;
			if (con) {
				newCommitStatus = null;
			}
			else {
				Patch p = new Patch(git, repository);
				HashMap<File,String> diffFiles = null;
				diffFiles = p.pullDiffs(oldCommitHash, newCommitHash);
				
				String project = "";
				String shortMessage = "";
				String commitHash = "";
				int date = 0;
				String author = "";
				
				project = "Hbase";
				shortMessage = commit.getShortMessage();
				commitHash = newCommitHash;
				date = commit.getCommitTime();
				author = commit.getAuthorIdent().getName();
				
				
				System.out.println("start~!");
				
				System.out.println(shortMessage);
				System.out.println(date);
				System.out.println(author);
				System.out.println(diffFiles);
				for (File diff : diffFiles.keySet()){
					if(diff == null)
						continue;
					System.out.println(diff);
					System.out.println(diffFiles.get(diff));
			        //System.out.println("key:"+mapkey+",value:"+mapobject.get(diff));
			        String patch = p.getStringFromFile(diff);
			        if(patch.equals(""))
			        	continue;
			        String path = diffFiles.get(diff);
			        newCommitStatus = null;
			        newCommitStatus = new CommitStatus(project, shortMessage, commitHash, date, author, path ,patch);
			        System.out.println(newCommitStatus);
			        commitStatusList.add(newCommitStatus);
			    }
				System.out.println("complete.");
				
				//ArrayList<String> patches = p.getStringFromFiles(diffFiles);
				
				
			}

		} catch (

		Exception e) {
			e.printStackTrace();
			System.out.println(newCommitStatus.toString());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}


}
