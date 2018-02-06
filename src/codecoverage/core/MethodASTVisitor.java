package codecoverage.core;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.jacoco.core.analysis.IClassCoverage;

import codecoverage.elements.BlockCodeElement;
import codecoverage.elements.BreakCodeElement;
import codecoverage.elements.CatchCodeElement;
import codecoverage.elements.ContinueCodeElement;
import codecoverage.elements.DoWhileCodeElement;
import codecoverage.elements.ForCodeElement;
import codecoverage.elements.ForeachCodeElement;
import codecoverage.elements.IfCodeElement;
import codecoverage.elements.MethodElement;
import codecoverage.elements.ReturnCodeElement;
import codecoverage.elements.SimpleCodeElement;
import codecoverage.elements.SwitchCaseCodeElement;
import codecoverage.elements.SwitchCodeElement;
import codecoverage.elements.ThrowCodeElement;
import codecoverage.elements.TryCodeElement;
import codecoverage.elements.WhileCodeElement;

public class MethodASTVisitor extends ASTVisitor {
	private Deque<AbstractBranchCodeElement> stack;
	private CompilationUnit cu;
	private IClassCoverage cc;
	private String currentLabel;
	private boolean labeledVisit = false;

	public MethodASTVisitor(MethodElement me, IClassCoverage cc) {
		stack = new ArrayDeque<AbstractBranchCodeElement>();
		stack.add(me);
		this.cu = me.getCu();
		this.cc = cc;
	}

	public void visitCode(ASTNode node) {
		AbstractBranchCodeElement el = stack.getLast();
		SimpleCodeElement code = new SimpleCodeElement();

		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		el.addElement(code);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		visitCode(node);
		return true;
	}

	@Override
	public boolean visit(Block node) {
		BlockCodeElement code = new BlockCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(Block node) {
		BlockCodeElement code = (BlockCodeElement) stack.getLast();
		if (stack.size() == 2) {
			code.setBlockWithEndTimestamp(true);
		}
		if (code.isBlockWithEndTimestamp()) {
			SimpleCodeElement element = new SimpleCodeElement();
			element.setStartLine(code.endLine);
			element.setEndLine(code.endLine);
			element.setThisNode(null);
			element.setTimestamps(cc.getLinesTimestamps(element.startLine));
			element.setNodeType(node.getNodeType());
			code.addElement(element);
		}
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		AbstractBranchCodeElement el = stack.getLast();
		String label = null;
		if (node.getLabel() != null) {
			label = node.getLabel().getIdentifier();
		}
		BreakCodeElement code = new BreakCodeElement(label);
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		el.addElement(code);
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		CatchCodeElement code = new CatchCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(CatchClause node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		AbstractBranchCodeElement el = stack.getLast();
		ContinueCodeElement code = new ContinueCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		el.addElement(code);
		return super.visit(node);
	}

	@Override
	public boolean visit(CreationReference node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Dimension node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		DoWhileCodeElement code = new DoWhileCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(DoStatement node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		ForeachCodeElement code = new ForeachCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		ForCodeElement code = new ForCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(ForStatement node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		IfCodeElement code = new IfCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(IfStatement node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(IntersectionType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		currentLabel = node.getLabel().getIdentifier();
		labeledVisit = true;
		// visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(LambdaExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberRef node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRef node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NameQualifiedType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PrimitiveType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		AbstractBranchCodeElement el = stack.getLast();
		ReturnCodeElement code = new ReturnCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		el.addElement(code);
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		if (!labeledVisit) {
			visitCode(node);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(SimpleName node) {
		labeledVisit = false;
		super.endVisit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		if (stack.getLast() instanceof SwitchCaseCodeElement) {
			stack.removeLast();
		}
		SwitchCaseCodeElement code = new SwitchCaseCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		SwitchCodeElement code = new SwitchCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setNodeType(node.getNodeType());
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(SwitchStatement node) {
		ICodeElement el = stack.getLast();
		if (el instanceof SwitchCaseCodeElement) {
			SwitchCaseCodeElement switchCase = (SwitchCaseCodeElement) el;
			switchCase.setLastCase(true);
			switchCase.repairLastCase();
			stack.removeLast();
		}
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TagElement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TextElement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		AbstractBranchCodeElement el = stack.getLast();
		ThrowCodeElement code = new ThrowCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setThisNode(node);
		code.setNodeType(node.getNodeType());
		el.addElement(code);
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		TryCodeElement code = new TryCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(TryStatement node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(UnionType node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		visitCode(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		WhileCodeElement code = new WhileCodeElement();
		code.setStartLine(cu.getLineNumber(node.getStartPosition()));
		code.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));
		code.setThisNode(node);
		code.setTimestamps(cc.getLinesTimestamps(code.startLine));
		code.setLabel(currentLabel);
		code.setNodeType(node.getNodeType());
		currentLabel = null;
		AbstractBranchCodeElement element = stack.getLast();
		if (element.addElement(code)) {
			stack.add(code);
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(WhileStatement node) {
		stack.removeLast();
		super.endVisit(node);
	}

	@Override
	public boolean visit(WildcardType node) {
		visitCode(node);
		return super.visit(node);
	}

}
