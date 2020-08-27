package edu.handong.csee.isel.bic.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.Utils;
import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.bic.szz.data.BICInfo;
import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphBuilder;
import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphModel;
import edu.handong.csee.isel.bic.szz.model.RevsWithPath;
import edu.handong.csee.isel.bic.szz.trace.Tracer;
import edu.handong.csee.isel.bic.szz.util.GitUtils;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;

public class AGSZZBICCollector implements BICCollector{
	
	List<String> bfcList = null;

	Git git;
	Repository repo;
	
	public AGSZZBICCollector() {
	}
	
	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;
	}

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) throws IOException {
		try {
			git = Git.open(edu.handong.csee.isel.Main.getGitDirectory());
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
		Utils.storeOutputFile(Input.outPath, Input.gitURL, BILines);
		
		return null;
	}
	
}
