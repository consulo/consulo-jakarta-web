package consulo.jsp.impl.language.psi.java.psi;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.util.CachedValueProvider;
import consulo.document.util.TextRange;
import consulo.jsp.language.JspLanguage;
import consulo.jsp.language.psi.JspDirectiveKind;
import consulo.jsp.language.psi.JspFile;
import consulo.jsp.language.psi.java.JspxImportList;
import consulo.jsp.language.psi.xml.JspDirective;
import consulo.language.ast.TokenType;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlAttributeValue;
import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 24-May-17
 */
public class JspxImportListImpl extends LightElement implements JspxImportList {
    private JspJavaFileImpl myJspJavaFile;

    public JspxImportListImpl(JspJavaFileImpl jspJavaFile) {
        super(jspJavaFile.getManager(), JavaLanguage.INSTANCE);
        myJspJavaFile = jspJavaFile;
    }

    @Override
    @RequiredWriteAction
    public PsiElement add(@Nonnull PsiElement element) throws IncorrectOperationException {
        if (element instanceof PsiImportStatement) {
            JspFile jspFile = (JspFile) myJspJavaFile.getViewProvider().getPsi(JspLanguage.INSTANCE);

            String text = "<%@ page import=\"" + ((PsiImportStatement) element).getQualifiedName() + "\"%>";

            JspFile dummyJsp = (JspFile) PsiFileFactory.getInstance(element.getProject()).createFileFromText("dummy.jsp", JspLanguage.INSTANCE, text);

            JspDirective dummyJspDirective = (JspDirective) dummyJsp.getDocument().getRootTag().getSubTags()[0];

            XmlTag[] directiveTags = jspFile.getDirectiveTags(JspDirectiveKind.PAGE, false);
            if (directiveTags.length == 0) {
                XmlTag rootTag = jspFile.getRootTag();
                PsiElement result = rootTag.addBefore(dummyJspDirective, rootTag.getFirstChild());

                result.getNode().addLeaf(TokenType.WHITE_SPACE, "\n", null);
                return this;
            }
            else {
                XmlTag lastTag = directiveTags[directiveTags.length - 1];
                XmlTag parentTag = lastTag.getParentTag();

                PsiElement result = parentTag.addAfter(dummyJspDirective, lastTag);

                parentTag.getNode().addLeaf(TokenType.WHITE_SPACE, "\n", result.getNode());
                return this;
            }
        }

        throw new UnsupportedOperationException(element.getClass().getName());
    }

    @Override
    public PsiElement replace(@Nonnull PsiElement newElement) throws IncorrectOperationException {
        return this;
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof JavaElementVisitor) {
            ((JavaElementVisitor) visitor).visitImportList(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @RequiredReadAction
    @Nonnull
    @Override
    public PsiElement[] getChildren() {
        return getImportStatements();
    }

    @Nonnull
    @Override
    public PsiImportStatement[] getImportStatements() {
        return LanguageCachedValueUtil.getCachedValue(myJspJavaFile, () -> CachedValueProvider.Result.create(calcImportStatements(), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @Nonnull
    @RequiredReadAction
    private PsiImportStatement[] calcImportStatements() {
        FileViewProvider viewProvider = myJspJavaFile.getContainingFile().getViewProvider();

        JspFile psi = (JspFile) viewProvider.getPsi(JspLanguage.INSTANCE);

        XmlTag[] directiveTags = psi.getDirectiveTags(JspDirectiveKind.PAGE, false);

        List<PsiImportStatement> list = new ArrayList<>();

        for (XmlTag directiveTag : directiveTags) {
            XmlAttribute xmlAttribute = directiveTag.getAttribute("import");
            if (xmlAttribute == null) {
                continue;
            }

            XmlAttributeValue valueElement = xmlAttribute.getValueElement();
            if (valueElement == null) {
                continue;
            }

            int startOffset = valueElement.getTextRange().getStartOffset();

            List<Pair<String, TextRange>> importList = parseImportList2(valueElement.getValue());

            for (Pair<String, TextRange> pair : importList) {
                String first = pair.getFirst();
                TextRange second = pair.getSecond();

                boolean error = StringUtil.isEmptyOrSpaces(first);

                // skip start delimiter
                TextRange textRange = new TextRange(second.getStartOffset() + startOffset + 1, second.getEndOffset() + startOffset + 1);

                list.add(new JspxImportStatementImpl(first.trim(), textRange, myJspJavaFile, error));
            }

        }
        return list.toArray(PsiImportStatement.EMPTY_ARRAY);
    }

    private static List<Pair<String, TextRange>> parseImportList2(@Nonnull String string) {
        List<Pair<String, TextRange>> list = new ArrayList<>();

        int startOffset = 0;
        while (true) {
            int i = string.indexOf(',', startOffset);
            if (i == -1) {
                break;
            }

            String text = string.substring(startOffset, i);

            list.add(Pair.create(text, new TextRange(startOffset, i + 1)));

            // +1 comma
            startOffset = i + 2;
        }

        if (startOffset != string.length()) {
            list.add(Pair.create(string.substring(startOffset, string.length()), new TextRange(startOffset, string.length())));
        }
        return list;
    }

    @Nonnull
    @Override
    public PsiImportStaticStatement[] getImportStaticStatements() {
        return PsiImportStaticStatement.EMPTY_ARRAY;
    }

    @Override
    public PsiImportModuleStatement[] getImportModuleStatements() {
        return PsiImportModuleStatement.EMPTY_ARRAY;
    }

    @Nonnull
    @Override
    public PsiImportStatementBase[] getAllImportStatements() {
        return getImportStatements();
    }

    @Nullable
    @Override
    public PsiImportStatement findSingleClassImportStatement(String qName) {
        return null;
    }

    @Nullable
    @Override
    public PsiImportStatement findOnDemandImportStatement(@NonNls String packageName) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable PsiImportModuleStatement findImportModuleStatement(String s) {
        return null;
    }

    @Nullable
    @Override
    public PsiImportStatementBase findSingleImportStatement(String name) {
        return null;
    }

    @Override
    public boolean isReplaceEquivalent(PsiImportList otherList) {
        return false;
    }

    @Override
    public String toString() {
        return "JspxImportListImpl";
    }
}
