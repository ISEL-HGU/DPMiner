package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.Utils;
import edu.handong.csee.isel.patch.collector.CPatchCollector;

/**
 * 
 * @author 
 *
 */
public class BagOfWordsCollector {
	private Git git;
	private Repository repo;
	private List<String> bicList;
	private List<RevCommit> commitList;
	private String referencePath;
	private String projectName;
	private String startDate;
	private String endDate;
	

	private File arff = null;

	/**
	 * 
	 */
	public void collect() {

		File cleanDirectory = getCleanDirectory();
		File buggyDirectory = getBuggyDirectory();
		if (cleanDirectory.exists()) {
			cleanDirectory.delete();
		}
		if (buggyDirectory.exists()) {
			buggyDirectory.delete();
		}

		cleanDirectory.mkdirs();
		buggyDirectory.mkdirs();
		
		for (RevCommit commit : commitList) {
			
			if (commit.getParentCount() < 1) {
				System.err.println("WARNING: Parent commit does not exist: " + commit.name());
				continue;
			}
			
			RevCommit parent = commit.getParent(0);
			
			//time 
			String BIDate = Utils.getStringDateTimeFromCommitTime(commit.getCommitTime());
			if(!(startDate.compareTo(BIDate)<=0 && BIDate.compareTo(endDate)<0)) // only consider BISha1 whose date is bewteen startDate and endDate
				continue;

			List<DiffEntry> diffs = Utils.diff(parent, commit, repo);

			for (DiffEntry diff : diffs) {

				String key = null;
				StringBuffer contentBuffer = new StringBuffer();
				
				contentBuffer.append(commit.getFullMessage());
				contentBuffer.append("\n");

				String newPath = diff.getNewPath();

				if (newPath.indexOf("Test") >= 0 || !newPath.endsWith(".java"))
					continue;
				
				key = Utils.getKeyName(commit.getName(), newPath);
				
//				if(key.length() > 254) {
//					CMetricCollector.tooLongName.put(key, CMetricCollector.tooLongNameIndex);
//					key = Integer.toString(CMetricCollector.tooLongNameIndex);
//					CMetricCollector.tooLongNameIndex++;
//				}
			
				CPatchCollector helper = new CPatchCollector();
				String patch = helper.getPatch(diff, repo);

				for (String line : patch.split("\n")) {
					if (helper.isStartWithMinus(line) || helper.isStartWithPlus(line)) {

						contentBuffer.append(line);
						contentBuffer.append("\n");
					}
				}

				if (isBuggy(commit,diff)) {

					File deletedAndAddedLinesFile = new File(buggyDirectory + File.separator + key + ".txt");

					try {
						FileUtils.write(deletedAndAddedLinesFile, contentBuffer.toString(), "UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					File deletedAndAddedLinesFile = new File(cleanDirectory + File.separator + key + ".txt");

					try {
						FileUtils.write(deletedAndAddedLinesFile, contentBuffer.toString(), "UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	/**
	 * 
	 */
	public void makeArff() {

		String bowDirectoryPath = getBOWDirectoryPath();
		ArffHelper arffHelper = new ArffHelper();
		arffHelper.setProjectName(projectName);

		arff = arffHelper.getArffFromDirectory(bowDirectoryPath);
	}

	private boolean isBuggy(RevCommit commit, DiffEntry diff) {
		
		
		for (String bic : bicList) {
			if (commit.getShortMessage().contains(bic)) {
				return true;
			}
			String key = commit.getId().getName() + "-" + diff.getNewPath().toString();
			if(key.contains(bic)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return
	 */
	public String getBOWDirectoryPath() {
		return referencePath + File.separator + projectName + "-bow";
	}

	/**
	 * 
	 * @return
	 */
	public File getBuggyDirectory() {

		String directoryPath = getBOWDirectoryPath();
		String path = directoryPath + File.separator + "buggy";
		return new File(path);
	}

	/**
	 * 
	 * @return
	 */
	public File getCleanDirectory() {
		String directoryPath = getBOWDirectoryPath();
		String path = directoryPath + File.separator + "clean";
		return new File(path);
	}

	/**
	 * 
	 * @param commitList
	 */
	public void setCommitList(List<RevCommit> commitList) {
		this.commitList = commitList;
	}

	/**
	 * 
	 * @return
	 */
	public File getArff() {
		return arff;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getReferencePath() {
		return referencePath;
	}

	/**
	 * 
	 * @param git
	 */
	public void setGit(Git git) {
		this.git = git;
	}

	/**
	 * 
	 * @param repo
	 */
	public void setRepository(Repository repo) {
		this.repo = repo;
	}

	/**
	 * 
	 * @param bicList
	 */
	public void setBIC(List<String> bicList) {
		this.bicList = bicList;
	}

	/**
	 * 
	 * @param referencePath
	 */
	public void setReferencePath(String referencePath) {
		this.referencePath = referencePath;
	}

	/**
	 * 
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;

	}
	
	/**
	 * 
	 * @param startDate
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * 
	 * @param endDate
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	
}
