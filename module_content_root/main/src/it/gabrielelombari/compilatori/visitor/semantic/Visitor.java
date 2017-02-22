package src.it.gabrielelombari.compilatori.visitor.semantic;

import com.scalified.tree.TreeNode;
import src.it.gabrielelombari.compilatori.base.Constants;
import src.it.gabrielelombari.compilatori.base.NodeConstants;
import src.it.gabrielelombari.compilatori.visitor.semantic.element.SemanticElement;
import src.it.gabrielelombari.compilatori.visitor.semantic.exception.ScopeException;
import src.it.gabrielelombari.compilatori.visitor.semantic.exception.TypeMismatchException;
import src.it.gabrielelombari.compilatori.visitor.semantic.exception.VariableNotDeclaredException;
import src.it.gabrielelombari.compilatori.visitor.element.VisitableNode;
import src.it.gabrielelombari.compilatori.visitor.syntactic.interfaces.IVisitor;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Gabriele on 13/01/2017.
 */
public class Visitor implements IVisitor<SemanticElement> {

    private Stack<SemanticElement> scope;
    private String lastType;

    public Visitor(Stack<SemanticElement> scope) {
        this.scope = scope;
    }

    @Override
    public String visit(VisitableNode<SemanticElement> vn) {

        SemanticElement curr = vn.data();

        if (curr.isCanHaveScope())
            scope.push(curr);

        Iterator<? extends TreeNode<SemanticElement>> iter = vn.subtrees().iterator();

        while (iter.hasNext()) {

            VisitableNode<SemanticElement> treeNodes = (VisitableNode<SemanticElement>) iter.next();

            if (!treeNodes.isLeaf()) {

                if (treeNodes.data().getData().equals(NodeConstants.PROC_DECL_OP))
                    lastType = Constants.PROCEDURE;

                checkDeclaration(treeNodes);

                checkType(treeNodes);

                treeNodes.accept(this);
            } else if (treeNodes.data().getData().equals(Constants.INTEGER) || treeNodes.data().getData().equals(Constants.BOOLEAN))
                lastType = treeNodes.data().getData();

            else if (treeNodes.data().isCanBeAdded()) {

                treeNodes.data().setType(lastType);

                try {
                    if (lastType.equals(Constants.PROCEDURE))
                        scope.get(scope.size() - 2).addToTos(treeNodes.data());
                    else scope.peek().addToTos(treeNodes.data());
                } catch (ScopeException e) {

                    System.out.println(e.getMessage());
                    System.exit(-1);
                }

            }

        }

        if (curr.isCanHaveScope()) {
            scope.pop().printTos();
            lastType = null;
        }

        return "OK";
    }

    public void checkDeclaration(VisitableNode<SemanticElement> treeNodes) {
        try {
            switch (treeNodes.data().getData()) {
                case NodeConstants.CONST_OP_NODE:
                    constOp(treeNodes);
                    break;
                case NodeConstants.ASSIGN_OP:
                case NodeConstants.READ_NODE:
                case NodeConstants.CALL_OP:
                case NodeConstants.VAR_OP_NODE:
                    identifierNode(treeNodes);
            }
        } catch (VariableNotDeclaredException e) {
            System.out.println(e.getMessage());
        }
    }

    public void constOp(VisitableNode<SemanticElement> constOpNode) {

        VisitableNode<SemanticElement> constantNode = constOpNode.getChild(0);
        SemanticElement constantValueNode = constantNode.getChild(0).data();
        String constantValue = constantValueNode.getData();

        if (constantValue.startsWith("'")) {
            constantValueNode.setType(Constants.CHAR);
        } else if (constantValue.startsWith("\"")) {
            constantValueNode.setType(Constants.STRING);
        } else if (constantValue.equals("true") || constantValue.equals("false")) {
            constantValueNode.setType(Constants.BOOLEAN);
        } else {
            constantValueNode.setType(Constants.INTEGER);
        }

        constantNode.data().setType(constantValueNode.getType());
        constOpNode.data().setType(constantValueNode.getType());
    }

