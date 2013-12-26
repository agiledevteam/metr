import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Test;

import com.lge.metr.JavaLexer;
import com.lge.metr.JavaParser;
import com.lge.metr.JavaParser.ClassBodyDeclarationContext;
import com.lge.metr.JavaParser.ClassDeclarationContext;
import com.lge.metr.JavaParser.CompilationUnitContext;
import com.lge.metr.JavaParser.MethodDeclarationContext;
import com.lge.metr.JavaParser.TypeDeclarationContext;

public class JavaParserTest {
    @Test
    public void runParser() throws FileNotFoundException, IOException {
        String name = "src/test/resources/AllGrammar.java";
        CompilationUnitContext ctx = compile(name);
        Assert.assertEquals("sample.input", ctx.packageDeclaration().qualifiedName().getText());
        List<TypeDeclarationContext> typeDeclarations = ctx.typeDeclaration();
        Assert.assertEquals(1, typeDeclarations.size());
        Assert.assertEquals("AllGrammar", typeDeclarations.get(0).classDeclaration().Identifier().getText());
        ClassDeclarationContext declaration = typeDeclarations.get(0).classDeclaration();
        List<ClassBodyDeclarationContext> bodyDeclarations = declaration.classBody().classBodyDeclaration();
        List<String> methods = new ArrayList<String>();
        for (ClassBodyDeclarationContext bodyDecl : bodyDeclarations) {
            MethodDeclarationContext methodDeclaration = bodyDecl.memberDeclaration().methodDeclaration();
            if (methodDeclaration != null) {
                methods.add(methodDeclaration.Identifier().getText());
            }
        }
        Assert.assertEquals("allStatements", methods.get(0));
        Assert.assertEquals("someExpression", methods.get(1));
        Assert.assertEquals("someStatement", methods.get(2));
    }

    private CompilationUnitContext compile(String name) throws IOException,
            FileNotFoundException {
        CharStream input = new ANTLRInputStream(new FileInputStream(name));
        JavaLexer source = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(source);
        JavaParser p = new JavaParser(tokens);
        CompilationUnitContext ctx = p.compilationUnit();
        return ctx;
    }
}
