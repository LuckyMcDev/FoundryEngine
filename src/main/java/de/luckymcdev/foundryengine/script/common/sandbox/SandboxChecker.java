package de.luckymcdev.foundryengine.script.common.sandbox;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.syntax.Types;

/**
 * Compile-time sandbox checks, run in two passes:
 * <ul>
 *   <li>{@link #preResolve()} at CONVERSION: imports, annotations (@Grab, @ASTTest) and
 *       member names, before names are resolved and before most AST transforms run</li>
 *   <li>{@link #postResolve()} at CANONICALIZATION: resolved types (constructors, casts,
 *       declarations, class literals, receivers)</li>
 * </ul>
 * Violations are reported as compilation errors with line numbers.
 * <p>
 * This layer is best-effort: Groovy is dynamic, so receivers are unknown and names can be
 * computed at runtime. Computed member names are rejected outright. The class loader filter
 * is the actual boundary; this layer closes the cheap paths and gives readable errors.
 */
public final class SandboxChecker extends ClassCodeVisitorSupport {

	private final SourceUnit source;
	private final boolean resolved;

	private SandboxChecker(SourceUnit source, boolean resolved) {
		this.source = source;
		this.resolved = resolved;
	}

	public static CompilationCustomizer preResolve() {
		return customizer(CompilePhase.CONVERSION, false);
	}

	public static CompilationCustomizer postResolve() {
		return customizer(CompilePhase.CANONICALIZATION, true);
	}

	private static CompilationCustomizer customizer(CompilePhase phase, boolean resolved) {
		return new CompilationCustomizer(phase) {
			@Override
			public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
				SandboxChecker checker = new SandboxChecker(source, resolved);
				if (!resolved) {
					ModuleNode module = source.getAST();
					if (module != null && !module.getClasses().isEmpty() && module.getClasses().get(0) == classNode) {
						checker.checkImports(module);
					}
				}
				checker.visitClass(classNode);
			}
		};
	}

	@Override
	protected SourceUnit getSourceUnit() {
		return source;
	}

	@Override
	public void addError(String msg, ASTNode expr) {
		if (expr != null && expr.getLineNumber() < 0) {
			return;
		}
		super.addError(msg, expr);
	}

	private void checkImports(ModuleNode module) {
		for (ImportNode node : module.getImports()) {
			checkImport(node, node.getClassName());
		}
		for (ImportNode node : module.getStarImports()) {
			checkImport(node, node.getPackageName());
		}
		for (ImportNode node : module.getStaticImports().values()) {
			checkImport(node, node.getClassName());
			if (node.getFieldName() != null && (SandboxRules.isMethodDenied(node.getFieldName())
				|| SandboxRules.isPropertyDenied(node.getFieldName()))) {
				addError("Static import of '" + node.getFieldName() + "' is not allowed in scripts", node);
			}
		}
		for (ImportNode node : module.getStaticStarImports().values()) {
			checkImport(node, node.getClassName());
		}
	}

	private void checkImport(ImportNode node, String name) {
		if (name != null && SandboxRules.isSourceTypeDenied(name)) {
			addError("Import of '" + name + "' is not allowed in scripts", node);
		}
		visitAnnotations(node);
	}

	@Override
	public void visitAnnotations(AnnotatedNode node) {
		for (AnnotationNode annotation : node.getAnnotations()) {
			String name = annotation.getClassNode().getName();
			String simple = name.substring(name.lastIndexOf('.') + 1);
			if (SandboxRules.DENIED_ANNOTATIONS.contains(simple)) {
				addError("Annotation @" + simple + " is not allowed in scripts", annotation);
			}
		}
		super.visitAnnotations(node);
	}

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {
		checkMemberName(call.getMethod(), call, false);
		super.visitMethodCallExpression(call);
	}

	@Override
	public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
		if (SandboxRules.isMethodDenied(call.getMethod())) {
			addError("Calling '" + call.getMethod() + "' is not allowed in scripts", call);
		}
		checkType(call.getOwnerType(), call);
		super.visitStaticMethodCallExpression(call);
	}

	@Override
	public void visitPropertyExpression(PropertyExpression property) {
		checkMemberName(property.getProperty(), property, true);
		super.visitPropertyExpression(property);
	}

	@Override
	public void visitAttributeExpression(AttributeExpression attribute) {
		checkMemberName(attribute.getProperty(), attribute, true);
		super.visitAttributeExpression(attribute);
	}

	@Override
	public void visitMethodPointerExpression(MethodPointerExpression pointer) {
		checkMemberName(pointer.getMethodName(), pointer, false);
		super.visitMethodPointerExpression(pointer);
	}

	@Override
	public void visitBinaryExpression(BinaryExpression expression) {
		// obj['classLoader'] is a property read through Object#getAt
		if (expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET
			&& expression.getRightExpression() instanceof ConstantExpression constant
			&& constant.getValue() instanceof String key
			&& (SandboxRules.isMethodDenied(key) || SandboxRules.isPropertyDenied(key))) {
			addError("Access to '" + key + "' is not allowed in scripts", expression);
		}
		super.visitBinaryExpression(expression);
	}

	private void checkMemberName(Expression name, ASTNode at, boolean property) {
		if (name instanceof ConstantExpression constant) {
			if (constant.getValue() instanceof String text) {
				boolean denied = property ? SandboxRules.isPropertyDenied(text) : SandboxRules.isMethodDenied(text);
				if (denied) {
					addError("Access to '" + text + "' is not allowed in scripts", at);
				}
			}
			return;
		}
		addError("Computed member names are not allowed in scripts", at);
	}

	@Override
	public void visitClassExpression(ClassExpression expression) {
		checkType(expression.getType(), expression);
		super.visitClassExpression(expression);
	}

	@Override
	public void visitConstructorCallExpression(ConstructorCallExpression call) {
		checkType(call.getType(), call);
		super.visitConstructorCallExpression(call);
	}

	@Override
	public void visitCastExpression(CastExpression expression) {
		checkType(expression.getType(), expression);
		super.visitCastExpression(expression);
	}

	@Override
	public void visitDeclarationExpression(DeclarationExpression expression) {
		if (expression.getLeftExpression() instanceof VariableExpression variable) {
			checkType(variable.getOriginType(), expression);
		}
		super.visitDeclarationExpression(expression);
	}

	@Override
	protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
		checkType(node.getReturnType(), node);
		for (Parameter parameter : node.getParameters()) {
			checkType(parameter.getType(), node);
		}
		super.visitConstructorOrMethod(node, isConstructor);
	}

	@Override
	public void visitField(FieldNode node) {
		checkType(node.getType(), node);
		super.visitField(node);
	}

	@Override
	public void visitClass(ClassNode node) {
		checkType(node.getSuperClass(), node);
		for (ClassNode iface : node.getInterfaces()) {
			checkType(iface, node);
		}
		super.visitClass(node);
	}

	private void checkType(ClassNode type, ASTNode at) {
		if (!resolved || type == null) {
			return;
		}
		ClassNode component = type;
		while (component.isArray()) {
			component = component.getComponentType();
		}
		String name = component.getName();
		if (SandboxRules.isSourceTypeDenied(name)) {
			addError("Use of '" + name + "' is not allowed in scripts", at);
		}
	}
}