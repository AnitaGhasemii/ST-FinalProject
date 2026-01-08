package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.stmt.ExpressionStmt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class MutationEngine {

    private static int mutantId = 0;
    private static String sourceCode = "";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);


        System.out.print("Enter path to Java file (e.g., src/main/java/TestSubject.java): ");
        String filePath = scanner.nextLine();
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found!");
            return;
        }

        sourceCode = new String(Files.readAllBytes(file.toPath()));
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);

        System.out.println("\n Integration Operators");
        System.out.println("1. IPEX: Parameter Exchange (Swap)");
        System.out.println("2. IMCD: Method Call Deletion");
        System.out.println("3. IREM: Return Expression Modification");
        System.out.println("4. IUOI: Unary Operator Insertion on Args");
        System.out.println("5. IPVR: Parameter Variable Replacement (with Default)");

        System.out.println("\n Mutation Operators");
        System.out.println("6. AOR (Arithmetic Replacement)");
        System.out.println("7. AOD (Arithmetic Deletion)");
        System.out.println("8. AOI (Arithmetic Insertion)");
        System.out.println("9. ROR (Relational Replacement)");
        System.out.println("10. COR (Conditional Replacement)");
        System.out.println("11. COD (Conditional Deletion)");
        System.out.println("12. COI (Conditional Insertion)");
        System.out.println("13. SOR (Shift Replacement)");
        System.out.println("14. LOR (Logical Replacement)");
        System.out.println("15. LOD (Logical Deletion)");
        System.out.println("16. LOI (Logical Insertion)");
        System.out.println("17. SDL (Statement Deletion)");

        System.out.println("\nEnter codes separated by comma (e.g., AOR,IPEX,SDL) or 'ALL':");

        String prefs = scanner.nextLine().toUpperCase();

        File outDir = new File("mutants_gen");
        if (!outDir.exists()) outDir.mkdir();

    }
}