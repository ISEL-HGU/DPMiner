package edu.handong.csee.isel.bic.szz.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bic.szz.graph.AnnotationGraphModel;
import edu.handong.csee.isel.bic.szz.model.Line;
import edu.handong.csee.isel.bic.szz.model.RevsWithPath;
import edu.handong.csee.isel.bic.szz.util.GitUtils;
import edu.handong.csee.isel.bic.szz.util.Utils;
import edu.handong.csee.isel.bic.szz.data.BICInfo;

public class Tracer {
	private static final int REFACTOIRNG_THRESHOLD = 10;
	private HashSet<Line> BILines = new HashSet<>();
	private List<BICInfo> bicList = new ArrayList<>();
	private static ArrayList<Line> formatChangedLineList = new ArrayList<Line>();

	public Tracer() {
	}

	public List<BICInfo> collectBILines(Repository repo, List<RevCommit> BFCList, AnnotationGraphModel annotationGraph,
			RevsWithPath revsWithPath) throws IOException {

		// Phase 1 : Find path and line index for tracing
	
		for (RevCommit BFC : BFCList) {
			
			if (BFC.getParentCount() == 0)
				continue;

			RevCommit parentRev = BFC.getParent(0); // Get BFC pre-commit (i.e. BFC~1 commit)
			if (parentRev == null) {
				System.err.println("ERROR: Parent commit does not exist: " + BFC.name());
				break;
			}

			List<DiffEntry> diffs = GitUtils.diff(repo, parentRev.getTree(), BFC.getTree());

			/*
			 * HEURISTIC : If the number of changed path in BFC is greater than 10, that
			 * commit is highly likely to involve refactoring codes that can be noise for
			 * collecting BIC.
			 */
			if (REFACTOIRNG_THRESHOLD <= diffs.size())
				continue;

			for (DiffEntry diff : diffs) {
				String path = diff.getNewPath();

				// Ignore non-java file and test file
				if (!path.endsWith(".java") || path.contains("test"))
					continue;

				// get subAnnotationGraph 
				// path 는 key 값이다.
				HashMap<RevCommit, ArrayList<Line>> subAnnotationGraph = annotationGraph.get(path); // 

				// Skip when subAnnotationGraph is null, because building AG could be omitted for some reasons.
				// For example, building AG is omitted when there are only one path. See AnnotationGraphBuilderThread.java
				if (subAnnotationGraph == null) 
					continue;

				// get list of lines of BFC 
				ArrayList<Line> linesToTrace = subAnnotationGraph.get(BFC);

				// get preFixSource and fixSource
				String parentContent = Utils.removeComments(GitUtils.fetchBlob(repo, parentRev, path)).trim();
				String childContent = Utils.removeComments(GitUtils.fetchBlob(repo, BFC, path)).trim();

				// get line indices that fix bug
				
				// editList에는 parentContent와 childContents의 차이를 나타내는 editList이다.
				EditList editList = GitUtils.getEditListFromDiff(parentContent, childContent);
				for (Edit edit : editList) {
					int begin = -1;
					int end = -1;
					
					// edit.getType() 를 사용하게 되면 자동으로 해당 커밋은 어떤 타입의 커밋이었는지, delete인지, replace인지 등을 나타내다ㅣ. 
					switch (edit.getType()) {
					// 버그는 고쳤을때가 delete or replace인 경우 밖에 없으니까 
					// 여기서 하고 싶은건 버그를 고친 line들을 구하고 싶은것이다. 
					case DELETE:
						// 애네들은 phase2에서 쓰임. 
						begin = edit.getBeginA();
						end = edit.getEndA();

						/*
						 * Get a revision just before BFC among changed revisions with path
						 *
						 * [REMARK] This list is sorted in chronological order.
						 *
						 * Latest ------------> Oldest 
						 * [][][][][][][][][][][][][][][]
						 */
						List<RevCommit> changeRevsWithPath = revsWithPath.get(path);
						// 전단계를 얻으려고 한다. BFC가 deletion이였기 때문에 해당 버그는 삭제가 되었기 때문에 다시 복구하여 BIline을 찾을 수 있다.(childPath가 0번째이다.) 
						RevCommit changedPreBugFixRev = changeRevsWithPath.get(changeRevsWithPath.indexOf(BFC) + 1);

						linesToTrace = annotationGraph.get(path).get(changedPreBugFixRev);

						break;

					case REPLACE:
						// 애네들은 phase2에서 쓰임. 
						begin = edit.getBeginB();
						end = edit.getEndB();
						
						// replace는 현 BFC가 어떻게 바뀌었는지 알아내면 된다. 
						linesToTrace = annotationGraph.get(path).get(BFC);
						break;

					default:
						break;
					}

					// Phase 2 : trace
					if (0 <= begin && 0 <= end) {
						for (int i = begin; i < end; i++) {
							Line line = linesToTrace.get(i);
							// analysiz는 cli 모드이다. 
							trace(line);			
						}
					}
				}

				String fixSha1 = BFC.name() + "";
				String fixDate = Utils.getStringDateTimeFromCommitTime(BFC);

				for (Line line : BILines) {
					BICInfo bicInfo = new BICInfo(fixSha1, path, fixDate, line);
					bicList.add(bicInfo);
				}

				BILines.clear();
			}
		}

		return bicList;
	}

	public void trace(Line line) {
		// The fact that there are no ancestors means that the type of this line is INSERT
		// However, due to the limit of building AG algorithm, the type of line can be CONTEXT if the line is initially inserted in commit history.
		if (line.getAncestors().size() == 0) {
			if (!Utils.isWhitespace(line.getContent())) {			
				BILines.add(line);
			}
		}

		for (Line ancestor : line.getAncestors()) {
			// Lines that are not white space, not format change, and within hunk are BI Lines.
			if (!Utils.isWhitespace(ancestor.getContent())) {
				if (ancestor.isFormatChange() || !ancestor.isWithinHunk()) {
					trace(ancestor);
				} else {				
					BILines.add(ancestor);
				}
			}
		}
	}

	public void traceWithAnalysis(Line line, String BFC) {
		for (Line ancestor : line.getAncestors()) {
			// Lines that are not white space, not format change, and within hunk are BI Lines.
			if (!Utils.isWhitespace(ancestor.getContent())) {
				if (ancestor.isFormatChange()) {
					if (!formatChangedLineList.contains(line)) {
						System.out.println(String.join(",", BFC, line.getRev(), line.getPath(), line.getContent().strip()));

						formatChangedLineList.add(line);
					}
					traceWithAnalysis(ancestor, BFC);

				} else if (!ancestor.isWithinHunk()) {

					traceWithAnalysis(ancestor, BFC);
				} else {
					BILines.add(ancestor);
				}
			}
		}
	}
}
