package edu.handong.csee.isel.szz;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class SZZRunner {

	public static void main(String[] args) {
		/**
		 * gitDir and BFCCommit are test cases. 
		 * Be aware of changing directory and change bfc commit unless you use SukJinKim/DataForSZZ github repo.
		 */
		File gitDir = new File("/Users/kimsukjin/git/DataForSZZ"); 
		String BFCCommit = "768b0df07b2722db926e99a8f917deeb5b55d628";
		
		Git git;
		
		try {
			git = Git.open(gitDir);
			Iterable<RevCommit> walk = git.log().call();
			
		} catch (IOException | GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
