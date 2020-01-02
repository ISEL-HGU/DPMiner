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
	List<String> bfcList;

	public CMetricCollector(Input input) throws IOException {
		git = Git.open(Main.getGitDirectory(input));
		repo = git.getRepository();
	}

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) {
		File bowArff, cVectorArff;

		// TODO: 1. collect BOW arff
		BagOfWordsCollector bowCollector = new BagOfWordsCollector();
		bowCollector.setGit(git);
		bowCollector.setRepository(repo);
		bowCollector.setBFC(bfcList);
		bowCollector.setCommitList(commitList);
		bowCollector.collect();
		bowArff = bowCollector.getArff();
		

		// TODO: 2. collect Characteristic vector arff
		CharacteristicVectorCollector cVectorCollector = new CharacteristicVectorCollector();
		cVectorCollector.setGit(git);
		cVectorCollector.setRepository(repo);
		cVectorCollector.setBFC(bfcList);
		cVectorCollector.setCommitList(commitList);
		cVectorCollector.collect();
		cVectorArff = cVectorCollector.getArff();

		// TODO: 3. Meta data

		// TODO: 4. Merge 1,2,3, and return csv

		return null;
	}

	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;

	}

}
