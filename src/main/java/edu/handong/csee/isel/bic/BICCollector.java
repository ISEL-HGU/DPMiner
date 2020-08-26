package edu.handong.csee.isel.bic;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.CSVInfo;

public interface BICCollector {

	void setBFC(List<String> bfcList);

	List<CSVInfo> collectFrom(List<RevCommit> commitList) throws IOException;

}
