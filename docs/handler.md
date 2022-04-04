# 内置 `Handler`

约定，请求和回复的`key`一致。

## `Run Command`

### 请求

```json
{
  "key": "soyuz-run-command",
  "command": "/status"
  // 执行的命令，参数用空格隔开
}
```

### 回复

```json
{
  "key": "soyuz-run-command",
  "result": "Success",
  // 命令执行结果，为 CommandExecuteResult 的子类类名
  "output": [
    "Line1",
    "Line2"
  ]
  // 命令执行输出，若命令不适用 `sendMessage` 输出则为空
}
```

## `List Handler`

### 请求

```json
{
  "key": "soyuz-list-handler"
}
```

### 回复

```json
{
  "key": "soyuz-list-handler",
  "list": [
    // 返回已注册的 Handler，name为 Handler key，cls为注册的类
    {
      "name": "soyuz-push-log",
      "cls": "org.itxtech.soyuz.handler.builtin.PushLogHandler"
    },
    {
      "name": "soyuz-list-handler",
      "cls": "org.itxtech.soyuz.handler.builtin.ListHandlerHandler"
    }
  ]
}
```

## `List Plugin`

### 请求

```json
{
  "key": "soyuz-list-plugin"
}
```

### 回复

`info`, `author` 字段可能为空。

```json
{
  "key": "soyuz-list-plugin",
  "list": [
    {
      "name": "Soyuz",
      "version": "1.0.0-beta.1",
      "info": "The Websocket API Server for Mirai Console",
      "author": "PeratX"
    }
  ]
}
```

## `Mirai Info`

### 请求

```json
{
  "key": "soyuz-mirai-info"
}
```

### 回复

```json
{
  "key": "soyuz-mirai-info",
  "version": "2.11.0-M1",
  "buildDate": 1648899229
}
```

## `Push Log`

用于启用或关闭`Terminal`输出推送。

### 请求

```json
{
  "key": "soyuz-push-log",
  "enable": true
}
```

### 回复

```json
{
  "key": "soyuz-push-log",
  "msg": "成功为success，失败为原因，若成功，之后此字段为输出的Log"
}
```
