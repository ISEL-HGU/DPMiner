package gumtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.tree.ITree;

public class Main {

	static File f1 = new File("/Users/imseongbin/samples/java/MultiMapping_v0.java");
	static File f2 = new File("/Users/imseongbin/samples/java/MultiMapping_v1.java");

	public static void main(String[] args) throws Exception {

		String s1 = read(f1);
		String s2 = read(f2);

//		System.out.println(s2);

		Run.initGenerators();

		ITree src = new JdtTreeGenerator().generateFromString(s1).getRoot();
		ITree dst = new JdtTreeGenerator().generateFromString(s1).getRoot();

		ArrayList<Integer> lst1 = readTree(src);
		ArrayList<Integer> lst2 = readTree(dst);

		System.out.println("lst1: ");
		for (Integer i : lst1) {
			System.out.println(i);
		}
		
		System.out.println();
		System.out.println("lst2: ");
		for (Integer i : lst2) {
			System.out.println(i);
		}

	}

	public static ArrayList<Integer> readTree(ITree t) {
		ArrayList<Integer> lst = new ArrayList<Integer>();

		return readTree(t, lst);

	}

	public static ArrayList<Integer> readTree(ITree t, ArrayList<Integer> lst) {
		for (ITree t1 : t.getChildren()) {
			int type = t1.getType();
			lst.add(type);
			readTree(t1, lst);
		}

		return lst;

	}

	public static String read(File f) throws FileNotFoundException {
		Scanner in = new Scanner(new FileReader(f));
		StringBuffer sb = new StringBuffer();
		while (in.hasNext()) {
			sb.append(in.nextLine());
			sb.append("\n");
		}

		return sb.toString();

	}
}
