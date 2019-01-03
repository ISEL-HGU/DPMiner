package edu.handong.csee.isel.newpackage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

public class Utils {
	public static Git gitClone(String REMOTE_URL)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		Pattern p = Pattern.compile(".*/(\\w+)\\.git");
		Matcher m = p.matcher(REMOTE_URL);
		m.find();
		File repositoriesDir = new File("repositories" + File.separator + m.group(1));

		if (repositoriesDir.exists()) {
			return Git.open(repositoriesDir);
		}

		repositoriesDir.mkdirs();
		return Git.cloneRepository().setURI(REMOTE_URL).setDirectory(repositoriesDir)
//				  .setBranch("refs/heads/master") // only master
				.setCloneAllBranches(true).call();
	}

	public static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

	private static ArrayList<String> getPathList(RevCommit commit, Repository repository) throws IOException {
		ArrayList<String> pathList = new ArrayList<String>();

		RevWalk walk = new RevWalk(repository);
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repository);
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		while (treeWalk.next()) {
			pathList.add(treeWalk.getPathString());
			// System.out.println(" found: " + treeWalk.getPathString());
		}

		walk.dispose();
		return pathList;
	}
	public static ArrayList<DiffEntry> listDiff(Repository repository, Git git, String oldCommit, String newCommit, int min, int max) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        ArrayList<DiffEntry> diffList = new ArrayList<>();
        for (DiffEntry diff : diffs) {
            
            switch(diff.getChangeType().ordinal()) {
            case 0: //ADD
            	continue;
            case 1: //MODIFY
            	if(!diff.getNewPath().endsWith(".java"))
            		continue;
            	
                ByteArrayOutputStream output = new ByteArrayOutputStream();
        		try (DiffFormatter formatter = new DiffFormatter(output)) {
        			formatter.setRepository(repository);
        			formatter.format(diff);
        		}
        		output.flush();
        		output.close();
        		String patch = output.toString("UTF-8");
        		if (patch.equals("") || (max != -1) && (min != -1)
						&& isExceedcondition(patch, max, min))
					continue;
        		diffList.add(diff);
        		
            case 2: //DELETE
            	continue;
            }
            
        }
        return diffList;
    }
	
	public static boolean isExceedcondition(String patch, int conditionMax, int conditionMin) {
		int line_count = parseNumOfDiffLine(patch);
		if (line_count > conditionMax || line_count < conditionMin) {
			return true;
		}
		return false;
	}
	
	private static boolean isStartWithPlus(String str) {
		if (str.startsWith("+")) {
			if (str.startsWith("+++"))
				return false;
			return true;
		}
		return false;
	}

	private static boolean isStartWithMinus(String str) {
		if (str.startsWith("-")) {
			if (str.startsWith("---"))
				return false;
			return true;
		}
		return false;
	}
	
	private static int parseNumOfDiffLine(String inStr) {
		int count = 0;
		String[] newStrings = inStr.split("\n");
		for(String str : newStrings) {
			if(isStartWithMinus(str)||isStartWithPlus(str)) {
				count ++;
			}
		}
		
		return count;
	}

	public static HashSet<String> parseReference(String reference) throws IOException {
		HashSet<String> keywords = new HashSet<String>();
		File CSV = new File(reference);
		Reader reader = new FileReader(CSV);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
		boolean first = true;
		
		for (CSVRecord record : records) {
			if(first) {
				first = false;
				continue;
			}
			keywords.add(record.get(1));
		}
		return keywords;
	}
	
}
