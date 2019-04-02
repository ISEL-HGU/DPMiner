package edu.handong.csee.isel.commitUnitMetrics;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class CommitCollector {
	private String inputPath;
	private String outputPath;
	private Git git;
	private Repository repo;
	static HashMap<String,MetricVariable> metricVariables = new HashMap<String,MetricVariable>();
	
	ArrayList<RevCommit> commits = new ArrayList<RevCommit>();

	public CommitCollector(String gitRepositoryPath, String resultDirectory) {
		this.inputPath = gitRepositoryPath;
		this.outputPath = resultDirectory;
	}

	void countCommitMetrics() {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		MetricVariable metricVariable = new MetricVariable();

		MetricParser metricParser = new MetricParser();
		int count = 0;
		String authorId;

		try {
			git = Git.open(new File(inputPath));
			Iterable<RevCommit> initialCommits = git.log().call();
			repo = git.getRepository();

			for (RevCommit initialCommit : initialCommits) {
				commits.add(count,initialCommit);
				count++;
			}
			//arryaList index 0 = 3492 번째 커밋 
			//arrayList index 3491 = 1 번째 커밋 

			int i = 0;
			for (int commitIndex = commits.size()-1; commitIndex > -1; commitIndex--) {// 커밋
				RevCommit commit = commits.get(commitIndex);
				if (commit.getParentCount() == 0) continue;
				RevCommit parent = commit.getParent(0);
				if (parent == null)
					continue;

				AbstractTreeIterator oldTreeParser = Utils.prepareTreeParser(repo, parent.getId().name().toString());
				AbstractTreeIterator newTreeParser = Utils.prepareTreeParser(repo, commit.getId().name().toString());

				List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
						//.setPathFilter(PathFilter.create("java")) //원하는 소스파일만 본다.
						.call();
				String commitHash = commit.getName();
				int NumOfModifyFiles = 0;

				//System.out.println(commitHash);
//test 디렉토리 제거 아님 옵션으로 
				metricVariable = new MetricVariable();
				metricVariables.put(commitHash, metricVariable);

				authorId = metricParser.computeParsonIdent(commit.getAuthorIdent().toString());// 커밋한 사람
				metricVariable.setCommitAuthor(authorId);
				metricVariable.setCommitDate(commit.getAuthorIdent().getWhen().toString());

				TreeSet<String> pathOfDirectory = new TreeSet<String>();
				for (DiffEntry entry : diff) {// 커밋안에 있는 소스파일
					String sourcePath = entry.getNewPath().toString();
					
					if(!sourcePath.contains(".md")&&sourcePath.contains(".java")) {
						try (DiffFormatter formatter = new DiffFormatter(byteStream)) { // 소스파일 내용
							formatter.setRepository(repo);
							formatter.format(entry);
							String diffContent = byteStream.toString(); // 한 소스파일 diff 내용을 저장

							metricParser.computeLine(commitHash,diffContent);
							metricParser.computeSourceInfo(commitHash, entry.getNewPath().toString(), authorId);
							pathOfDirectory.add(entry.getNewPath().toString());
							NumOfModifyFiles++;
							
							byteStream.reset();
						}
					}
				}
				metricParser.computeDirectory(commitHash, pathOfDirectory);
				metricVariable.setNumOfModifyFiles(NumOfModifyFiles);// 수정된 파일 개수
//				if (i == 50)
//					break; // 커밋 5개까지 본다.
//				i++;
				//System.out.println("\n");
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

	void saveResultToCsvFile() {
		String projectName = null;
		BufferedWriter writer;
		Pattern pattern = Pattern.compile(".+/(.+)");
		Matcher matcher = pattern.matcher(inputPath);
		while(matcher.find()) {
			projectName = matcher.group(1);
		}
		try {
			writer = new BufferedWriter(new FileWriter(outputPath+"/"+projectName+".csv"));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Commit Hash","Modify Lines","Add Lines","Delete Lines","Distribution modified Lines","Modify Files","AuthorID","Directories","SumOfSourceRevision","SumOfDeveloper","CommitDate"));
			for(String keyName : metricVariables.keySet()) {
				MetricVariable metric = metricVariables.get(keyName);
				csvPrinter.printRecord(keyName,metric.getNumOfModifyLines(),metric.getNumOfAddLines(),metric.getNumOfDeleteLines(),metric.getDistributionOfModifiedLines(),metric.getNumOfModifyFiles(),metric.getCommitAuthor(),metric.getNumOfDirectories(),metric.getSumOfSourceRevision(),metric.getSumOfDeveloper(),metric.getCommitDate());
			}
			csvPrinter.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
}