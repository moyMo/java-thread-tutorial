# JDK 11 运行环境搭建指南

## 当前状态

- 当前系统Java版本: JDK 1.8.0_402 (Amazon Corretto 8)
- 项目要求Java版本: JDK 11
- 已完成的配置:
  1. 更新了 `pom.xml` 添加了Maven工具链插件支持
  2. 创建了 `~/.m2/toolchains.xml` 工具链配置文件
  3. 创建了 `setup_jdk11.sh` 环境设置脚本

## 安装JDK 11

### 方法1: 使用Homebrew安装（推荐）

如果您已经开始了Homebrew安装但需要输入密码，请完成密码输入。

如果安装被中断，可以运行:
```bash
brew install --cask temurin@11
```

### 方法2: 手动下载安装

1. 访问 [Temurin JDK 11 下载页面](https://adoptium.net/temurin/releases/?version=11)
2. 下载 macOS 版本 (.pkg 文件)
3. 双击下载的 .pkg 文件并按照提示安装

### 方法3: 使用提供的脚本

运行环境设置脚本:
```bash
./setup_jdk11.sh
```

脚本会引导您完成安装和配置过程。

## 配置环境

### 临时使用JDK 11

运行以下命令设置临时环境:
```bash
source .javaenv
```

### 永久配置

将以下内容添加到 `~/.zshrc` 或 `~/.bash_profile`:
```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

## 验证安装

1. 检查Java版本:
```bash
java -version
```
应该显示类似 `openjdk version "11.0.x"` 的信息。

2. 检查JAVA_HOME:
```bash
echo $JAVA_HOME
```

3. 测试Maven编译:
```bash
mvn clean compile
```

## 项目配置说明

### Maven工具链

项目已配置为使用Maven工具链插件，这意味着:
- 即使系统默认是JDK 8，Maven也会使用JDK 11编译项目
- 工具链配置在 `~/.m2/toolchains.xml`
- 项目 `pom.xml` 已添加工具链插件

### 编译项目

使用以下命令编译项目:
```bash
mvn clean compile
```

如果遇到工具链错误，请确保 `~/.m2/toolchains.xml` 中的 `jdkHome` 路径正确指向JDK 11安装目录。

## 故障排除

### 1. 工具链配置错误

如果Maven报告工具链错误，检查:
```bash
cat ~/.m2/toolchains.xml
```

确保 `jdkHome` 路径正确。典型路径:
- Temurin: `/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home`
- AdoptOpenJDK: `/Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home`
- Amazon Corretto: `/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home`

### 2. Java版本未切换

如果 `java -version` 仍然显示JDK 8:
- 确保已运行 `source .javaenv`
- 或已正确设置 `JAVA_HOME` 环境变量

### 3. Maven仍然使用JDK 8编译

检查Maven使用的Java版本:
```bash
mvn -v
```

如果显示JDK 8，请确保:
1. 工具链配置正确
2. 重新启动终端
3. 清除Maven缓存: `mvn clean`

## 快速开始

1. 安装JDK 11 (如果尚未安装)
2. 运行环境设置:
```bash
./setup_jdk11.sh
```
3. 设置环境变量:
```bash
source .javaenv
```
4. 编译项目:
```bash
mvn clean compile
```

## 支持

如果遇到问题，请检查:
- JDK 11是否正确安装
- 环境变量设置是否正确
- Maven工具链配置是否指向正确的JDK路径

项目现在已配置为支持JDK 11编译，即使系统默认是JDK 8。
