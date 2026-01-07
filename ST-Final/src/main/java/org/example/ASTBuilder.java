package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;

import java.io.File;
import java.util.List;

public class ASTBuilder {

    public static void main(String[] args) throws Exception {
        // مسیر فایل Sample.java
        File file = new File("src/main/resources/Sample.java");

        // ساخت AST
        CompilationUnit cu = StaticJavaParser.parse(file);

        // چاپ AST
        System.out.println(cu);



        List<BinaryExpr> binaryExprs = cu.findAll(BinaryExpr.class);

        System.out.println("Found Binary Expressions:");
        for (BinaryExpr expr : binaryExprs) {
            System.out.println(expr + " ---> " + expr.getOperator());
        }

    }
}
