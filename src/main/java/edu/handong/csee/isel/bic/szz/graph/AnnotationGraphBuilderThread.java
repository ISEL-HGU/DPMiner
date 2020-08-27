package edu.handong.csee.isel.bic.szz.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Iterators;

import edu.handong.csee.isel.bic.szz.exception.EmptyHunkTypeException;
import edu.handong.csee.isel.bic.szz.model.Hunk;
import edu.handong.csee.isel.bic.szz.model.Line;
import edu.handong.csee.isel.bic.szz.model.LineType;
import edu.handong.csee.isel.bic.szz.model.RevsWithPath;
import edu.handong.csee.isel.bic.szz.util.GitUtils;
import edu.handong.csee.isel.bic.szz.util.Utils;

public class AnnotationGraphBuilderThread implements Runnable {
	private Repository repo;
	private RevsWithPath revsWithPath;
	public AnnotationGraphModel partitionedAnnotationGraph;

	public AnnotationGraphBuilderThread(Repository repo, RevsWithPath revsWithPath) {
		super();
		this.repo = repo;
		this.revsWithPath = revsWithPath;
	}

	@Override
	public void run() {
		try {
			partitionedAnnotationGraph = buildPartitionedAnnotationGraph(repo, revsWithPath);

		} catch (IOException | EmptyHunkTypeException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			// TODO Logging

		}
	}

	private AnnotationGraphModel buildPartitionedAnnotationGraph(Repository repo, RevsWithPath revsWithPath)
			throws IOException, EmptyHunkTypeException {
		// Generate Annotation Graph
		AnnotationGraphModel partitionedAnnotationGraph = new AnnotationGraphModel();

		int childIdx, hunkIdx, offset;
		int beginOfChild, endOfChild;
		Line childLine;
		Hunk hunk;
		String hunkType;

		// traverse all paths in the repo
		Iterator<String> paths = revsWithPath.keySet().iterator();

		Iterator<String> pathsForCalculatingSize = revsWithPath.keySet().iterator();
		int numOfPaths = Iterators.size(pathsForCalculatingSize);

		int pathCnt = 1;
		while (paths.hasNext()) {

			String path = paths.next();

			List<RevCommit> revs = revsWithPath.get(path);

			// 한 파일에 코드를 쓰고, 추가하고 고치고 수정한 것들을 모아서 한 커밋이 된다. 초기 커밋은 추후에 다른 커밋에 의해 코드가 수정되고 고쳐져서 바뀔수 있다. 
			// 이때 만약 커밋이 초기커밋 밖에 없다는 뜻은 이게 그냥 코드를 새로 추가해서 올린건데 그다음 커밋이 없으면 이게 결함을 가지고 있는지 없는지 알길이 없다. 
			// 그래서 커밋리스크 사이즈가 1이면 예외처리를 해준다. 
			// Skip building AG when the number of paths is 1 as it's not appropriate
			if(revs.size() == 1) 
				continue;

			// Generate subAnnotationGraph
			// commitList와 바뀐 Lines 세트가 여러개의 hashMap
			HashMap<RevCommit, ArrayList<Line>> subAnnotationGraph = new HashMap<RevCommit, ArrayList<Line>>();

			ArrayList<Line> parentLineList = new ArrayList<>();
			ArrayList<Line> childLineList = new ArrayList<>();

			// Logging
//			System.out.println("\n" + Thread.currentThread().getName() + " In progress (" + pathCnt + " / " + numOfPaths + ")");
//			System.out.println("\tBuilding Annotation Graph of " + path);

			for (RevCommit childRev : revs) {
				// Escape from the loop when there is no parent rev anymore
				// childRev가 0인데, revs.size가 1이라면 커밋이 하나밖에 없으니 조상 커밋은 없다. 
				if (revs.indexOf(childRev) == revs.size() - 1) break;

				RevCommit parentRev = revs.get(revs.indexOf(childRev) + 1);

				String parentContent = Utils.removeComments(GitUtils.fetchBlob(repo, parentRev, path)).trim();
				String childContent = Utils.removeComments(GitUtils.fetchBlob(repo, childRev, path)).trim();


				// get the parent line list from content
				configureLineList(parentLineList, path, parentRev, parentContent);

				// get the child line list only when initial iteration
				if (revs.indexOf(childRev) == 0)
					configureLineList(childLineList, path, childRev, childContent);

				ArrayList<Hunk> hunkList = configureHunkList(GitUtils.getEditListFromDiff(parentContent, childContent));

				// map child line with its ancestor(s)
				childIdx = 0; 
				hunkIdx = 0;
				offset = 0;

				while (childIdx < childLineList.size()) {
					boolean isIgnorable = false;
					childLine = childLineList.get(childIdx);

					// Case 1 - when there is no hunk anymore
					if (hunkList.size() <= hunkIdx) {
						childLine.setLineType(LineType.CONTEXT);

						mapChildLineWithAncestor(childIdx, offset, parentLineList, childLine);

						childIdx++;
						continue;
					}

					hunk = hunkList.get(hunkIdx);
					beginOfChild = hunk.getBeginOfChild();
					endOfChild = hunk.getEndOfChild();
					hunkType = hunk.getHunkType();


					// Case 2 - child index is out of hunk range
					if (childIdx < beginOfChild) {
						childLine.setLineType(LineType.CONTEXT);
						mapChildLineWithAncestor(childIdx, offset, parentLineList, childLine);

					}
					// Case 3 - child index is in hunk range
					else {
						switch (hunkType) {
						case "INSERT":
							// When childIdx is the last index in hunk, increment hunk index
							if (childIdx == endOfChild - 1) 
								hunkIdx++;

							childLine.setLineType(LineType.INSERT);
							childLine.setWithinHunk(true);

							offset--;

							break;

						case "REPLACE":
							// When childIdx is the last index in hunk, update offset and increment hunk index
							if (childIdx == endOfChild - 1) {
								offset += (hunk.getRangeOfParent() - hunk.getRangeOfChild());

								hunkIdx++;
							}

							// check whether format change happens
							String mergedParentContent = Utils.mergeLineList(parentLineList.subList(hunk.getBeginOfParent(), hunk.getEndOfParent()));
							String mergedChildContent = Utils.mergeLineList(childLineList.subList(hunk.getBeginOfChild(), hunk.getEndOfChild()));

							if (mergedParentContent.equals(mergedChildContent)) 
								childLine.setFormatChange(true);

							childLine.setLineType(LineType.REPLACE);
							childLine.setWithinHunk(true);
							mapChildLineWithAncestors(hunk, parentLineList, childLine);

							break;

						case "DELETE":
							// If the last child line is in DELETE, it maps with nothing
							if (childIdx == childLineList.size() - 1)
								break;
							
							// If the begin of child belongs to both DELETE and INSERT or both DELETE and REPALCE
							if (belongsToBothDELETEAndINSERT(hunkList, hunkIdx, beginOfChild) || belongsToBothDELETEAndREPLACE(hunkList, hunkIdx, beginOfChild)) {
								offset += hunk.getRangeOfParent();

								hunkIdx++;
								
								isIgnorable = true; // Iteration can be ignored in this situation. 

								break;
							}

							offset += hunk.getRangeOfParent();

							childLine.setLineType(LineType.CONTEXT);
							mapChildLineWithAncestor(childIdx, offset, parentLineList, childLine);

							hunkIdx++;

							break;

						default:
							throw new EmptyHunkTypeException();
						}
					}
					if(!isIgnorable) {
						childIdx++;
					}
				}

				// put lists of line corresponding to commit into subAG
				subAnnotationGraph.put(parentRev, parentLineList);
				subAnnotationGraph.put(childRev, childLineList);

				childLineList = parentLineList;
				parentLineList = new ArrayList<Line>();
			}
			// put subAG corresponding to path into AG
			partitionedAnnotationGraph.put(path, subAnnotationGraph);

			pathCnt++;
		}

		return partitionedAnnotationGraph;
	}

