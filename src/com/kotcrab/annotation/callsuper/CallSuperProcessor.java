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

import com.kotcrab.annotation.OverrideCallSuper;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("java.lang.Override") //processor is triggered by Override annotation
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CallSuperProcessor extends AbstractProcessor {
	private Trees trees;

	@Override
	public void init (ProcessingEnvironment pe) {
		super.init(pe);
		trees = Trees.instance(pe);
	}

	public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element e : roundEnv.getElementsAnnotatedWith(Override.class)) {
			if (e.getAnnotation(OverrideCallSuper.class) != null) return false;

			CodeAnalyzerTreeScanner codeScanner = new CodeAnalyzerTreeScanner();
			codeScanner.setMethodName(e.getSimpleName().toString());

			TreePath tp = trees.getPath(e.getEnclosingElement());
			codeScanner.scan(tp, trees);

			if (codeScanner.isCallSuperUsed()) {
				List list = codeScanner.getMethod().getBody().getStatements();

				if (!doesCallSuper(list, codeScanner.getMethodName())) {
					processingEnv.getMessager().printMessage(
							Kind.ERROR,
							"Overriding method '" + codeScanner.getMethodName() + "' must explicitly call super method from it's parent class",
							e);
				}
			}
		}

		return false;
	}

	private boolean doesCallSuper (List list, String methodName) {
		for (Object object : list) {
			if (object instanceof JCTree.JCExpressionStatement) {
				JCTree.JCExpressionStatement expr = (JCExpressionStatement) object;
				String exprString = expr.toString();
				if (exprString.startsWith("super." + methodName) && exprString.endsWith(");")) return true;
			}
		}

		return false;
	}
}
