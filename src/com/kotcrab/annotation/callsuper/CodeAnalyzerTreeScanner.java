package com.kotcrab.annotation.callsuper;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;

public class CodeAnalyzerTreeScanner extends TreePathScanner<Object, Trees> {
	private String methodName;
	private String baseClassName;

	private ArrayList<MethodTree> methodOverriders = new ArrayList<>();

	@Override
	public Object visitClass (ClassTree classTree, Trees trees) {
		if (checkMatch(classTree)) {
			List<? extends Tree> members = classTree.getMembers();

			for (Tree tree : members) {
				if (tree instanceof MethodTree) {
					MethodTree methodTree = (MethodTree) tree;
					if (methodTree.getName().toString().equals(methodName)) methodOverriders.add(methodTree);
				}
			}
		}

		return super.visitClass(classTree, trees);
	}

	private boolean checkMatch (ClassTree classTree) {
		Tree extendTree = classTree.getExtendsClause();

		return extendTree instanceof JCTree.JCIdent && (((JCTree.JCIdent) extendTree).sym).toString().equals(baseClassName);

	}

	public void setMethodName (String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName () {
		return methodName;
	}

	public void setBaseClassName (String baseClassName) {
		this.baseClassName = baseClassName;
	}

	public String getBaseClassName () {
		return baseClassName;
	}

	public void setMethodOverriders (ArrayList<MethodTree> methodOverriders) {
		this.methodOverriders = methodOverriders;
	}
}
