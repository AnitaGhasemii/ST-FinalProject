package org.example;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeInstrumenter {

    private static int nodeCounter = 0; // shomarande bra file ha

    public static void main(String[] args) throws IOException {
   //imput
        File sourceFile = new File("C:\\Users\\Asus\\Desktop\\final ST d\\ST-Final\\src\\main\\resources\\Sample.java");

    // final
        File outputFile = new File("src/InstrumentedTargetCode.java");

        instrument(sourceFile, outputFile);
        System.out.println("Instrumentation finished! Output saved to: " + outputFile.getPath());
    }

    public static void instrument(File source, File destination) throws IOException {
        nodeCounter = 0; //reset counter


        CompilationUnit cu = StaticJavaParser.parse(source);


        new InstrumentationVisitor().visit(cu, null);

       //ijad file jadid v taqeer esm kelas bray jelogiri az error
        String newClassName = destination.getName().replace(".java", "");
        cu.getClassByName(source.getName().replace(".java", ""))
                .ifPresent(cls -> cls.setName(newClassName));

        try (FileWriter writer = new FileWriter(destination)) {
            writer.write(cu.toString());
        }
    }


    private static class InstrumentationVisitor extends ModifierVisitor<Void> {

        // چیزی که تزریق میکنیم رو میسازه
        private Statement createLogStmt() {
            String id = "Node_" + (++nodeCounter);
            return StaticJavaParser.parseStatement("CoverageTracker.log(\"" + id + "\");");
        }

        // تزریق به ابتدای متدها
        @Override
        public Visitable visit(MethodDeclaration n, Void arg) {
            super.visit(n, arg);
            if (n.getBody().isPresent()) {
                // اضافه کردن لاگ به اولین خط متد
                n.getBody().get().addStatement(0, createLogStmt());
            }
            return n;
        }

        // تزریق به داخل If
        @Override
        public Visitable visit(IfStmt n, Void arg) {
            super.visit(n, arg); // اول فرزندان را ویزیت کن

            // تزریق به بخش Then
            if (n.getThenStmt().isBlockStmt()) {
                // اگر بلوک {} دارد، به اولش اضافه کن
                ((BlockStmt) n.getThenStmt()).addStatement(0, createLogStmt());
            } else {
                // اگر {} ندارد (تک خطی است)، آن را داخل {} بگذار و لاگ را اضافه کن
                BlockStmt newBlock = new BlockStmt();
                newBlock.addStatement(createLogStmt());
                newBlock.addStatement(n.getThenStmt());
                n.setThenStmt(newBlock);
            }

            // تزریق به بخش Else (اگر وجود داشته باشد)
            if (n.getElseStmt().isPresent()) {
                Statement elseStmt = n.getElseStmt().get();
                if (elseStmt.isBlockStmt()) {
                    ((BlockStmt) elseStmt).addStatement(0, createLogStmt());
                } else if (!elseStmt.isIfStmt()) {
                    // نکته: اگر else if باشد، نباید بلاک بسازیم چون ساختار بهم می‌ریزد،
                    // خود IfStmt بعدی در ویزیت بعدی هندل می‌شود.
                    // فقط اگر else خالی باشد بلاک می‌سازیم.
                    BlockStmt newBlock = new BlockStmt();
                    newBlock.addStatement(createLogStmt());
                    newBlock.addStatement(elseStmt);
                    n.setElseStmt(newBlock);
                }
            }
            return n;
        }

        // تزریق به حلقه For
        @Override
        public Visitable visit(ForStmt n, Void arg) {
            super.visit(n, arg);
            if (n.getBody().isBlockStmt()) {
                ((BlockStmt) n.getBody()).addStatement(0, createLogStmt());
            } else {
                BlockStmt newBlock = new BlockStmt();
                newBlock.addStatement(createLogStmt());
                newBlock.addStatement(n.getBody());
                n.setBody(newBlock);
            }
            return n;
        }

        // تزریق به حلقه While
        @Override
        public Visitable visit(WhileStmt n, Void arg) {
            super.visit(n, arg);
            if (n.getBody().isBlockStmt()) {
                ((BlockStmt) n.getBody()).addStatement(0, createLogStmt());
            } else {
                BlockStmt newBlock = new BlockStmt();
                newBlock.addStatement(createLogStmt());
                newBlock.addStatement(n.getBody());
                n.setBody(newBlock);
            }
            return n;
        }
    }
}