    public void identifierNode(VisitableNode<SemanticElement> idParent) throws VariableNotDeclaredException {

        VisitableNode<SemanticElement> idNode = idParent.getChild(0);
        VisitableNode<SemanticElement> idValueNode = idNode.getChild(0);
        String idLexem = idValueNode.data().getData();

        SemanticElement lookuped = null;
        for (int i = scope.size() - 1; i >= 0; i--) {

            if ((lookuped = scope.get(i).lookup(idLexem)) != null)
                break;
        }

        if (lookuped == null)
            throw new VariableNotDeclaredException(String.format("Variable %s not declared", idLexem));

        idValueNode.data().setType(lookuped.getType());
        idNode.data().setType(lookuped.getType());
        idParent.data().setType(lookuped.getType());
    }

    public String checkType(VisitableNode<SemanticElement> node) {

        try {
            switch (node.data().getData()) {
                case NodeConstants.ASSIGN_OP: {
                    String type = assignOpTypeCheck(node);
                    node.data().setType(type);

                    return type;
                }
                case NodeConstants.RELOP_OP: {
                    String type = expressionTypeCheckAndGet(node);
                    node.data().setType(type);

                    return type;
                }
                case NodeConstants.WRITE_NODE: {
                    String type = null;
                    for (int i = 0; i < node.childsNum(); i++) {
                        type = expressionTypeCheckAndGet(node.getChild(i));

                        node.data().setType(type);
                    }
                    return type;
                }

                case NodeConstants.WHILE_OP:
                case NodeConstants.COMP_ST_OP:
                case NodeConstants.IF_OP:
                case NodeConstants.IF_OP_ESLE:
                    String type = statementTypeCheckAndGet(node);

                    node.data().setType(type);
                    return type;
            }
        } catch (TypeMismatchException e) {
            System.out.println(e.getMessage());
        }

        return null;

    }

    public String assignOpTypeCheck(VisitableNode<SemanticElement> assignOpNode) throws TypeMismatchException {

        checkDeclaration(assignOpNode);
        SemanticElement identifier = assignOpNode.getChild(0).data();
        String idType = identifier.getType();

        VisitableNode<SemanticElement> expressionNode = assignOpNode.getChild(1);
        String expressioneType = expressionTypeCheckAndGet(expressionNode);

        if (!idType.equals(expressioneType))
            throw new TypeMismatchException(String.format("Type mismatch in assignement of %s ", identifier.getData()));

        assignOpNode.data().setType(expressioneType);

        return expressioneType;
    }

    public String expressionTypeCheckAndGet(VisitableNode<SemanticElement> expressionNode) throws TypeMismatchException {
        //O una simple expression o una relop

        //Se simple expressione passare figlio 0
        switch (expressionNode.data().getData()) {
            case NodeConstants.SIMPLE_OP: {

                String type = simpleExpressionTypeCheckAndGet(expressionNode.getChild(0));

                expressionNode.data().setType(type);
                return type;
            }
            case NodeConstants.RELOP_OP: {
                VisitableNode<SemanticElement> simpleExpr1 = expressionNode.getChild(1);
                String typeSimpleExpr1 = simpleExpressionTypeCheckAndGet(simpleExpr1);

                VisitableNode<SemanticElement> simpleExpr2 = expressionNode.getChild(2);
                String typeSimpleExpr2 = simpleExpressionTypeCheckAndGet(simpleExpr2);

                if (!typeSimpleExpr1.equals(typeSimpleExpr2))
                    throw new TypeMismatchException(String.format("Type mismatch"));

                expressionNode.data().setType(typeSimpleExpr1);

                return typeSimpleExpr1;
            }
            default:
                return null;
        }
    }

