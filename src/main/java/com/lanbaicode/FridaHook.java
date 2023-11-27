package com.lanbaicode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class FridaHook extends AnAction {
    private static final Map<String, String> ARRAY_TYPE_MAPPING = Map.of(
            "int", "[I",
            "byte", "[B",
            "short", "[S",
            "long", "[J",
            "float", "[F",
            "double", "[D",
            "char", "[C",
            "boolean", "[Z"
    );

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiElement element = (PsiElement) e.getDataContext().getData("psi.Element");
        String fridaSnippet = generateFridaSnippet(element);
        if (fridaSnippet.isEmpty()) {
            showMessage("请选择一个正确的对象");
            return;
        }
        CopyPasteManager.getInstance().setContents(new StringSelection(fridaSnippet));
        showMessage("复制成功");
    }

    private String generateFridaSnippet(PsiElement element) {
        if (element instanceof PsiClass psiClass) {
            return generateClassSnippet(psiClass);
        } else if (element instanceof PsiMethod psiMethod) {
            return generateMethodSnippet(psiMethod);
        } else if (element instanceof PsiField psiField) {
            return generateFieldSnippet(psiField);
        } else {
            return "";
        }
    }

    private String generateClassSnippet(PsiClass psiClass) {
        return String.format(""" 
                let %s = Java.use("%s");""", psiClass.getName(), psiClass.getQualifiedName());
    }

    private String generateMethodSnippet(PsiMethod psiMethod) {
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);
        if (psiClass == null) return "";
        String shortClassName = psiClass.getName();

        String args = Arrays.stream(psiMethod.getParameterList().getParameters())
                .map(PsiParameter::getName)
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse("");

        String logArgs = Arrays.stream(psiMethod.getParameterList().getParameters())
                .map(parameter -> parameter.getName() + "=${" + parameter.getName() + "}")
                .reduce((s1, s2) -> s1 + ", " + s2)
                .map(result -> ": " + result)
                .orElse("");

        //检查是否构造函数
        String methodName = psiMethod.isConstructor() ? "$init" : psiMethod.getName();

        //检查此函数是否被重载, 若重载则需要使用overload(x, y, z), 否则只需要使用单独的implementation
        String overload = "";
        boolean isOverloaded = Arrays.stream(psiMethod.getParent().getChildren())
                .filter(element -> element instanceof PsiMethod)
                .map(element -> (PsiMethod) element)
                .anyMatch(method -> {
                    //方法名称相同, 但是签名不相同, 则存在重载
                    return psiMethod.getName().equals(method.getName()) && !method.getSignature(PsiSubstitutor.EMPTY).equals(psiMethod.getSignature(PsiSubstitutor.EMPTY));
                });

        if (isOverloaded) {
            String argsType = Arrays.stream(psiMethod.getParameterList().getParameters())
                    .map(parameter -> parseArgType(parameter.getType()))
                    .reduce((s1, s2) -> s1 + "', '" + s2)
                    .orElse("");
            overload = String.format(".overload('%s')", argsType);
        }

        if (psiMethod.isConstructor() || Objects.equals(psiMethod.getReturnType(), PsiType.VOID)) {
            // 没有返回值
            return String.format("""
                            %s
                            %s["%s"]%s.implementation = function (%s) {
                                console.log(`%s.%s is called%s`);
                                this["%s"](%s);
                            };
                            """,
                    generateClassSnippet(psiClass),
                    shortClassName, methodName, overload, args,
                    shortClassName, methodName, logArgs,
                    methodName, args
            );
        } else {
            return String.format("""
                            %s
                            %s["%s"]%s.implementation = function (%s) {
                                console.log(`%s.%s is called%s`);
                                let result = this["%s"](%s);
                                console.log(`%s.%s result=${result}`);
                                return result;
                            };
                            """,
                    generateClassSnippet(psiClass),
                    shortClassName, methodName, overload, args,
                    shortClassName, methodName, logArgs,
                    methodName, args,
                    shortClassName, methodName
            );
        }
    }

    private String parseArgType(PsiType type) {
        String CanonicalText = type.getCanonicalText();
        if (type instanceof PsiArrayType arrayType) {
            CanonicalText = ARRAY_TYPE_MAPPING.getOrDefault(arrayType.getCanonicalText(), "");
            //<Object>[] (如String[], Set[]等), 改成: L<package>.<Object>;
            if (arrayType.getComponentType() instanceof PsiClassType classType) {
                CanonicalText = "[L" + Objects.requireNonNull(classType.resolve()).getQualifiedName() + ";";
            }
        } else if (type instanceof PsiClassType classType) {
            //<Object> (如String<>, List<>), 改成: <package>.<Object>
            CanonicalText = Objects.requireNonNull(classType.resolve()).getQualifiedName();
        }
        return CanonicalText;
    }

    private String generateFieldSnippet(PsiField psiField) {
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiField, PsiClass.class);
        if (psiClass != null) {
            return String.format("""
                    %s
                    let %s = %s.%s.value;
                    """, generateClassSnippet(psiClass), psiField.getName(), psiClass.getName(), psiField.getName());
        }
        return "";
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Tip", JOptionPane.INFORMATION_MESSAGE);
    }
}
