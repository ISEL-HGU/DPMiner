package edu.handong.csee.isel.metric;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.data.CSVInfo;

public interface MetricCollector {

	File collectFrom(List<RevCommit> commitList);

	void setBIC(List<String> bfcList);
}
