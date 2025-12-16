#!/bin/bash

# JDK 11 环境设置脚本
echo "=== JDK 11 环境设置脚本 ==="

# 检查是否已安装JDK 11
check_jdk11() {
    echo "检查JDK 11安装状态..."
    
    # 方法1: 检查java_home
    if /usr/libexec/java_home -v 11 2>/dev/null; then
        echo "✓ JDK 11 已通过java_home检测到"
        return 0
    fi
    
    # 方法2: 检查标准安装目录
    if [ -d "/Library/Java/JavaVirtualMachines/temurin-11.jdk" ]; then
        echo "✓ JDK 11 (Temurin) 已安装在标准位置"
        return 0
    fi
    
    if [ -d "/Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk" ]; then
        echo "✓ JDK 11 (AdoptOpenJDK) 已安装在标准位置"
        return 0
    fi
    
    if [ -d "/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk" ]; then
        echo "✓ JDK 11 (Amazon Corretto) 已安装在标准位置"
        return 0
    fi
    
    echo "✗ 未检测到JDK 11安装"
    return 1
}

# 安装JDK 11
install_jdk11() {
    echo "正在安装JDK 11..."
    echo "请选择安装方法："
    echo "1. 使用Homebrew安装Temurin JDK 11 (推荐)"
    echo "2. 手动下载安装"
    echo "3. 跳过安装，仅配置环境"
    
    read -p "请输入选择 (1-3): " choice
    
    case $choice in
        1)
            echo "使用Homebrew安装Temurin JDK 11..."
            brew install --cask temurin@11
            ;;
        2)
            echo "请手动下载并安装JDK 11："
            echo "1. 访问 https://adoptium.net/temurin/releases/?version=11"
            echo "2. 下载 macOS 版本"
            echo "3. 双击 .pkg 文件安装"
            ;;
        3)
            echo "跳过安装..."
            ;;
        *)
            echo "无效选择，跳过安装"
            ;;
    esac
}

# 配置环境
configure_environment() {
    echo "配置Java环境..."
    
    # 查找JDK 11
    local jdk11_path=""
    
    # 尝试多种可能的位置
    possible_paths=(
        "/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home"
        "/Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home"
        "/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home"
        "/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home"
        "$(brew --prefix)/opt/openjdk@11"
    )
    
    for path in "${possible_paths[@]}"; do
        if [ -d "$path" ]; then
            jdk11_path="$path"
            echo "找到JDK 11: $path"
            break
        fi
    done
    
    if [ -n "$jdk11_path" ]; then
        # 创建环境变量设置脚本
        cat > .javaenv << EOF
# Java 11 环境设置
export JAVA_HOME="$jdk11_path"
export PATH="\$JAVA_HOME/bin:\$PATH"
echo "已设置 JAVA_HOME=\$JAVA_HOME"
java -version
EOF
        
        echo "已创建环境设置文件: .javaenv"
        echo "使用以下命令设置环境:"
        echo "  source .javaenv"
        
        # 更新Maven工具链配置
        update_toolchains "$jdk11_path"
    else
        echo "未找到JDK 11，请确保已正确安装"
    fi
}

# 更新Maven工具链配置
update_toolchains() {
    local jdk_home="$1"
    local toolchains_file="$HOME/.m2/toolchains.xml"
    
    echo "更新Maven工具链配置..."
    
    # 如果工具链文件不存在，创建它
    if [ ! -f "$toolchains_file" ]; then
        mkdir -p "$(dirname "$toolchains_file")"
        cat > "$toolchains_file" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>11</version>
      <vendor>temurin</vendor>
    </provides>
    <configuration>
      <jdkHome>$jdk_home</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
EOF
        echo "已创建Maven工具链配置: $toolchains_file"
    else
        echo "Maven工具链配置已存在: $toolchains_file"
        echo "请确保其中的jdkHome指向: $jdk_home"
    fi
}

# 验证安装
verify_installation() {
    echo "验证Java安装..."
    
    if command -v java &> /dev/null; then
        echo "Java版本:"
        java -version
    else
        echo "Java未在PATH中找到"
    fi
    
    if [ -n "$JAVA_HOME" ]; then
        echo "JAVA_HOME: $JAVA_HOME"
    fi
    
    echo "Maven工具链配置:"
    if [ -f "$HOME/.m2/toolchains.xml" ]; then
        cat "$HOME/.m2/toolchains.xml"
    else
        echo "未找到工具链配置"
    fi
}

# 主函数
main() {
    echo "当前Java版本:"
    java -version 2>&1 | head -3
    
    if check_jdk11; then
        echo "JDK 11 已安装"
    else
        echo "JDK 11 未安装"
        install_jdk11
    fi
    
    configure_environment
    verify_installation
    
    echo ""
    echo "=== 设置完成 ==="
    echo "要使用JDK 11编译项目，请运行:"
    echo "  mvn clean compile"
    echo ""
    echo "如果需要临时使用JDK 11，运行:"
    echo "  source .javaenv"
}

# 执行主函数
main
