# iTXTech Soyuz - `Союз`

为 [Mirai Console](https://github.com/mamoe/mirai) 提供可扩展、轻量级的`Websocket API` 服务。

关于`开发`，`内置Handler`等，请 [查看文档](docs/README.md)。

## 特性

可扩展的架构，任何插件都可以在 `iTXTech Soyuz` 中注册 `Handler` 实现功能。

可通过 `Websocket` 客户端：

* 接入 `Mirai Console Terminal` 实时查看终端输出
* 接入 `Mirai Console` 执行命令，监控运行状态
* 接入 `Mirai Console Loader` 进行包管理 - 请安装 [MCL Addon](https://github.com/iTXTech/mcl-addon)

即将到来：

* 接入 `Mirai Web Panel` 使用 `WebUI` 管理 `Mirai Console`

## `/soyuz` 命令

```
/soyuz disconnect <连接ID>   # 断开指定连接
/soyuz disconnectAll         # 断开所有连接
/soyuz listConnections       # 列出所有连接
/soyuz listHandlers          # 列出所有Handler
/soyuz token [token]         # 设置或重新生成 Access Token
```

## 安装

1. 使用MCL命令行

```
./mcl --update-package org.itxtech:soyuz
```

2. 从 [Release](https://github.com/iTXTech/soyuz/releases) 下载

## 开源许可证

    iTXTech Soyuz
    Copyright (C) 2022 iTX Technologies

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
