package com.kotcrab.annotation.callsuper;

import com.kotcrab.annotation.CallSuperOverride;
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
			if (e.getAnnotation(CallSuperOverride.class) != null) return false;

			CodeAnalyzerTreeScanner codeScanner = new CodeAnalyzerTreeScanner();
			codeScanner.prEnv = processingEnv;
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