	private boolean belongsToBothDELETEAndINSERT(ArrayList<Hunk> hunkList, int currHunkIdx, int currBeginOfChild) {
		int nextHunkIdx = currHunkIdx + 1;

		if (nextHunkIdx < hunkList.size()) {
			String nextHunkType = hunkList.get(nextHunkIdx).getHunkType();
			int nextBeginOfChild = hunkList.get(nextHunkIdx).getBeginOfChild();

			if (nextHunkType.equals("INSERT") && currBeginOfChild == nextBeginOfChild) {
				return true;
			}
		}

		return false;
	}
	
	private boolean belongsToBothDELETEAndREPLACE(ArrayList<Hunk> hunkList, int currHunkIdx, int currBeginOfChild) {
		int nextHunkIdx = currHunkIdx + 1;

		if (nextHunkIdx < hunkList.size()) {
			String nextHunkType = hunkList.get(nextHunkIdx).getHunkType();
			int nextBeginOfChild = hunkList.get(nextHunkIdx).getBeginOfChild();

			if (nextHunkType.equals("REPLACE") && currBeginOfChild == nextBeginOfChild) {
				return true;
			}
		}
		
		return false;
	}

	private void configureLineList(ArrayList<Line> lst, String path, RevCommit rev, String content) {
		String[] contentArr = content.split("\r\n|\r|\n");

		for (int i = 0; i < contentArr.length; i++) {
			// make new Line
			List<Line> ancestors = new ArrayList<>();
			String committer = rev.getCommitterIdent().getName();
			String author = rev.getAuthorIdent().getName();
			String StringDateTime = Utils.getStringDateTimeFromCommitTime(rev);

			Line line = new Line(path, rev.getName(), contentArr[i], i, LineType.CONTEXT, ancestors, false, false, committer, author, StringDateTime);

			lst.add(line);
		}
	}

	private ArrayList<Hunk> configureHunkList(EditList editList) {
		ArrayList<Hunk> hunkList = new ArrayList<>();

		for (Edit edit : editList) {
			Hunk hunk = new Hunk(edit.getType().toString(), edit.getBeginA(), edit.getEndA(), edit.getBeginB(), edit.getEndB());
			
			hunkList.add(hunk);
		}

		return hunkList;
	}

	private void mapChildLineWithAncestor(int childIdx, int offset, List<Line> parentLineList, Line childLine) {
		Line ancestor = parentLineList.get(childIdx + offset);
		List<Line> ancestorsOfChild = childLine.getAncestors();
		
		ancestorsOfChild.add(ancestor);
		childLine.setAncestors(ancestorsOfChild);
	}

	private void mapChildLineWithAncestors(Hunk hunk, List<Line> parentLineList, Line childLine) {
		List<Line> ancestorsOfChild = parentLineList.subList(hunk.getBeginOfParent(), hunk.getEndOfParent());
		
		childLine.setAncestors(ancestorsOfChild);
	}

}
