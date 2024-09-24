package com.pan.io.findusages.data;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.*;
import java.nio.file.Paths;

public class MxCell implements Serializable {

    static {
    }

    @Serial
    private static final long serialVersionUID = -1634887455836192526L;


    private transient PsiElement element;
    private String filePath;

    private int psiOffset;

    public MxCell(PsiElement element) {
        this.element = element;
        this.filePath = element.getContainingFile().getVirtualFile().getPath();
        this.psiOffset = element.getTextOffset();
    }

    public PsiElement getElement() {
        return element;
    }

    @Override
    public String toString() {
        String text = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            if (element instanceof PsiMethod psiMethod) {
                PsiClass psiClass = psiMethod.getContainingClass();
                return psiClass != null ? psiClass.getName() + "." + psiMethod.getName() : psiMethod.getName();
            }

            return element.getText();
        });
        if (text != null && text.length() > 30) {
            return text.substring(0, 30) + "...";
        }
        return text;
    }

    static final float per = 0.5f;
    static final float per2 = 0.2f;

    public static int modifyWidth(String parent, String current, int defaultWidth) {
        if (parent == null && current == null) {
            return defaultWidth;
        }

        if (parent != null && current != null) {
            return Math.max(defaultWidth, (int) (parent.length() * per2 + current.length() * per2));
        }
        int width = defaultWidth;
        if (parent != null) {
            width = (int) Math.max(width, parent.length() * per);
        }
        if (current != null) {
            width = (int) Math.max(width, current.length() * per);
        }
        return width;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MxCell mxCell = (MxCell) o;

        return new EqualsBuilder().append(element, mxCell.element).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(element).toHashCode();
    }


    // Custom serialization
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(filePath);
        out.writeInt(psiOffset);
    }

    // Custom deserialization
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        filePath = in.readUTF();
        psiOffset = in.readInt();

        element = decryptData(filePath, psiOffset);
    }

    private PsiElement decryptData(String filePath, int psiOffset) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        PsiFile psiFile = findPsiFileByPath(MxProject.project, filePath);
        if (psiFile == null) {
            return null;
        }
        return findPsiElementByOffset(psiFile, psiOffset);
    }


    public static VirtualFile getVirtualFileByPath(String virtualFilePath) {
        return VirtualFileManager.getInstance().findFileByNioPath(Paths.get(virtualFilePath));
    }

    public static PsiFile findPsiFileByPath(Project project, String filePath) {
        VirtualFile virtualFile = getVirtualFileByPath(filePath);
        if (virtualFile != null) {
            return PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }

    public static PsiElement findPsiElementByOffset(PsiFile psiFile, int offset) {
        return psiFile.findElementAt(offset);
    }

}
