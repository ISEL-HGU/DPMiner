package edu.handong.csee.isel.parsers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Parser {
	public String[] convertStringAsLineList(String oneLine) {
		String[] newStrings = oneLine.split("\n");
		return newStrings;
	}

	public boolean isStartWithPlus(String str) {
		if (str.startsWith("+")) {
			if (str.startsWith("+++"))
				return false;
			return true;
		}
		return false;
	}

	public boolean isStartWithMinus(String str) {
		if (str.startsWith("-")) {
			if (str.startsWith("---"))
				return false;
			return true;
		}
		return false;
	}
	
	public int parseNumOfDiffLine(String inStr) {
		int count = 0;
		String[] newStrings = inStr.split("\n");
		for(String str : newStrings) {
			if(this.isStartWithMinus(str)||this.isStartWithPlus(str)) {
				count ++;
			}
		}
		
		return count;
	}
	
	// counting authors and commits
	public static void main(String[] args) throws IOException {
		Git git = Git.open(new File("/Users/imseongbin/Desktop/pivot"));
//		Git git = Git.open(new File("/Users/imseongbin/Desktop/incubator-brooklyn"));
		Repository repository = git.getRepository();
        HashSet<String> authors = new HashSet<String>();
        int count = 0;
        
		try (RevWalk walk = new RevWalk(repository)) {
			
			Collection<Ref> allRefs = repository.getAllRefs().values();

            try (RevWalk revWalk = new RevWalk( repository )) {
                for( Ref ref : allRefs ) {
                    revWalk.markStart( revWalk.parseCommit( ref.getObjectId() ));
                }
                System.out.println("Walking all commits starting with " + allRefs.size() + " refs: " + allRefs);
                
                for( RevCommit rev : revWalk ) {
                	String author = rev.getAuthorIdent().getName()+"<"+rev.getAuthorIdent().getEmailAddress()+">";
                    authors.add(author);
                    count++;
                }
            }
            
            for(String author : authors) {
                System.out.println(author);
            }
            System.out.println(count);
            System.out.println(authors.size());

            walk.dispose();
        }
	}
}
