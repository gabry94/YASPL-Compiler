package src.it.gabrielelombari.compilatori.visitor.syntactic.interfaces;

/**
 * Created by Gabriele on 09/01/2017.
 */
public interface Visitable {

    String accept(IVisitor v);
}
