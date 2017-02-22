package src.it.gabrielelombari.compilatori.visitor.syntactic.interfaces;

import src.it.gabrielelombari.compilatori.visitor.element.VisitableNode;

/**
 * Created by Gabriele on 09/01/2017.
 */
public interface IVisitor<T> {

    String visit(VisitableNode<T> vn);
}
