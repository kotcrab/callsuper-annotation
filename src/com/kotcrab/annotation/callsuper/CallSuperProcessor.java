package com.kotcrab.annotation.callsuper;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.kotcrab.annotation.callsuper.CallSuper")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CallSuperProcessor extends AbstractProcessor {
	private Trees trees;
	private CodeAnalyzerTreeScanner codeScanner;

	@Override
	public void init (ProcessingEnvironment pe) {
		super.init(pe);

		trees = Trees.instance(pe);
		codeScanner = new CodeAnalyzerTreeScanner();
	}

	public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element e : roundEnv.getElementsAnnotatedWith(CallSuper.class)) {
			ArrayList<MethodTree> methodOverriders = new ArrayList<MethodTree>();
			codeScanner.setMethodOverriders(methodOverriders);
			codeScanner.setMethodName(e.getSimpleName().toString());
			codeScanner.setBaseClassName(e.getEnclosingElement().toString());

			Set<? extends Element> elements = roundEnv.getRootElements();
			for (Element element : elements) {
				TreePath tp = trees.getPath(element);
				codeScanner.scan(tp, trees);
			}

			for (MethodTree methodTree : methodOverriders) {
				List list = methodTree.getBody().getStatements();
				if (!doesCallSuper(list, codeScanner.getMethodName()))
					processingEnv.getMessager().printMessage(
							Kind.ERROR,
							"Overriding method must call super method from '" + codeScanner.getBaseClassName() + "'",
							getElement(roundEnv, ((JCTree.JCMethodDecl) methodTree).sym.owner.toString(), methodTree.getName().toString())
					);
			}
		}

		return true;
	}

	private Element getElement (RoundEnvironment roundEnv, String className, String methodName) {
		Set<? extends Element> elements = roundEnv.getRootElements();
		for (Element element : elements) {
			if (element.toString().equals(className)) {
				for (Element innerElements : element.getEnclosedElements()) {
					String string = innerElements.toString();
					if (string.startsWith(methodName) && string.endsWith(")")) return innerElements;
				}
			}
		}

		return null;
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
