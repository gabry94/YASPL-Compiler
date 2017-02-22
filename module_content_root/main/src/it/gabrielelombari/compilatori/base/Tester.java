package src.it.gabrielelombari.compilatori.base;

import java_cup.runtime.Symbol;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by gabri on 10/21/2016.
 */
public class Tester {

    public static void main(String[] args) {

        try {
            Lexer l = new Lexer(new FileReader("finalCode.txt"));


            Symbol s = null;
            while ((s = l.next_token()).sym != 0)
                ;//System.out.println(s.sym);

            parser p = new parser(new Lexer(new FileReader("finalCode.txt")), "");
            p.parse();


        } catch (IOException e) {
            System.out.println("File problems");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
