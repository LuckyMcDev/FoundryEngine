package de.luckymcdev.foundryengine.common.script;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites every closure in a bundle script so it can be called with more
 * arguments than it declares. Declared parameters bind to the first incoming
 * arguments; extra arguments are ignored; missing ones fall back to their
 * initial expression or {@code null}. The implicit {@code it} binds to the
 * first argument.
 */
final class LenientClosureTransformer extends ClassCodeExpressionTransformer {

	private final SourceUnit source;
	private final VariableScope scope = new VariableScope();
	private int counter;
	private String itRename;

	private LenientClosureTransformer(SourceUnit source) {
		this.source = source;
	}

	private static Statement bind(VariableScope scope, String name, Expression value) {
		return GeneralUtils.declS(GeneralUtils.varX(name), value);
	}

	static CompilationCustomizer customizer() {
		return new CompilationCustomizer(CompilePhase.CANONICALIZATION) {
			@Override
			public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
				new LenientClosureTransformer(source).visitClass(classNode);
				new VariableScopeVisitor(source).visitClass(classNode);
			}
		};
	}

	@Override
	protected SourceUnit getSourceUnit() {
		return source;
	}

	@Override
	public Expression transform(Expression expression) {
		if (expression instanceof ClosureExpression closureExpression) {
			return rewrite(closureExpression);
		}
		if (itRename != null && expression instanceof VariableExpression variableExpression
			&& variableExpression.getName().equals("it")) {
			VariableExpression renamed = new VariableExpression(itRename);
			renamed.setSourcePosition(expression);
			return renamed;
		}
		return super.transform(expression);
	}

	private Expression rewrite(ClosureExpression closure) {
		Parameter[] parameters = closure.getParameters();
		Statement body = closure.getCode();

		if (parameters != null && parameters.length == 1 && parameters[0].getType().isArray()) {
			body.visit(this);
			return closure;
		}

		body.visit(this);
		if (parameters != null) {
			for (Parameter parameter : parameters) {
				if (parameter.hasInitialExpression()) {
					parameter.setInitialExpression(transform(parameter.getInitialExpression()));
				}
			}
		}

		String argsName = "__foundryArgs" + counter++;
		Expression argsVariable = GeneralUtils.varX(argsName);
		Expression length = GeneralUtils.propX(argsVariable, "length");
		List<Statement> bindings = new ArrayList<>();

		if (parameters == null) {
		} else if (parameters.length == 0) {
			String itName = "__foundryIt" + counter++;
			renameIt(body, itName);
			bindings.add(bind(scope, itName, GeneralUtils.ternaryX(
				GeneralUtils.gtX(length, GeneralUtils.constX(0, true)),
				GeneralUtils.indexX(argsVariable, GeneralUtils.constX(0, true)),
				GeneralUtils.nullX())));
		} else {
			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				Expression fallback = parameter.hasInitialExpression()
					? parameter.getInitialExpression()
					: GeneralUtils.nullX();
				bindings.add(bind(scope, parameter.getName(), GeneralUtils.ternaryX(
					GeneralUtils.gtX(length, GeneralUtils.constX(i, true)),
					GeneralUtils.indexX(argsVariable, GeneralUtils.constX(i, true)),
					fallback)));
			}
		}

		Statement newBody;
		if (bindings.isEmpty()) {
			newBody = body;
		} else {
			List<Statement> statements = new ArrayList<>(bindings.size() + 1);
			statements.addAll(bindings);
			statements.add(body);
			newBody = new BlockStatement(statements, new VariableScope());
		}

		Parameter varargsParameter = GeneralUtils.param(ClassHelper.OBJECT_TYPE.makeArray(), argsName);
		ClosureExpression result = GeneralUtils.closureX(new Parameter[]{varargsParameter}, newBody);
		result.setVariableScope(closure.getVariableScope());
		result.setSourcePosition(closure);
		return result;
	}

	private void renameIt(Statement body, String newName) {
		String previous = itRename;
		itRename = newName;
		body.visit(this);
		itRename = previous;
	}
}
