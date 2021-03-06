# YASPL-Compiler

Compiler's front-end for YASPL, programming language made for Compiler class at University of Salerno.

A code example is reported above:

```

program exam;
    var somma1, somma2, result integer;
    mul1, mul2 integer;
    div1, div2 integer;
    pow1, pow2 integer;
    fibo integer;

    procedure fibonacci;
        var f1, f0, fn, i integer;
        begin
            f1 := 1;
            f0 := 0;
            fn := fibo;
            i := 2;
            while (i <= fibo) do
                begin
                    i := i + 1;
                    fn := f1 + f0;
                    f0 := f1;
                    f1 := fn
                end;
                ;
            ->(fn)
        end;

    procedure multiplication;
        var i, multi integer;
        begin
            multi := 0;
            i := 0;
            while(i < mul2) do
                begin
                    multi := multi + mul1;
                    i := i + 1
                 end;
             ;
            ->(multi)
    end;

    procedure power;
        var pow, i integer;

        begin
            pow := 1;
            i := 0;

            while (i < pow2) do
            begin
                pow := pow * pow1;
                i := i + 1
             end;
             ;
             ->(pow)
    end;

    begin

    /*
    var somma1, somma2, result integer;
        mul1, mul2 integer;
        div1, div2 integer;
        pow1, pow2 integer;
        fibo integer
    */

        somma1 := 20;
        somma2 := 30;
        mul1 := 5;
        mul2 := 4;
        div1 := 10;
        div2 := 5;
        pow1 := 3;
        pow2 := 4;
        fibo := 10;

        result := somma1 + somma2;
        ->("Il risultato della somma è:");
        ->(result);
        ->("Il risultato della moltiplicazione è:");
        multiplication;

        ->("Il risultato della potenza è:");
        power;

        ->("Il risultato della divisione è:");
        result := div1 / div2;
        ->(result);

        ->("Il risultato di fibonacci è:");
        fibonacci
    end
.
```

The source code can be divided in three sections:
- Variable declaration part;
- Procedure declaration part;
- Main part.

The compiler was generated using:
- JFlex to implements Lexical "parser";
- JavaCup to implements Syntactic parse;
- Visitor Pattern to perform semantic analysis and translation to C code.

To manage AST I used [Tree Data Structure Java Library](https://github.com/Scalified/tree) by [Scalified](https://github.com/Scalified).

The JFlex and JavaCup files are in the root project's folder and they were named, respectively, *esercizio2.flex* and *esercizio.cup*.
