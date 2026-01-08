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


        System.out.print("Enter path to Java file: ");
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

        System.out.println("\nEnter codes separated by comma :");

        String prefs = scanner.nextLine().toUpperCase();

        File outDir = new File("mutants_gen");
        if (!outDir.exists()) outDir.mkdir();

        generateMutants(cu, prefs);
    }

    private static void generateMutants(CompilationUnit cu, String prefs) {
        cu.accept(new VoidVisitorAdapter<Void>() {


            @Override
            public void visit(MethodCallExpr n, Void arg) {
                super.visit(n, arg);

                if ((shouldApply(prefs, "IPEX") || shouldApply(prefs, "ALL")) && n.getArguments().size() > 1) {
                    MethodCallExpr mutant = n.clone();
                    Expression arg0 = mutant.getArgument(0);
                    Expression arg1 = mutant.getArgument(1);
                    mutant.setArgument(0, arg1);
                    mutant.setArgument(1, arg0);
                    saveMutant(n, mutant, "IPEX_ParamSwap");
                }

                if (shouldApply(prefs, "IMCD") || shouldApply(prefs, "ALL")) {
                    saveMutantString(n, "/* Method Call Deleted */ null", "IMCD_CallDel");
                }

                if (shouldApply(prefs, "IREM") || shouldApply(prefs, "ALL")) {
                    saveMutantString(n, "(" + n.toString() + " + 1)", "IREM_ReturnMod");
                }

                if ((shouldApply(prefs, "IUOI") || shouldApply(prefs, "ALL")) && n.getArguments().size() > 0) {
                    MethodCallExpr mutant = n.clone();
                    Expression originalArg = mutant.getArgument(0);
                    mutant.setArgument(0, new UnaryExpr(originalArg, UnaryExpr.Operator.MINUS));
                    saveMutant(n, mutant, "IUOI_ArgUnary");
                }

                if ((shouldApply(prefs, "IPVR") || shouldApply(prefs, "ALL")) && n.getArguments().size() > 0) {
                    MethodCallExpr mutant = n.clone();
                    mutant.setArgument(0, new IntegerLiteralExpr("0"));
                    saveMutant(n, mutant, "IPVR_ParamToZero");
                }
            }

            @Override
            public void visit(BinaryExpr n, Void arg) {
                super.visit(n, arg);
                BinaryExpr.Operator op = n.getOperator();


                if (shouldApply(prefs, "AOR") && isArithmetic(op)) {
                    if (op == BinaryExpr.Operator.PLUS) mutateBinary(n, BinaryExpr.Operator.MINUS, "AOR_PlusToMinus");
                    else mutateBinary(n, BinaryExpr.Operator.PLUS, "AOR_Generic");
                }
                if (shouldApply(prefs, "AOD") && isArithmetic(op)) {
                    saveMutantString(n, n.getLeft().toString(), "AOD_LeftKeep");
                }

                if (shouldApply(prefs, "ROR") && isRelational(op)) {
                    if (op == BinaryExpr.Operator.EQUALS) mutateBinary(n, BinaryExpr.Operator.NOT_EQUALS, "ROR_EqToNeq");
                    else mutateBinary(n, BinaryExpr.Operator.EQUALS, "ROR_Generic");
                }


                if (shouldApply(prefs, "COR") && isConditional(op)) {
                    if (op == BinaryExpr.Operator.AND) mutateBinary(n, BinaryExpr.Operator.OR, "COR_AndToOr");
                    else mutateBinary(n, BinaryExpr.Operator.AND, "COR_OrToAnd");
                }
                if (shouldApply(prefs, "COD") && isConditional(op)) {
                    saveMutantString(n, n.getLeft().toString(), "COD_LeftKeep");
                }

                if (shouldApply(prefs, "SOR") && isShift(op)) {
                    if (op == BinaryExpr.Operator.LEFT_SHIFT) mutateBinary(n, BinaryExpr.Operator.SIGNED_RIGHT_SHIFT, "SOR_LtoR");
                    else mutateBinary(n, BinaryExpr.Operator.LEFT_SHIFT, "SOR_RtoL");
                }

                if (shouldApply(prefs, "LOR") && isLogical(op)) {
                    if (op == BinaryExpr.Operator.BINARY_AND) mutateBinary(n, BinaryExpr.Operator.BINARY_OR, "LOR_AndToOr");
                    if (op == BinaryExpr.Operator.BINARY_OR) mutateBinary(n, BinaryExpr.Operator.BINARY_AND, "LOR_OrToAnd");

                }
            }

            @Override
            public void visit(NameExpr n, Void arg) {
                super.visit(n, arg);

                if (shouldApply(prefs, "AOI")) {
                    if (n.getParentNode().isPresent() && !(n.getParentNode().get() instanceof UnaryExpr)) {
                        saveMutantString(n, n.toString() + "++", "AOI_Inc");
                    }
                }

                if (shouldApply(prefs, "COI")) {
                    saveMutantString(n, "!(" + n.toString() + ")", "COI_Negate");
                }

                if (shouldApply(prefs, "LOI")) {
                    saveMutantString(n, "~" + n.toString(), "LOI_BitwiseNot");
                }
            }

            @Override
            public void visit(UnaryExpr n, Void arg) {
                super.visit(n, arg);
                if (shouldApply(prefs, "LOD") && n.getOperator() == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
                    saveMutantString(n, n.getExpression().toString(), "LOD_RemoveNot");
                }
            }

            @Override
            public void visit(ExpressionStmt n, Void arg) {
                if (shouldApply(prefs, "SDL")) {
                    if (n.getParentNode().isPresent() && !(n.getParentNode().get() instanceof CompilationUnit)) {
                        saveMutantString(n, "// Statement Deleted by SDL", "SDL_Delete");
                    }
                }
                super.visit(n, arg);
            }

        }, null);

        System.out.println("\n---------------------------------------------");
        System.out.println("Finished! Total Mutants Generated: " + mutantId);
        System.out.println("Check 'mutants_gen' folder.");
    }


    private static boolean shouldApply(String prefs, String key) {
        return prefs.contains("ALL") || prefs.contains(key);
    }

    private static boolean isArithmetic(BinaryExpr.Operator op) {
        return op == BinaryExpr.Operator.PLUS || op == BinaryExpr.Operator.MINUS ||
                op == BinaryExpr.Operator.MULTIPLY || op == BinaryExpr.Operator.DIVIDE || op == BinaryExpr.Operator.REMAINDER;
    }

    private static boolean isRelational(BinaryExpr.Operator op) {
        return op == BinaryExpr.Operator.EQUALS || op == BinaryExpr.Operator.NOT_EQUALS ||
                op == BinaryExpr.Operator.GREATER || op == BinaryExpr.Operator.LESS ||
                op == BinaryExpr.Operator.GREATER_EQUALS || op == BinaryExpr.Operator.LESS_EQUALS;
    }

    private static boolean isConditional(BinaryExpr.Operator op) {
        return op == BinaryExpr.Operator.AND || op == BinaryExpr.Operator.OR;
    }

    private static boolean isShift(BinaryExpr.Operator op) {
        return op == BinaryExpr.Operator.LEFT_SHIFT || op == BinaryExpr.Operator.SIGNED_RIGHT_SHIFT;
    }

    private static boolean isLogical(BinaryExpr.Operator op) {
        return op == BinaryExpr.Operator.BINARY_AND || op == BinaryExpr.Operator.BINARY_OR || op == BinaryExpr.Operator.XOR;
    }

    private static void mutateBinary(BinaryExpr original, BinaryExpr.Operator newOp, String type) {
        BinaryExpr mutant = original.clone();
        mutant.setOperator(newOp);
        saveMutant(original, mutant, type);
    }

    private static void saveMutant(Node originalNode, Node newNode, String type) {
        saveMutantString(originalNode, newNode.toString(), type);
    }

    private static void saveMutantString(Node originalNode, String newString, String type) {
        if (!originalNode.getRange().isPresent()) return;

        mutantId++;
        String originalText = originalNode.toString();

        String mutatedCode = sourceCode.replaceFirst(java.util.regex.Pattern.quote(originalText), newString);

        try {
            String fileName = "mutants_gen/Mutant_" + mutantId + "_" + type + ".java";
            FileWriter writer = new FileWriter(fileName);
            writer.write(mutatedCode);
            writer.close();
            if(mutantId % 10 == 0) System.out.print(".");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}