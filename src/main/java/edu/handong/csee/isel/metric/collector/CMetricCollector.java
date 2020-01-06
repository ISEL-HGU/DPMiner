package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.Main;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.metric.MetricCollector;

public class CMetricCollector implements MetricCollector {

	final Git git;
	final Repository repo;
	final String referencePath;
	final Input input;
	List<String> bfcList;

	public CMetricCollector(Input input) throws IOException {
		this.input = input;
		git = Git.open(Main.getGitDirectory(input));
		repo = git.getRepository();
		referencePath = input.outPath + File.separator + "reference";
	}

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) {
		File bowArff, cVectorArff;

		// 1. collect BOW arff
		BagOfWordsCollector bowCollector = new BagOfWordsCollector();
		bowCollector.setGit(git);
		bowCollector.setRepository(repo);
		bowCollector.setBFC(bfcList);
		bowCollector.setCommitList(commitList);
		bowCollector.setReferencePath(referencePath);
		bowCollector.setProjectName(input.projectName);
		bowCollector.collect();
//		bowCollector.makeArff(); //TODO: will be removed
		bowArff = bowCollector.getArff();

		// 2. collect Characteristic vector arff
		CharacteristicVectorCollector cVectorCollector = new CharacteristicVectorCollector();
		cVectorCollector.setGit(git);
		cVectorCollector.setRepository(repo);
		cVectorCollector.setBFC(bfcList);
		cVectorCollector.setCommitList(commitList);
		cVectorCollector.setReferencePath(referencePath);
		cVectorCollector.setProjectName(input.projectName);
		cVectorCollector.collect();
//		cVectorCollector.makeArff();
		cVectorArff = cVectorCollector.getArff();

		// 3. make merged arff between BOW and C-Vector
		File mergedArff = null;
		
		ArffHelper arffHelper = new ArffHelper();
		arffHelper.setReferencePath(referencePath);
		arffHelper.setProjectName(input.projectName);
		mergedArff = arffHelper.getMergedBOWArffBetween(bowCollector,cVectorCollector);
		
		// TODO: 4. Meta data

		// TODO: 5. Merge 1,2,3, and return csv

		return null;
	}

	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;

	}

}
