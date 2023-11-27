<div align="center">
    <h1>HookCodeGenerator</h1>
    <h5 style="margin-top: -5px;">HookCodeGenerator Plugin for IntelliJ IDEA.</h5>

<a href="https://github.com/LanBaiCode/HookCodeGenerator/actions/workflows/build.yml"><img src="https://github.com/LanBaiCode/HookCodeGenerator/actions/workflows/build.yml/badge.svg"></a>
<a href="https://plugins.jetbrains.com/plugin/23194-hook-code-generator/"><img src="https://img.shields.io/jetbrains/plugin/v/23194-hook-code-generator.svg?style=flat-square"></a>
<a href="https://plugins.jetbrains.com/plugin/23194-hook-code-generator/"><img src="https://img.shields.io/jetbrains/plugin/d/23194-hook-code-generator.svg?style=flat-square"></a>

</div>

<br>

# 描述
- 这是一个 **IntelliJ IDEA 插件**, 它可以快捷的生成 Xposed 和 Frida 的 Hook 代码
- 支持类名、函数、字段
- 生成的内容自动保存至剪贴板
# 安装
- 你可以通过 [Jetbrains Marketplace](https://plugins.jetbrains.com/plugin/23194-hook-code-generator)安装此插件
- 也可通过这里[下载](https://github.com/LanBaiCode/HookCodeGenerator/releases)并选择磁盘安装


# 使用
安装完成后, 将光标停留在需要 Hook 的位置, 右键选择 `XposedCode` 或者 `FridaCode`, 即可自动生成对应的 Hook 代码

- https://github.com/LanBaiCode/HookCodeGenerator/assets/72244576/d1c67a66-10ae-4cce-b5eb-eeab08abc0f9

- https://github.com/LanBaiCode/HookCodeGenerator/assets/72244576/bc508844-a047-4cdf-b07a-70389c9be364

# 项目编译
- JVM设置: Preferences -> Build, Execution, Deployment -> Gradle JVM (设置为17)
- 运行IDE: Gradle -> Tasks -> intellij -> runIde
- 编译插件: Gradle -> Tasks -> intellij -> buildPlugin

# Credits
- [jadx](https://github.com/skylot/jadx)

# License
[MIT License](LICENSE)
