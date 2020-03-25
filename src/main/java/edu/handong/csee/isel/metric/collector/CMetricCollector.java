package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.Main;
import edu.handong.csee.isel.metric.metadata.CommitCollector;
import edu.handong.csee.isel.szz.SZZRunner;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.csv.BICInfo;
import edu.handong.csee.isel.metric.MetricCollector;
import edu.handong.csee.isel.metric.metadata.CommitCollector;

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
		referencePath = input.outPath + File.separator + input.projectName + "-reference";
	}

	@Override
	public File collectFrom(List<RevCommit> commitList) {
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
		arffHelper.setOutPath(input.outPath);
		mergedArff = arffHelper.getMergedBOWArffBetween(bowCollector, cVectorCollector);

		// 4. Meta data, SJ help me

		List<String> bicList = getBICFromBFC(commitList);

		CommitCollector commitCollector = new CommitCollector(git, referencePath, bicList, input.projectName);
		//TODO: adjust date
		// StartDate,
																												// strEndDate,
																												// test
		commitCollector.countCommitMetrics();
		commitCollector.saveResultToCsvFile();
		String arffOutputPath = commitCollector.CSV2ARFF();

		File metaArff = new File(arffOutputPath); // TODO: Here your logic: make
													// metadata arff

		ArrayList<String> keyOrder = arffHelper.getKeyOrder();

		// 5. Merge 1,2,3, and return csv

		File resultArff = null;

		try {
			resultArff = arffHelper.makeMergedArff(mergedArff, metaArff, keyOrder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return resultArff;
	}

	private List<String> getBICFromBFC(List<RevCommit> commitList) {

		List<String> bfcList = new ArrayList<>();
		List<String> bicList = new ArrayList<>();

		for (RevCommit commit : commitList) {

			// 1. is bfc?
			if (!isBFC(commit)) {
				continue;
			}

			bfcList.add(commit.getName());

		}

		// 2. bfc -> bic
		SZZRunner bicTracker = new SZZRunner();
		bicTracker.setBFC(bfcList);
	
		List<CSVInfo> infoList = bicTracker.collectFrom(commitList);
		for(CSVInfo info : infoList) {
			
			if(!(info instanceof BICInfo)) {
				continue;
			}
			BICInfo bicInfo = (BICInfo) info;
			
			bicList.add(bicInfo.getBISha1());
		}
		

		return bicList;
	}

	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;

	}

	private boolean isBFC(RevCommit commit) {

		for (String bfc : bfcList) {
			if (commit.getShortMessage().contains(bfc) || commit.getName().contains(bfc)) {
				return true;
			}
		}

		return false;
	}

}
