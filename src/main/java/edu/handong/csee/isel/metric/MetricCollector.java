package edu.handong.csee.isel.metric;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.CSVInfo;

public interface MetricCollector {

	List<CSVInfo> collectFrom(List<RevCommit> commitList);

	void setBFC(List<String> bfcList);


}
