package src.it.gabrielelombari.compilatori.visitor.semantic.exception;

/**
 * Created by Gabriele on 13/01/2017.
 */
public class ScopeException extends Exception {

    public ScopeException(String message) {
        super("La variabile " + message + " è stata già dichirata");
    }
}
