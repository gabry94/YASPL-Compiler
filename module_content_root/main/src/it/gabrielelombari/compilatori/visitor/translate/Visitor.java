package src.it.gabrielelombari.compilatori.visitor.translate;

import src.it.gabrielelombari.compilatori.base.Constants;
import src.it.gabrielelombari.compilatori.base.NodeConstants;
import src.it.gabrielelombari.compilatori.visitor.semantic.element.SemanticElement;
import src.it.gabrielelombari.compilatori.visitor.element.VisitableNode;
import src.it.gabrielelombari.compilatori.visitor.syntactic.interfaces.IVisitor;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Gabriele on 17/01/2017.
 */
public class Visitor implements IVisitor<SemanticElement> {

    String cCode, filename;

    @Override
    public String visit(VisitableNode<SemanticElement> vn) {

        cCode = "";
        switch (vn.data().getData()) {

            case NodeConstants.PROG_NODE: {
                cCode += "#include<stdio.h>\n" +
                        "typedef int bool;\n" +
                        "#define true 1\n" +
                        "#define false 0\n\n";

                this.filename = vn.getChild(0).getChild(0).data().getData() + ".c";
                cCode += vn.getChild(1).accept(this);

                break;
            }
            case NodeConstants.BLOCK: {

                for (int i = 0; i < vn.childsNum() - 1; i++)
                    cCode += vn.getChild(i).accept(this);

                cCode += "\nint main(){\n";

                cCode += vn.getChild(2).accept(this) + " ";

                cCode += "}";
                break;
            }

            case NodeConstants.VAR_DECL_OP: {
                VisitableNode<SemanticElement> typeNode = vn.getChild(0);

                cCode += (typeNode.data().getData().equals(Constants.INTEGER) ? "int" : "bool") + " ";

                for (int i = 1; i < vn.childsNum() - 1; i++) {
                    cCode += vn.getChild(i).accept(this) + ", ";
                }

                cCode += vn.getChild(vn.childsNum() - 1).accept(this) + ";\n";
                break;
            }

            case NodeConstants.ID_NODE: {
                cCode += vn.getChild(0).data().getData();

                break;
            }

            case NodeConstants.PROC_DECL_OP: {

                VisitableNode<SemanticElement> nameNode = vn.getChild(0);

                cCode += String.format("void %s(){\n", nameNode.getChild(0).data().getData());

                cCode += vn.getChild(1).accept(this);

                cCode += "\n}\n";


                break;
            }

            case NodeConstants.RELOP_OP: {

                cCode += vn.getChild(1).accept(this) + " ";

                cCode += vn.getChild(0).getChild(0).data().getData() + " ";

                cCode += vn.getChild(2).accept(this) + " ";

                break;
            }

            case NodeConstants.ADD_OP: {

                cCode += String.format("%s %s %s", vn.getChild(1).accept(this), vn.getChild(0).getChild(0).data().getData(), vn.getChild(2).accept(this));

                break;
            }

            case NodeConstants.MUL_OP: {

                cCode += String.format("%s %s %s", vn.getChild(1).accept(this), vn.getChild(0).getChild(0).data().getData(), vn.getChild(2).accept(this));

                break;
            }

            case NodeConstants.ASSIGN_OP: {

                cCode += String.format("%s = %s;\n", vn.getChild(0).accept(this), vn.getChild(1).accept(this));

                break;
            }

            case NodeConstants.READ_NODE: {
                cCode += "scanf(\"";

                for (int i = 0; i < vn.childsNum(); i++)
                    cCode += " %d ";

                cCode += "\"";

                for (int i = 0; i < vn.childsNum(); i++) {
                    cCode += ", &" + vn.getChild(i).accept(this);
                }

                cCode += "); \n";

                break;

            }

            case NodeConstants.WRITE_NODE: {
                cCode += "printf(\"";

                for (int i = 0; i < vn.childsNum(); i++) {
                    switch (vn.getChild(i).data().getType()) {
                        case Constants.INTEGER:
                        case Constants.BOOLEAN: {
                            cCode += " %d ";
                            break;
                        }
                        case Constants.STRING: {
                            cCode += " %s ";
                            break;
                        }
                        case Constants.CHAR: {
                            cCode += " %c ";
                            break;
                        }
                    }
                }

                cCode += "\"";

                for (int i = 0; i < vn.childsNum(); i++) {
                    cCode += ", " + vn.getChild(i).accept(this);
                }

                cCode += "); \n";

                break;
            }

            case NodeConstants.CALL_OP: {
                cCode += vn.getChild(0).accept(this) + "();\n";
                break;
            }

            case NodeConstants.WHILE_OP: {

                cCode += String.format("while(%s){\n%s}\n", vn.getChild(0).accept(this), vn.getChild(1).accept(this));
                break;
            }

            case NodeConstants.IF_OP: {
                cCode += String.format("if(%s){\n%s}", vn.getChild(0).accept(this), vn.getChild(1).accept(this));
                break;
            }
            case NodeConstants.IF_OP_ESLE: {
                cCode += String.format("if(%s){\n%s}\nelse{\n%s}", vn.getChild(0).accept(this), vn.getChild(1).accept(this), vn.getChild(2).accept(this));
                break;
            }
            case NodeConstants.NOT_NODE: {
                cCode += String.format("!(%s)", vn.getChild(0).accept(this));
                break;
            }
            case NodeConstants.CONST_NODE: {
                cCode += vn.getChild(0).data().getData();
                break;

            }

            default: {
                for (int i = 0; i < vn.childsNum(); i++) {
                    cCode += vn.getChild(i).accept(this);
                }
            }
        }

        return cCode;
    }

    public void toFile() throws Exception {
        File outputFile = new File(this.filename);

        if (!outputFile.exists())
            outputFile.createNewFile();

        PrintWriter writer = new PrintWriter(outputFile);

        writer.write(this.cCode);

        writer.close();
    }
}
