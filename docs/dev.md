# 基于 `iTXTech Soyuz` 开发

## 引入插件

### `build.gradle.kts`

添加依赖 `implementation("org.itxtech:soyuz:1.0.0-beta.1")`，打包时不加入依赖

### 插件代码

```kotlin
object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "com.example.example",
        name = "example",
        version = "0.1.0"
    ) {
        // 如果基于 iTXTech Soyuz 的功能为可选功能，则可以选择性依赖，通过第二个参数控制
        dependsOn("org.itxtech.soyuz", true)
    }
) { ... }
```

如为可选依赖，插件加载后检测`iTXTech Soyuz`是否存在

```kotlin
try {
    Class.forName("org.itxtech.soyuz.Soyuz")
    // Your logic with iTXTech Soyuz
} catch (e: Exception) {
    PluginMain.logger.warning("iTXTech Soyuz 未安装")
}
```

## 注册 `Soyuz Handler`

### 请求格式

`key` 用于匹配 `Handler`，其他字段均可自定义。

```json
Websocket Request ->
{
  "key": "do-something",
  "something": "father"
}

Websocket Response <-
{
  "key": "do-something",
  "msg": "hi, father"
}
```

### `Exmaple Handler`

```kotlin
import kotlinx.serialization.Serializable
import org.itxtech.soyuz.Soyuz
import org.itxtech.soyuz.SoyuzWebSocketSession
import org.itxtech.soyuz.handler.HandlerManager
import org.itxtech.soyuz.handler.SoyuzHandler
import org.itxtech.soyuz.ReplyMessage

class ExampleHandler : SoyuzHandler("do-something") {
    @Serializable
    data class Payload(
        val something: String
    )

    override suspend fun handle(session: SoyuzWebSocketSession, data: String) {
        // 解码数据
        val payload = Soyuz.json.decodeFromString(Payload.serializer(), data)
        // 使用 ReplyMessage 可返回简单回复
        session.sendText(ReplyMessage(key, "hi, ${payload.something}").toJson())
    }
}
```

### 注册 `Handler`

`Handler Key` 不可重复，若与现有的重复则将抛出错误。

```kotlin
import org.itxtech.soyuz.handler.HandlerManager

HandlerManager.register(ExampleHandler())
```
