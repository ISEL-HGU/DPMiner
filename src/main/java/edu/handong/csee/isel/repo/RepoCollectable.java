package edu.handong.csee.isel.repo;

import java.io.IOException;
import java.util.HashSet;


public interface RepoCollectable {
	public HashSet<String> collectFrom() throws InterruptedException, IOException;
	public void getOneQueryData(String query);

}
