package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;

import edu.handong.csee.isel.Utils;

/**
 * 
 * @author 
 *
 */
public class CharacteristicVectorCollector {
	private Git git;
	private Repository repo;
	private List<String> bicList = null;
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
			
			String BIDate = Utils.getStringDateTimeFromCommitTime(commit.getCommitTime());
			if(!(startDate.compareTo(BIDate)<=0 && BIDate.compareTo(endDate)<0)) // only consider BISha1 whose date is bewteen startDate and endDate
				continue;

			List<DiffEntry> diffs = Utils.diff(parent, commit, repo);

			for (DiffEntry diff : diffs) {
				String key = null;
				StringBuffer contentBuffer = new StringBuffer();

				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();

				if (newPath.indexOf("Test") >= 0 || !newPath.endsWith(".java"))
					continue;

				key = Utils.getKeyName(commit.getName(), newPath);

				String prevFileSource = Utils.fetchBlob(repo, commit.getId().getName() + "~1", oldPath);
				String fileSource = Utils.fetchBlob(repo, commit.getId().getName(), newPath);

				List<Action> vector = null;

				try {
					vector = getCharacteristicVector(prevFileSource, fileSource);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				for (Action element : vector) {
					switch (element.getName()) {
					case "INS":
					case "DEL":
					case "UPD":
					case "MOV":

						String changedNode = element.getName() + String.valueOf(element.getNode().getType());//node

						contentBuffer.append(changedNode);
						contentBuffer.append(" ");

						break;

					default:
						continue;
					}
				}

				String content = contentBuffer.toString().trim();
				
//				if(key.length() > 254) {
//					if(CMetricCollector.tooLongName.containsKey(key)) {
//						key = CMetricCollector.tooLongName.get(key).toString();
//					}else {
//						System.err.println("Error : can not find key");
//					}
//				}

				if (isBuggy(commit,diff)) {

					File changedVectorFile = new File(buggyDirectory + File.separator + key + ".txt");

					try {
						FileUtils.write(changedVectorFile, content, "UTF-8");
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					File changedVectorFile = new File(cleanDirectory + File.separator + key + ".txt");

					try {
						FileUtils.write(changedVectorFile, content, "UTF-8");
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

		String characteristicDirectoryPath = getCVectorirectoryPath();
		ArffHelper arffHelper = new ArffHelper();
		arffHelper.setProjectName(projectName);

		arff = arffHelper.getArffFromDirectory(characteristicDirectoryPath);
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
	 * @param prevFileSource
	 * @param fileSource
	 * @return
	 * @throws IOException
	 */
	public List<Action> getCharacteristicVector(String prevFileSource, String fileSource) throws IOException {

		Run.initGenerators();

		ITree src = new JdtTreeGenerator().generateFromString(prevFileSource).getRoot();
		ITree dst = new JdtTreeGenerator().generateFromString(fileSource).getRoot();

		Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
		m.match();

		ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
		g.generate();

		List<Action> actions = g.getActions();
		return actions;
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
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
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

	/**
	 * 
	 * @return
	 */
	public String getCVectorirectoryPath() {
		return referencePath + File.separator + projectName + "-characteristic_vector";
	}

	/**
	 * 
	 * @return
	 */
	public File getBuggyDirectory() {

		String directoryPath = getCVectorirectoryPath();
		String path = directoryPath + File.separator + "buggy";
		return new File(path);
	}

	/**
	 * 
	 * @return
	 */
	public File getCleanDirectory() {
		String directoryPath = getCVectorirectoryPath();
		String path = directoryPath + File.separator + "clean";
		return new File(path);
	}


}
