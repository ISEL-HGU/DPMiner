package gumtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;

import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.Utils;

public class Main {
	public static void main(String[] args) throws UnsupportedOperationException, IOException, InvalidRemoteException,
			TransportException, GitAPIException {

		String name = "/Users/imseongbin/Desktop/incubator-hudi";
		String csvName = "/Users/imseongbin/Desktop/hudi.csv";

		Git git = Git.open(new File(name));
		Repository repo = git.getRepository();
		RevWalk walk = new RevWalk(repo);

		for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
			
			if (entry.getKey().contains("refs/heads/master")) { // only master
				Ref ref = entry.getValue();
				RevCommit commit = walk.parseCommit(ref.getObjectId());
				walk.markStart(commit);
			}
		}
		int cnt = 0;


//		String header[] = {"commit-file","INS", "DEL", "MOV", "UPD" };
		String header[] = null;
		

		for (RevCommit commit : walk) {
			
			if(commit.getParentCount() < 1)
				continue;
			
			cnt++;
			RevCommit parent = null;
			try {
			parent = commit.getParent(0);
			} catch (Exception e) {
				continue;
			}
			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
			df.setRepository(repo);
			df.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS));
			df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
			df.setDetectRenames(true);

			List<DiffEntry> diffs;
			ArrayList<GChange> lst = new ArrayList<GChange>();

			try {

				diffs = df.scan(parent.getTree(), commit.getTree());
				for (DiffEntry diff : diffs) {

					String oldPath = diff.getOldPath();
					String newPath = diff.getNewPath();

					if (!newPath.endsWith(".java") || newPath.contains("test"))
						continue;

					String prevFileSource = Utils.fetchBlob(repo, commit.getId().getName() + "~1", oldPath);
					String fileSource = Utils.fetchBlob(repo, commit.getId().getName(), newPath);
					
					ArrayList<GChange> changes = MGumtree.diff(prevFileSource, fileSource);
//					System.out.println(prevFileSource.equals(fileSource));
					
					lst.addAll(changes);
					
					if(lst.size() < 1)
						continue;

					ArrayList<Integer> lst1 = new ArrayList<>();
					ArrayList<Integer> lst2 = new ArrayList<>();
					ArrayList<Integer> lst3 = new ArrayList<>();
					ArrayList<Integer> lst4 = new ArrayList<>();

					String[] strlst = new String[lst.size()];
//					strlst[0] = commit.getId().getName()+"-"+newPath;
					int count = 0 ;
					
					for (GChange change : lst) {
						switch (change.change_type) {
						case "INS":
							lst1.add(change.node_type);
							strlst[count] = "INS"+change.node_type;
							count++;
							break;

						case "DEL":
							lst2.add(change.node_type);
							strlst[count] = "DEL"+change.node_type;
							count++;
							break;

						case "UPD":
							lst3.add(change.node_type);
							strlst[count] = "UPD"+change.node_type;
							count++;
							break;

						case "MOV":
							lst4.add(change.node_type);
							strlst[count] = "MOV"+change.node_type;
							count++;
							break;
						}
					}
					
//					CSVmaker maker = new CSVmaker(new File("/Users/imseongbin/Desktop/"+commit.getId().getName()+newPath), null);
					String fn = "/Users/imseongbin/Desktop/txts/"+commit.getId().getName()+newPath.replace("/", "-")+".txt"; //TODO: File.seperator
					
					File f = new File(fn.replace("/", "////"));
					
					FileWriter print = new FileWriter(f);
					
					
//					String s1 = lst1.toString().substring(1, lst1.toString().length()-1);
//					String s2 = lst2.toString().substring(1, lst2.toString().length()-1);
//					String s3 = lst3.toString().substring(1, lst3.toString().length()-1);
//					String s4 = lst4.toString().substring(1, lst4.toString().length()-1);
					
					
					
					for(String s : strlst) {
						
						print.write(s);
						print.write(" ");
					}
					print.flush();
					print.close();
					
//					print.printRecord(strlst);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("cnt: " + cnt);
	}

	private static String pull(String name) throws FileNotFoundException {

		File f = new File(name);
		Scanner in = new Scanner(new FileReader(f));
		StringBuffer sb = new StringBuffer();
		while (in.hasNext()) {
			sb.append(in.nextLine());
			sb.append("\n");
		}

		return sb.toString();
	}

	static void t1() throws UnsupportedOperationException, IOException {

		Run.initGenerators();
		String file1 = "/Users/imseongbin/Desktop/Main1.java";
		String file2 = "/Users/imseongbin/Desktop/Main2.java";

		ITree src = Generators.getInstance().getTree(file1).getRoot();
		ITree dst = Generators.getInstance().getTree(file2).getRoot();
		Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
		m.match();

		ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
		g.generate();
		List<Action> actions = g.getActions();

		for (Action action : actions) {
			System.out.println(action.getName() + ", " + action.getNode().getType());
		}

	}
}