    public String simpleExpressionTypeCheckAndGet(VisitableNode<SemanticElement> simpleExprNode) throws TypeMismatchException {

        checkDeclaration(simpleExprNode);

        switch (simpleExprNode.data().getData()) {
            case NodeConstants.ADD_OP:
            case NodeConstants.MUL_OP: {

                VisitableNode<SemanticElement> simpleExpr1 = simpleExprNode.getChild(1);
                String typeSimpleExpr1 = simpleExpressionTypeCheckAndGet(simpleExpr1);

                VisitableNode<SemanticElement> simpleExpr2 = simpleExprNode.getChild(2);
                String typeSimpleExpr2 = simpleExpressionTypeCheckAndGet(simpleExpr2);

                if (!typeSimpleExpr1.equals(typeSimpleExpr2))
                    throw new TypeMismatchException(String.format("Type mismatch"));

                simpleExprNode.data().setType(typeSimpleExpr1);
                simpleExpr1.data().setType(typeSimpleExpr1);
                simpleExpr2.data().setType(typeSimpleExpr1);

                return typeSimpleExpr1;
            }
            case NodeConstants.ID_NODE: {

                VisitableNode<SemanticElement> identifierNodeValue = simpleExprNode.getChild(0);

                String type = identifierNodeValue.data().getType();

                identifierNodeValue.data().setType(type);
                simpleExprNode.data().setType(type);

                return type;
            }
            case NodeConstants.CONST_NODE: {

                VisitableNode<SemanticElement> constantNode = simpleExprNode.getChild(0);
                VisitableNode<SemanticElement> constantNodeValue = constantNode.getChild(0);

                String type = constantNodeValue.data().getType();

                simpleExprNode.data().setType(type);

                return type;
            }
            case NodeConstants.EXPR_NODE:
            case NodeConstants.NOT_NODE: {

                VisitableNode<SemanticElement> expr = simpleExprNode.getChild(0);
                String type = expressionTypeCheckAndGet(expr);

                simpleExprNode.data().setType(type);
                expr.data().setType(type);

                return type;
            }
            case NodeConstants.UNARY_MINUS_NODE: {

                VisitableNode<SemanticElement> expr = simpleExprNode.getChild(0);
                String type = simpleExpressionTypeCheckAndGet(expr);

                simpleExprNode.data().setType(type);

                return type;
            }
            case NodeConstants.VAR_OP_NODE: {
                String type = simpleExpressionTypeCheckAndGet(simpleExprNode.getChild(0));

                simpleExprNode.data().setType(type);

                return type;
            }
            case NodeConstants.CONST_OP_NODE: {
                String type = simpleExprNode.data().getType();

                simpleExprNode.data().setType(type);

                return type;

            }
            default:
                return null;
        }
    }

    public String whileTypeCheck(VisitableNode<SemanticElement> whileNode) throws TypeMismatchException {

        VisitableNode<SemanticElement> expressionNode = whileNode.getChild(0);
        String expressionNodeType = expressionTypeCheckAndGet(expressionNode);

        VisitableNode<SemanticElement> statementNode = whileNode.getChild(1);
        String statementNodeType = checkType(statementNode);

        if (!expressionNodeType.equals(Constants.BOOLEAN) && !expressionNodeType.equals(Constants.INTEGER))
            throw new TypeMismatchException("Condition type's wrong");

        if (statementNodeType == null)
            throw new TypeMismatchException("Somethings goes wrong on statement's type checking!");

        whileNode.data().setType(statementNodeType);

        return Constants.VOID;
    }

    public String statementTypeCheckAndGet(VisitableNode<SemanticElement> statementNode) throws TypeMismatchException {

        String type = null;

        switch (statementNode.data().getData()) {
            case NodeConstants.WHILE_OP: {

                type = whileTypeCheck(statementNode);

                statementNode.data().setType(type);
                break;
            }
            case NodeConstants.COMP_ST_OP: {

                for (int i = 0; i < statementNode.childsNum(); i++) {

                    type = checkType(statementNode.getChild(i));
                }

                statementNode.data().setType(type);

                break;
            }

            case NodeConstants.IF_OP:
            case NodeConstants.IF_OP_ESLE: {

                type = ifStatementTypeChecking(statementNode);

                statementNode.data().setType(type);
            }
        }

        return type;
    }

    public String ifStatementTypeChecking(VisitableNode<SemanticElement> ifNode) throws TypeMismatchException {

        if (ifNode.data().getData().equals(NodeConstants.IF_OP_ESLE)) {

            VisitableNode<SemanticElement> elseNode = ifNode.getChild(2);
            String type = checkType(elseNode);

            if (type == null)
                new TypeMismatchException("Somethings goes wrong on statement's type checking!");
        }

        return whileTypeCheck(ifNode);
    }

}