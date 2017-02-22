package src.it.gabrielelombari.compilatori.visitor.syntactic;

import com.scalified.tree.TreeNode;
import src.it.gabrielelombari.compilatori.base.Constants;
import src.it.gabrielelombari.compilatori.visitor.semantic.element.SemanticElement;
import src.it.gabrielelombari.compilatori.visitor.element.VisitableNode;
import src.it.gabrielelombari.compilatori.visitor.syntactic.interfaces.IVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Created by Gabriele on 09/01/2017.
 */
public class Visitor implements IVisitor<SemanticElement> {

    private String toWriteIntoFile;

    @Override
    public String visit(VisitableNode<SemanticElement> vn) {

        toWriteIntoFile = String.format("<%s>", vn.data().getData());

        Iterator<? extends TreeNode<SemanticElement>> childs = vn.subtrees().iterator();

        while (childs.hasNext()) {

            VisitableNode<SemanticElement> child = (VisitableNode<SemanticElement>) childs.next();

            if (!child.isLeaf())
                toWriteIntoFile += child.accept(this);
            else {

                String data = child.data().getData();

                if (data.equals(Constants.INTEGER) || data.equals(Constants.BOOLEAN))
                    toWriteIntoFile += String.format("<%s/>", data);
                else
                    toWriteIntoFile += data.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;");
            }
        }

        toWriteIntoFile += String.format("</%s>", vn.data().getData());

//        System.out.println(toWriteIntoFile);
        return toWriteIntoFile;
    }

    public void toFile() throws Exception {
        File outputFile = new File("tree.xml");

        if (!outputFile.exists())
            outputFile.createNewFile();

        PrintWriter writer = new PrintWriter(outputFile);

        writer.write(toWriteIntoFile);

        writer.close();
    }

}
