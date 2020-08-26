package edu.handong.csee.isel.bic.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;

import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphBuilder;
import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphModel;
import edu.handong.csee.isel.bic.szz.model.RevsWithPath;
import edu.handong.csee.isel.bic.szz.trace.Tracer;
import edu.handong.csee.isel.bic.szz.util.GitUtils;

import edu.handong.csee.isel.Utils;
//import edu.handong.csee.isel.bic.szz.util.Utils;

//import edu.handong.csee.isel.data.csv.BICInfo;
import edu.handong.csee.isel.bic.szz.data.BICInfo;

public class SZZBICCollector implements BICCollector{
	
	Input input;
	List<String> bfcommitList = null;

	Git git;
	Repository repo;
	
	public SZZBICCollector(Input input) {
		this.input = input;
	}
	
	@Override
	public void setBFC(List<String> bfcList) {
		// 7a26bedd92cff633d641118d19b5237ac8474105 리스트가 넘겨짐. 
		this.bfcommitList = bfcList;
	}

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) throws IOException {
		// TODO Auto-generated method stub
		try {
			git = Git.open(edu.handong.csee.isel.Main.getGitDirectory(input));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repo = git.getRepository();
		
		// Colleting BFCs 
		List<RevCommit> bfcList = new ArrayList<RevCommit>();
		
		for (RevCommit commit : commitList) {

			if (commit.getParentCount() < 1) {
				System.err.println("WARNING: Parent commit does not exist: " + commit.name());
				continue;
			}
			
			if(!Utils.isBFC(commit, bfcommitList)) {
				continue;
			}
			
			bfcList.add(commit);
		}
		
		// Colleting BFCs 
		//ArrayList<RevCommit> bfcList = GitUtils.getBFCLIST(bfcommitList, commitList);
		
		// Pre-step for building annotation graph
		
		// 파일이 고쳐진 새로운 getNewPath가 나온다. filter 가 된거. (.java 파일이고, test가 아닌것) (
		List<String> targetPaths = GitUtils.getTargetPaths(repo, bfcList); 
		
		// bfcList에서 계속 이어지는거임. BFCs는 jiraCrawler를 사용하여 얻은 targetPaths와 일치하는 것을 찾아냄. 이건 모든 이슈커밋리스트에서 
		// Wrong(X) -> 여기 안에 configurePathRevisionList(repo, revs) 이건 filter가 안된 부분이다. 
		RevsWithPath revsWithPath = GitUtils 
				.collectRevsWithSpecificPath(GitUtils.configurePathRevisionList(repo, commitList), targetPaths);
		// 한 path당 커밋 리스트를 부여해줌.  	
			
		
		// Phase 1 : Build the annotation graph
		final long startBuildingTime = System.currentTimeMillis();
		
		// 수진님한테 물어볼것 
		AnnotationGraphBuilder agb = new AnnotationGraphBuilder(); 
		// set debug mode = false for a while
		AnnotationGraphModel agm = agb.buildAnnotationGraph(repo, revsWithPath, false);

		final long endBuildingTime = System.currentTimeMillis();
		System.out.println("\nBuilding Annotation Graph takes " + (endBuildingTime - startBuildingTime) / 1000.0 + "s\n");

		// Phase 2 : Trace and collect BIC candidates and filter out format changes, comments, etc among candidates
		final long startTracingTime = System.currentTimeMillis();
		
		// 일단은 analysis, debug option은 false 로 세팅 
		Tracer tracer = new Tracer(false, false);
		List<BICInfo> BILines = tracer.collectBILines(repo, bfcList, agm, revsWithPath);

		final long endTracingTime = System.currentTimeMillis();
		System.out.println("\nCollecting BICs takes " + (endTracingTime - startTracingTime) / 1000.0 + "s\n");

		// Sort BICs in the order FixSha1, BISha1, BIContent, biLineIdx
		Collections.sort(BILines);

		// Phase 3 : store outputs
		// GIT_URL : input.
		Utils.storeOutputFile(input.outPath, input.gitURL, BILines);
		
		
		return null;
	}
	
}
