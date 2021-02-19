package edu.handong.csee.isel.bic.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;


import edu.handong.csee.isel.GitFunctions;

import edu.handong.csee.isel.Utils;
import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.bic.szz.data.BICInfo;
import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphBuilder;
import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphModel;
import edu.handong.csee.isel.bic.szz.model.RevsWithPath;
import edu.handong.csee.isel.bic.szz.trace.Tracer;
import edu.handong.csee.isel.bic.szz.util.GitUtils;
import edu.handong.csee.isel.data.CSVInfo;

/**
 * The {@code AGSZZBICCollector} class do collect BIC(bug introducing commit)<br>
 * using the annotation graph.<br>
 * It implements BICColletor which is interface about collecting BIC <br>
 * 
 * @author SJ
 * @author JY
 * @version 1.0
 */
public class AGSZZBICCollector implements BICCollector{
	
	private List<String> bfcList = null;
	private Git git;
	private Repository repo;
	private String outPath;
	private String gitURL;
	private String projectName;
	private GitFunctions gitUtils;
	
	public AGSZZBICCollector(String outPath, String projectName, String gitURL) {
		this.outPath = outPath;
		this.gitURL = gitURL;
		this.projectName = projectName;
		gitUtils = new GitFunctions(projectName, outPath, gitURL, true);
	}
	
	/**
     * setting BFC(bug fixing commit)
     *
     * @param bfcList The list that is only bug fixing commit name list.
     */
	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;
	}
	/**
	 * Make the annotation graph and than create bic csv file using annotation graph.
     * 
     * @param commitList The list that is all commit list from github project. 
     * @return null 
     * @see "AnnotationGraphBuilder" 
     * @see "AnnotationGraphModel" 
     * @see "RevCommit" 
     * 
     */
	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) throws IOException {
		try {
			git = Git.open(gitUtils.getGitDirectory());

		} catch (IOException e) {
			e.printStackTrace();
		}
		repo = git.getRepository();
		
		// Pre-step for building annotation graph
		// Colleting BFC (BFC is RevCommit)
		List<RevCommit> bfcCommitList = new ArrayList<RevCommit>();
		
		for (RevCommit commit : commitList) {

			if (commit.getParentCount() < 1) {
				System.err.println("WARNING: Parent commit does not exist: " + commit.name());
				continue;
			}
			
			if(!Utils.isBFC(commit, bfcList)) {
				continue;
			}
			
			bfcCommitList.add(commit);
		}
			
		List<String> targetPaths = GitUtils.getTargetPaths(repo, bfcCommitList); 
		
		RevsWithPath revsWithPath = GitUtils 
				.collectRevsWithSpecificPath(GitUtils.configurePathRevisionList(repo, commitList), targetPaths);
		
		// Phase 1 : Build the annotation graph
		AnnotationGraphBuilder agb = new AnnotationGraphBuilder(); 
		AnnotationGraphModel agm = agb.buildAnnotationGraph(repo, revsWithPath);

		// Phase 2 : Trace and collect BIC candidates and filter out format changes, comments, etc among candidates
		
		Tracer tracer = new Tracer();
		List<BICInfo> BILines = tracer.collectBILines(repo, bfcCommitList, agm, revsWithPath);
		// Sort BICs in the order FixSha1, BISha1, BIContent, biLineIdx
		Collections.sort(BILines);

		// Phase 3 : store outputs
		// GIT_URL : input.
		Utils.storeOutputFile(outPath, projectName, gitURL, BILines);
		
		return null;
	}
	
}
