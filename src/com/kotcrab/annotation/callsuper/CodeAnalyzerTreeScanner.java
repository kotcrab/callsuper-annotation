/*
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.annotation.callsuper;

import com.kotcrab.annotation.CallSuper;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree.JCIdent;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.annotation.Annotation;

class CodeAnalyzerTreeScanner extends TreePathScanner<Object, Trees> {
	private String methodName;
	private MethodTree method;
	private boolean callSuperUsed;

	public ProcessingEnvironment prEnv;

	@Override
	public Object visitClass (ClassTree classTree, Trees trees) {
		Tree extendTree = classTree.getExtendsClause();
		if (extendTree instanceof JCIdent) {
			JCIdent tree = (JCIdent) extendTree;
			Scope members = tree.sym.members();

			if (checkScope(members))
				return super.visitClass(classTree, trees);

			if (checkSuperTypes((ClassType) tree.type))
				return super.visitClass(classTree, trees);

		}
		callSuperUsed = false;

		return super.visitClass(classTree, trees);
	}

	public boolean checkSuperTypes (ClassType type) {
		if (type.supertype_field.tsym != null) {
			if (checkScope(type.supertype_field.tsym.members()))
				return true;
			else
				return checkSuperTypes((ClassType) type.supertype_field);
		}

		return false;
	}

	public boolean checkScope (Scope members) {
		for (Symbol s : members.getElements()) {
			if (s instanceof MethodSymbol) {
				MethodSymbol ms = (MethodSymbol) s;

				if (ms.getSimpleName().toString().equals(methodName)) {
					Annotation annotation = ms.getAnnotation(CallSuper.class);
					if (annotation != null) {
						callSuperUsed = true;
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public Object visitMethod (MethodTree methodTree, Trees trees) {
		if (methodTree.getName().toString().equals(methodName))
			method = methodTree;

		return super.visitMethod(methodTree, trees);
	}

	public void setMethodName (String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName () {
		return methodName;
	}

	public MethodTree getMethod () {
		return method;
	}

	public boolean isCallSuperUsed () {
		return callSuperUsed;
	}
}
