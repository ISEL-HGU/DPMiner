package edu.handong.csee.isel.commitUnitMetrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

public class CommitCollector {
	private String inputPath;
	private String outputPath;
	private Git git;
	private Repository repo;

	public CommitCollector(String gitRepositoryPath, String resultDirectory) {
		this.inputPath = gitRepositoryPath;
		this.outputPath = resultDirectory;
	}

	void countCommitMetrics() {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		MetricVariable metricVariable = new MetricVariable();
		MetricParser MetricParser = new MetricParser();
		ArrayList<MetricVariable> metricVariables = new ArrayList<MetricVariable>();

		try {
			git = Git.open(new File(inputPath));
			Iterable<RevCommit> initialCommits = git.log().call();
			repo = git.getRepository();

			int i = 1;

			for (RevCommit commit : initialCommits) {// 커밋
				MetricParser.resetValues();
				
				if (commit.getParentCount() == 0)
					break;
				RevCommit parent = commit.getParent(0);
				if (parent == null)
					continue;

				AbstractTreeIterator oldTreeParser = Utils.prepareTreeParser(repo, parent.getId().name().toString());
				AbstractTreeIterator newTreeParser = Utils.prepareTreeParser(repo, commit.getId().name().toString());

				List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
						// .setPathFilter(PathFilter.create("README.md")) //원하는 소스파일만 본다.
						.call();

				MetricParser.computeParsonIdent(commit.getAuthorIdent().toString());// 커밋한 사람. 
				metricVariable.setNumOfModifyFiles(diff.size());// 수정된 파일 개수

				for (DiffEntry entry : diff) {// 커밋안에 있는 소스파일
					try (DiffFormatter formatter = new DiffFormatter(byteStream)) { // 소스파일 내용
						formatter.setRepository(repo);
						formatter.format(entry);
						String diffContent = byteStream.toString(); // 한 소스파일 diff 내용을 저장
						
						MetricParser.computeLine(diffContent);
						
						byteStream.reset();
					}
				}

				if (i == 1)
					break; // 커밋 5개까지 본다.
				i++;
			}
			byteStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	void makeCsvFile() {
		
	}
}
