package edu.handong.csee.isel.patch.collector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.Main;
import edu.handong.csee.isel.Utils;
import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.csv.PatchInfo;
import edu.handong.csee.isel.data.processor.input.converter.CLIConverter;
import edu.handong.csee.isel.patch.PatchCollector;

public class CPatchCollector implements PatchCollector {

	List<String> bfcList = null;
	private Input input;

	public CPatchCollector(Input input) {
		this.input = input;
	}

	public CPatchCollector() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setBFC(List<String> bfcList) {
		this.bfcList = bfcList;

	}

	@Override
	public List<CSVInfo> collectFrom(List<RevCommit> commitList) {
		List<CSVInfo> csvInfoList = new ArrayList<>();

		for (RevCommit commit : commitList) {

			if (isBFC(commit)) {

				RevCommit parent = commit.getParent(0);

				if (parent == null) {
					System.err.println("WARNING: Parent commit does not exist: " + commit.name());
					continue;
				}

				List<CSVInfo> patch = getPatchBetween(parent, commit);
				int patchSize = getPatchSize(patch);

				if (patchSize < input.minSize || patchSize > input.maxSize) {
					continue;
				}

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
				patch.project = input.projectName;
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

	private int getChangedLine(String content) {
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

	private boolean isBFC(RevCommit commit) {

		for (String bfc : bfcList) {
			System.out.println(commit.getShortMessage());
			System.out.println(bfc);
			System.out.println();
			if (commit.getShortMessage().contains(bfc)) {
				System.out.println(commit.getShortMessage());
				return true;
			}
		}

		return false;
	}

	private Git openGitRepository() {
		File clonedDirectory = Main.getGitDirectory(input);
		Git git = null;
		try {
			git = Git.open(clonedDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return git;
	}
}
