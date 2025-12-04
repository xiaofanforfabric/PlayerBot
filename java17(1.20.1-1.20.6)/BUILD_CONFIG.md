# 构建配置说明

## build-all.bat 更新

### 新增功能
1. **AllCommon 模块编译** - 在编译 Minecraft 模组之前先编译 AllCommon 模块
2. **构建顺序保证** - 确保 AllCommon 模块在模组编译前完成构建
3. **输出信息更新** - 显示 AllCommon 模块的编译输出位置

### 编译顺序
1. GUI 客户端
2. AI 服务器
3. **AllCommon 模块**（新增）
4. Minecraft 模组（1.20.1）

## Gradle 构建配置

### settings.gradle
- 使用 `includeBuild("../allcommon")` 引入 AllCommon 模块

### common/build.gradle
- 添加了 `implementation("com.xiaofan:playerbot-allcommon:1.0.0")` 依赖
- 添加了 `compileJava` 任务依赖，确保 AllCommon 在编译前被构建

### 构建流程
1. 执行 `build-all.bat`
2. 脚本先编译 AllCommon 模块（通过 `:allcommon:build`）
3. 然后编译 Minecraft 模组
4. Gradle 自动处理 AllCommon 的依赖关系
5. AllCommon 的类会被打包到最终的模组 JAR 中

## 注意事项

1. **includeBuild 机制** - AllCommon 通过 `includeBuild` 引入，不是直接子项目
2. **依赖解析** - Gradle 会自动解析 AllCommon 的依赖并打包到最终 JAR
3. **构建顺序** - 通过任务依赖确保 AllCommon 在编译前被构建
4. **Shadow JAR** - Fabric/Forge/Quilt 平台使用 Shadow 插件打包依赖

## 验证构建

运行 `build-all.bat` 后，检查以下文件：
- `allcommon/build/libs/playerbot-allcommon-1.0.0.jar`
- `fabric+forge=1.20.1/fabric/build/libs/*.jar`
- `fabric+forge=1.20.1/forge/build/libs/*.jar`
- `fabric+forge=1.20.1/quilt/build/libs/*.jar`

所有 JAR 文件应包含 AllCommon 模块的类。

