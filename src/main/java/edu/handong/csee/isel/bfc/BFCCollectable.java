package edu.handong.csee.isel.bfc;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bfc.collector.jira.InvalidDomainException;
import edu.handong.csee.isel.bfc.collector.jira.InvalidProjectKeyException;

public interface BFCCollectable {
	List<String> collectFrom(List<RevCommit> commitList) throws IOException, InvalidProjectKeyException, InvalidDomainException;
}
