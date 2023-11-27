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
import java.util.stream.Collectors;

public class XposedHook extends AnAction {
    private static final Map<String, String> PRIMITIVE_TYPE_MAPPING = Map.of(
            "int", "Int",
            "byte", "Byte",
            "short", "Short",
            "long", "Long",
            "float", "Float",
            "double", "Double",
            "char", "Char",
            "boolean", "Boolean"
    );

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiElement element = (PsiElement) e.getDataContext().getData("psi.Element");
        String xposedSnippet = generateXposedSnippet(element);
        if (xposedSnippet.isEmpty()) {
            showMessage("请选择一个正确的对象");
            return;
        }
        CopyPasteManager.getInstance().setContents(new StringSelection(xposedSnippet));
        showMessage("复制成功");
    }

    private String generateXposedSnippet(PsiElement element) {
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

    private String generateMethodSnippet(PsiMethod psiMethod) {
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);
        if (psiClass == null) return "";

        String argsType = "";
        String methodName = "";
        String xposedMethod;
        //检查是否构造函数, 构造函数不需要函数名称
        if (psiMethod.isConstructor()) {
            xposedMethod = "findAndHookConstructor";
        } else {
            xposedMethod = "findAndHookMethod";
            methodName = "\"" + psiMethod.getName() + "\", ";
        }
        if (psiMethod.hasParameters()) {
            argsType = Arrays.stream(psiMethod.getParameterList().getParameters())
                    .map(parameter -> parseArgType(parameter.getType()) + ".class, ")
                    .collect(Collectors.joining());
        }

        return String.format("""
                            XposedHelpers.%s("%s", classLoader, %snew XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    super.beforeHookedMethod(param);
                                }
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                }
                            });
                """, xposedMethod, psiClass.getQualifiedName(), methodName + argsType);
    }

    private String parseArgType(PsiType type) {
        String CanonicalText = type.getCanonicalText();
        //只处理省略类型还有泛型
        if (type instanceof PsiEllipsisType ellipsisType) {
            if (ellipsisType.getComponentType() instanceof PsiClassType classType) {
                CanonicalText = Objects.requireNonNull(classType.resolve()).getQualifiedName() + "[]";
            }
        } else if (type instanceof PsiClassType classType) {
            CanonicalText = Objects.requireNonNull(classType.resolve()).getQualifiedName();
        }
        return CanonicalText;
    }

    private String generateFieldSnippet(PsiField psiField) {
        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiField, PsiClass.class);
        if (psiClass == null) return "";

        String isStatic = psiField.hasModifierProperty(PsiModifier.STATIC) ? "Static" : "";
        String type = PRIMITIVE_TYPE_MAPPING.getOrDefault(psiField.getType().getPresentableText(), "Object");
        String xposedMethod = "XposedHelpers.get" + isStatic + type + "Field";

        return String.format("%s(/*runtimeObject*/, \"%s\");", xposedMethod, psiField.getName());
    }

    private String generateClassSnippet(PsiClass psiClass) {
        return String.format("""
                Class<?> %sClass = XposedHelpers.findClass("%s", classLoader);
                 """, psiClass.getName(), psiClass.getQualifiedName());
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Tip", JOptionPane.INFORMATION_MESSAGE);
    }
}
