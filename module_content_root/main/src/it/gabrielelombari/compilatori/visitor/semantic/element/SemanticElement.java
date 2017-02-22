package src.it.gabrielelombari.compilatori.visitor.semantic.element;

import com.sun.istack.internal.Nullable;
import src.it.gabrielelombari.compilatori.base.NodeConstants;
import src.it.gabrielelombari.compilatori.visitor.semantic.exception.ScopeException;

import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Created by Gabriele on 13/01/2017.
 */
public class SemanticElement {

    private String data;
    @Nullable
    private String type;
    private boolean canHaveScope, canBeAdded;
    private HashMap<String, SemanticElement> tos;

    public SemanticElement(String data, String type) {
        this.data = data;
        this.type = type;
        this.canHaveScope = (this.data.equals(NodeConstants.PROG_NODE) || this.data.equals(NodeConstants.PROC_DECL_OP));

        if (canHaveScope) tos = new HashMap<>();
    }

    public SemanticElement(String data) {
        this(data, null);
    }

    public boolean isCanHaveScope() {
        return canHaveScope;
    }

    public boolean isCanBeAdded() {
        return canBeAdded;
    }

    public void setCanBeAdded(boolean canBeAdded) {
        this.canBeAdded = canBeAdded;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void addToTos(SemanticElement semanticElement) throws ScopeException {
        if (tos.put(semanticElement.data, semanticElement) != null)
            throw new ScopeException(semanticElement.data);
    }

    public SemanticElement lookup(String data) {
        return tos.get(data);
    }

    public void printTos() {

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println();
        System.out.println("ToS per " + data);

        tos.forEach(new BiConsumer<String, SemanticElement>() {
            @Override
            public void accept(String s, SemanticElement semanticElement) {

                System.out.println(s + ": " + semanticElement);
                System.out.println();
            }
        });
        System.out.println("-----------------------------------------------------------------------------------------");


    }

    @Override
    public String toString() {
        return "SemanticElement{" +
                "data='" + data + '\'' +
                ", type='" + type + '\'' +
                ", canHaveScope=" + canHaveScope +
                ", canBeAdded=" + canBeAdded +
                ", tos=" + tos +
                '}';
    }
}
