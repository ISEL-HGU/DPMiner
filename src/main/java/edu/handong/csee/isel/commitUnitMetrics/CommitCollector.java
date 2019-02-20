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
						// .setPathFilter(PathFilter.create("README.md")) //원하는 소스파일만 본다.
						.call();
				String commitHash = commit.getName();
			
				System.out.println(commitHash);
				
				metricVariable = new MetricVariable();
				metricVariables.put(commitHash, metricVariable);
				
				metricParser.computeParsonIdent(commitHash,commit.getAuthorIdent().toString());// 커밋한 사람
				metricVariable.setNumOfModifyFiles(diff.size());// 수정된 파일 개수
				
				TreeSet<String> pathOfDirectory = new TreeSet<String>();
				
				for (DiffEntry entry : diff) {// 커밋안에 있는 소스파일
	
					try (DiffFormatter formatter = new DiffFormatter(byteStream)) { // 소스파일 내용
						formatter.setRepository(repo);
						formatter.format(entry);
						String diffContent = byteStream.toString(); // 한 소스파일 diff 내용을 저장
						
						metricParser.computeLine(commitHash,diffContent);
						pathOfDirectory.add(entry.getNewPath().toString());
							
						byteStream.reset();
					}
				}
				metricParser.computeDirectory(commitHash, pathOfDirectory);
//				if (i == 12)
//					break; // 커밋 5개까지 본다.
				i++;
				System.out.println("\n");
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
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(outputPath+"/zxing.csv"));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Commit Hash","Modify Lines","Add Lines","Delete Lines","Distribution modified Lines","Modify Files","AuthorID","Directories"));
			for(String keyName : metricVariables.keySet()) {
				MetricVariable metric = metricVariables.get(keyName);
				csvPrinter.printRecord(keyName,metric.getNumOfModifyLines(),metric.getNumOfAddLines(),metric.getNumOfDeleteLines(),metric.getDistributionOfModifiedLines(),metric.getNumOfModifyFiles(),metric.getCommitAuthor(),metric.getNumOfDirectories());
			}
			csvPrinter.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
}