package edu.handong.csee.isel.parser;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import edu.handong.csee.isel.runner.Input;
import edu.handong.csee.isel.utils.Utils;

public class Parser {
	public static final String RESET = "\u001B[0m";
	public static final String GREEN_BACKGROUND = "\u001B[42m";
	public static final String RED = "\u001B[31m";


	Input input;
	RevWalk walk; 
	Git git;
	public Parser(Input input) {
		this.input = input;
	}

	public void parse() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		Git git = Utils.gitClone(input.REMOTE_URI);
		Repository repo = git.getRepository();
		walk = new RevWalk(repo);

		for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
			if (entry.getKey().contains("refs/heads/master")) { // only master
				Ref ref = entry.getValue();
				RevCommit commit = walk.parseCommit(ref.getObjectId());
				walk.markStart(commit);
			}
		}

	}

}
