package edu.handong.csee.isel.runner;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import edu.handong.csee.isel.utils.Utils;

public class Input {
	public Input(String url, String resultDirectory, String reference, String label, Repository repository,
int conditionMin, int conditionMax, boolean isBI) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		super();
		this.url = url;
		this.outPath = resultDirectory;
		this.reference = reference;
		this.label = label;
		this.repository = repository;
		this.conditionMax = conditionMax;
		this.conditionMin = conditionMin;
		this.isBI = isBI;
//		this.lineLimited = 1 != conditionMax * conditionMin; // -1 * -1 = 1

		this.REMOTE_URI = url + ".git";
		this.projectName = Utils.getProjectName(REMOTE_URI);
		if (!outPath.endsWith(File.separator))
			outPath += File.separator;
		this.git = Utils.gitClone(REMOTE_URI);
	}
	
	public String url;
	public String reference = null;
	public String label = null;
	public Repository repository;
	public int conditionMax = -1;
	public int conditionMin = -1;
	public boolean isBI;
//	public boolean lineLimited;
	
	public final String REMOTE_URI;
	public final String projectName;
	public String outPath;
	public Git git;
}
