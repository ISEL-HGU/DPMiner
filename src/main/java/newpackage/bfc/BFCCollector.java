package newpackage.bfc;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public interface BFCCollector {

	List<String> collectFrom(List<RevCommit> commitList);

}
