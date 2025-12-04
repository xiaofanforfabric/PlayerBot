# Minecraft 模组编译失败 - 解决方案

## 🔍 失败原因

从错误信息可以看到：

```
Exception in thread "main" java.io.IOException: Downloading from https://services.gradle.org/distributions/gradle-8.4-bin.zip failed: timeout (10000ms)
Caused by: java.net.SocketTimeoutException: Read timed out
```

**问题分析：**
- Minecraft 模组项目需要下载 Gradle 8.4
- 网络超时时间设置为 10 秒（10000ms）
- 由于网络较慢或 Gradle 服务器响应慢，10 秒内无法完成下载

## ✅ 已修复

已更新 `gradle-wrapper.properties`，将超时时间从 10 秒增加到 60 秒：

```properties
networkTimeout=60000  # 从 10000 增加到 60000（60秒）
```

## 🚀 解决方案

### 方案1：重新编译（推荐）

现在可以直接重新运行编译脚本：

```cmd
build-all.bat
```

超时时间已增加到 60 秒，应该可以成功下载。

### 方案2：单独编译 Minecraft 模组

如果还是失败，可以单独编译：

```cmd
cd "java17(1.20.1-1.20.6)\fabric+forge=1.20.1"
gradlew.bat build
```

### 方案3：手动下载 Gradle

如果网络问题持续，可以手动下载：

1. 访问：https://services.gradle.org/distributions/gradle-8.4-bin.zip
2. 下载到：`%USERPROFILE%\.gradle\wrapper\dists\gradle-8.4-bin\<hash>\`
3. 解压到该目录
4. 重新运行编译

### 方案4：使用代理或镜像

如果在中国大陆，可以使用 Gradle 镜像：

1. 编辑 `gradle.properties`，添加：
```properties
systemProp.http.proxyHost=your-proxy-host
systemProp.http.proxyPort=your-proxy-port
```

或使用国内镜像（需要配置 Gradle 镜像源）

### 方案5：检查网络连接

确保：
- 网络连接正常
- 可以访问 `services.gradle.org`
- 防火墙没有阻止下载

## 📊 编译状态

从您的编译结果看：

- ✅ **GUI 客户端**：编译成功
- ✅ **AI 服务器**：编译成功
- ❌ **Minecraft 模组**：网络超时失败

## 🔧 其他可能的问题

### 如果增加超时后仍然失败

1. **检查网络速度**
   - Gradle 8.4 大约 100MB
   - 如果网络很慢，60 秒可能还不够

2. **增加更多超时时间**
   - 编辑 `gradle-wrapper.properties`
   - 将 `networkTimeout=60000` 改为 `networkTimeout=120000`（120秒）

3. **使用已下载的 Gradle**
   - 如果其他项目已经下载了 Gradle，可以复制到模组项目的 Gradle 目录

## 💡 建议

1. **首次编译时**：
   - 确保网络稳定
   - 预留足够时间（首次下载可能需要几分钟）

2. **如果经常失败**：
   - 考虑使用 VPN 或代理
   - 或手动下载 Gradle 分发版

3. **跳过模组编译**：
   - 如果只需要 GUI 和服务器，可以暂时跳过模组编译
   - 两个主要项目（GUI 和服务器）已经编译成功

## 🎯 快速修复

最简单的解决方案：

```cmd
# 重新运行编译（超时时间已增加到 60 秒）
build-all.bat
```

如果还是失败，可以单独编译模组项目，并观察具体的错误信息。

