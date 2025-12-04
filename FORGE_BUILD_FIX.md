# Forge 编译错误修复说明

## 🔍 错误原因

**错误信息：**
```
Could not get unknown property 'fg' for object of type org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler.
```

**问题分析：**
- 项目使用了 `fg.deobf()` 这是**旧的 Forge Gradle 插件**的 API
- 但项目实际使用的是 **Architectury Loom**，它不使用 `fg` API
- Architectury Loom 使用 `modImplementation` 或 `modApi` 来处理模组依赖

## ✅ 已修复

已将 Forge 项目的依赖声明从：
```gradle
implementation fg.deobf(files("../lib/baritone-api-forge-1.10.3.jar"))
```

改为：
```gradle
modImplementation files("../lib/baritone-api-forge-1.10.3.jar")
```

## 📝 Architectury Loom vs Forge Gradle

### 旧方式（Forge Gradle）
```gradle
// 需要应用 Forge Gradle 插件
apply plugin: 'net.minecraftforge.gradle'

dependencies {
    implementation fg.deobf(files("lib.jar"))  // 使用 fg.deobf()
}
```

### 新方式（Architectury Loom）
```gradle
// 使用 Architectury Loom
plugins {
    id "dev.architectury.loom"
}

dependencies {
    modImplementation files("lib.jar")  // 使用 modImplementation
    // 或
    modApi files("lib.jar")  // 使用 modApi
}
```

## 🚀 现在可以重新编译

```cmd
build-all.bat
```

Forge 项目现在应该可以正常编译了！

## 📚 相关文档

- [Architectury Loom 文档](https://docs.architectury.dev/)
- [Forge 模组开发指南](https://docs.minecraftforge.net/)

