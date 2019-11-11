package gumtree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;

public class MGumtree {
	public static ArrayList<GChange> diff(String s1, String s2) throws UnsupportedOperationException, IOException {

		Run.initGenerators();
		
		ITree src = new JdtTreeGenerator().generateFromString(s1).getRoot();
		ITree dst = new JdtTreeGenerator().generateFromString(s2).getRoot();

		Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
		m.match();

		ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
		g.generate();
		List<Action> actions = g.getActions();

		ArrayList<GChange> changes = new ArrayList<GChange>();
		
		for (Action action : actions) {
			
			changes.add(new GChange(action.getName(), action.getNode().getType()));
		}

		return changes;

	}
}
