package edu.handong.csee.isel.patch;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.CSVInfo;

public interface PatchCollector {

	void setBFC(List<String> bfcList);

	List<CSVInfo> collectFrom(List<RevCommit> commitList);

}
