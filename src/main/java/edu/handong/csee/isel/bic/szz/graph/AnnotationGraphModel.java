package edu.handong.csee.isel.bic.szz.graph;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bic.szz.model.Line;

// 한 path당 커밋과 고친 라인들 
public class AnnotationGraphModel extends HashMap<String, HashMap<RevCommit, ArrayList<Line>>> {

}
