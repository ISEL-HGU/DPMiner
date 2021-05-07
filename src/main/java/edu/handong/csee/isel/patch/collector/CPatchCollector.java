package edu.handong.csee.isel.patch.collector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.GitFunctions;
import edu.handong.csee.isel.Utils;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.csv.PatchInfo;
import edu.handong.csee.isel.patch.PatchCollector;

public class CPatchCollector implements PatchCollector {

	List<String> bfcList = null;
	String projectName;
	public int maxSize = 500;
	public int minSize = 0;
	public GitFunctions gitUtils;

	public CPatchCollector(String projectName, String outPath, String gitURL) {
		gitUtils = new GitFunctions(projectName, outPath, gitURL, false);
		this.projectName = projectName;
	}

	/**
	 *
	 * @param bfcList
	 */
	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;

	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) {
		List<CSVInfo> csvInfoList = new ArrayList<>();

		for (RevCommit commit : commitList) {

			if (Utils.isBFC(commit, bfcList)) {

				RevCommit parent = commit.getParent(0);

				if (parent == null) {
					System.err.println("WARNING: Parent commit does not exist: " + commit.name());
					continue;
				}

				List<CSVInfo> patch = getPatchBetween(parent, commit);
				int patchSize = getPatchSize(patch);

				if (patchSize < minSize || patchSize > maxSize) {
					continue;
				}
//				System.out.println("CPatchCollector collectFrom if Working");

				csvInfoList.addAll(patch);
			}
		}
		return csvInfoList;
	}

	private int getPatchSize(List<CSVInfo> patch) {
		int patchSize = 0;
		for (CSVInfo info : patch) {
			PatchInfo patchInfo = (PatchInfo) info;
			int changedLine = getChangedLine(patchInfo.patch);

			patchSize += changedLine;

		}

		return patchSize;
	}

	private List<CSVInfo> getPatchBetween(RevCommit parent, RevCommit commit) {

		List<CSVInfo> csvInfoList = new ArrayList<>();

		try {
			Git git = openGitRepository();
			Repository repo = git.getRepository();

			final List<DiffEntry> diffs = git.diff().setOldTree(Utils.prepareTreeParser(repo, parent.getId().name()))
					.setNewTree(Utils.prepareTreeParser(repo, commit.getId().name())).call();

			for (DiffEntry diff : diffs) {
				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();

				if (oldPath.equals("/dev/null") || newPath.indexOf("Test") >= 0 || !newPath.endsWith(".java"))
					continue;

				PatchInfo patch = new PatchInfo();
				patch.project = projectName;
				patch.commitName = commit.getName();
				patch.commitMessage = commit.getFullMessage();
				patch.date = Utils.getStringDateTimeFromCommit(commit);
				patch.author = commit.getAuthorIdent().getName();

				String content = getPatch(diff, repo);

				patch.patch = content;

				if (content != null) {
					csvInfoList.add(patch);
				}

			}

		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}

		return csvInfoList;
	}

	public int getChangedLine(String content) {
		if (content == null) {
			return 0;
		}
		int count = 0;
		String[] lines = content.split("\n");
		for (String line : lines) {
			if (isStartWithMinus(line) || isStartWithPlus(line)) {
				count++;
			}
		}

		return count;
	}

	public boolean isStartWithPlus(String line) {
		if (line.startsWith("+")) {
			if (line.startsWith("+++"))
				return false;
			return true;
		}
		return false;
	}

	public boolean isStartWithMinus(String line) {
		if (line.startsWith("-")) {
			if (line.startsWith("---"))
				return false;
			return true;
		}
		return false;
	}

	public String getPatch(DiffEntry diff, Repository repo) {
		String patch = null;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (DiffFormatter formatter = new DiffFormatter(output)) {
			formatter.setRepository(repo);
			formatter.format(diff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			patch = output.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return patch;
	}

	
	public Git openGitRepository() {
		File clonedDirectory = gitUtils.getGitDirectory();
		Git git = null;
		try {
			git = Git.open(clonedDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return git;
	}
}
