package edu.handong.csee.java.isel;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class BugPatchCollector {

	public static void main(String[] args) {
		File directory = new File("/Users/imseongbin/documents/Java/BugPatchCollector");
		
		
		try {
		
		Git git = Git.open(directory);
		Repository repository = git.getRepository();
		
		git.diff().setOutputStream( System.out ).call();
		
		
		
		
		} catch (Exception e) {System.out.println(e.fillInStackTrace());}
	}

}
