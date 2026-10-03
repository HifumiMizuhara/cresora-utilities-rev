# Cresora Utilities

Minecraft **26.2** 的 Fabric 模组开发框架（Java 25 + Gradle）。

这是一个可以直接 `build` 通过的最小可运行骨架：包含主/客户端入口点、Mixin 基础设施、
Minecraft 26.x 的模型与语言文件格式示例，以及一个已经接好线的物品注册示例。

## 版本矩阵

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Minecraft | `26.2` | 2026 年起使用“年份.序号”的新版本号（26.2 于 2026-06-16 发布） |
| Fabric Loader | `0.19.5` | |
| Fabric API | `0.161.0+26.2` | |
| Fabric Loom | `1.17.21` | 26.2 对应 Loom 1.17 系列 |
| Gradle | `9.5.1` | 由 wrapper 自动下载，无需本机安装 |
| Java | `25` | Minecraft 26.2 要求 Java 25 |

所有版本都集中在 `gradle.properties` 中，升级时只改这一个文件。

## 与旧版（1.21.x 及更早）的三个关键差异

1. **不再需要映射（mappings）**。Minecraft 26.x 分发的是未混淆（non-obfuscated）版本，
   代码里直接使用官方类名（如 `net.minecraft.client.Minecraft`、`net.minecraft.resources.Identifier`）。
   Yarn 映射已废弃，旧的 `mappings "net.fabricmc:yarn:..."` 依赖行必须删除。
2. **插件 id 不同**。未混淆版本必须使用 `net.fabricmc.fabric-loom`；
   用于混淆版本、会自动重映射的 `net.fabricmc.fabric-loom-remap` 不能用于 26.x。
3. **必须使用 Java 25**。`build.gradle` 中通过 `options.release = 25` 与 Java toolchain 强制。

## 环境要求

- **JDK 25**。`settings.gradle` 中已经启用 `foojay-resolver-convention`，
  所以如果本机没有 JDK 25，Gradle 会自动下载一个匹配的 JDK，无需手工配置 `JAVA_HOME`。
- **Gradle 不需要单独安装**，仓库自带 wrapper（`gradlew` / `gradlew.bat`）。

本机已安装的 JDK 25 路径：

```
C:\Users\hifum\.jdks\jdk-25.0.4.1+1
```

在 IDE 里导入项目时，把 Gradle 的 JDK 指向它即可（IntelliJ IDEA：
`Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JVM`）。

## 常用命令

```powershell
# 编译并打包（产物在 build/libs/）
.\gradlew.bat build

# 启动带模组的客户端
.\gradlew.bat runClient

# 启动带模组的服务端
.\gradlew.bat runServer

# 查看可用任务
.\gradlew.bat tasks
```

macOS / Linux 下把 `.\gradlew.bat` 换成 `./gradlew`。

## 目录结构

```
build.gradle                              构建脚本（Loom、依赖、Java 25）
settings.gradle                           插件仓库、foojay JDK 自动下载、项目名
gradle.properties                         所有版本号集中在此
gradle/wrapper/                           Gradle wrapper（9.5.1）
src/main/java/com/cresora/utilities/
  CresoraUtilities.java                   公共入口点（ModInitializer），含 MOD_ID / LOGGER / id()
  registry/ModItems.java                  内容注册示例（物品）
  mixin/MinecraftServerMixin.java         公共 Mixin 示例
src/client/java/com/cresora/utilities/client/
  CresoraUtilitiesClient.java             客户端入口点（ClientModInitializer）
  mixin/MinecraftClientMixin.java         仅客户端 Mixin 示例
src/main/resources/
  fabric.mod.json                         模组元数据（入口点、依赖、Mixin 配置）
  cresora-utilities.mixins.json           公共 Mixin 配置
  assets/cresora-utilities/               图标、物品模型、纹理、语言文件
src/client/resources/
  cresora-utilities.client.mixins.json    仅客户端 Mixin 配置
```

`loom { splitEnvironmentSourceSets() }` 使 `main` 与 `client` 成为两个独立源码集：
放在 `src/client` 下的代码不会被加载进专用服务端，可以安全地引用客户端专属类。

## 添加新内容

1. 在 `src/main/java/com/cresora/utilities/registry/` 下新建 `ModBlocks`、`ModEntities` 等注册类，
   照抄 `ModItems` 的写法，并在 `CresoraUtilities#onInitialize` 中调用其 `initialize()`。
2. 用 `CresoraUtilities.id("path")` 生成命名空间为自己模组的 `Identifier`，不要手写字符串。
3. 物品模型需要三个文件（26.x 的格式，与 1.21.4+ 一致）：
   - `assets/cresora-utilities/items/<id>.json` —— 物品模型定义，指向真正的模型
   - `assets/cresora-utilities/models/item/<id>.json` —— `parent: minecraft:item/generated` 与 `layer0` 纹理
   - `assets/cresora-utilities/textures/item/<id>.png` —— 16×16 纹理
4. 语言键使用 `item.cresora-utilities.<id>` 的形式写在 `assets/cresora-utilities/lang/en_us.json`。

## 修改模组标识

如果要把模组改名，需要同步修改这几处：

- `gradle.properties` 中的 `archives_base_name` 与 `group`
- `settings.gradle` 中的 `rootProject.name`
- `build.gradle` 中 `loom { mods { ... } }` 的字符串
- `fabric.mod.json` 中的 `id` / `name` / 入口点类名 / Mixin 配置名
- Java 包名与 `CresoraUtilities.MOD_ID`
- `assets/cresora-utilities/` 目录名与两个 Mixin 配置文件名

## 关于版权

`fabric.mod.json` 中的 `license` 目前是 `All-Rights-Reserved`，仓库内没有 `LICENSE` 文件。
确定要开源时请补上许可证并把该字段改成对应标识（例如 `MIT`）。
