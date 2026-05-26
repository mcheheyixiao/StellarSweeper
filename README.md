# StellarSweeper

English README: [README_EN.md](README_EN.md)

StellarSweeper 是一个基于 Architectury 的多平台 Minecraft 服务端掉落物清理模组。

仓库模块：
- `common`
- `fabric`
- `neoforge`

目标版本：
- `Minecraft 1.21.1`（Mojmap）

## 功能概览

- 定时自动清理掉落物。
- 阈值检测与确认机制。
- 以在线玩家为中心进行范围扫描（水平半径 + Y 轴范围）。
- 玩家范围重叠时，对同一 `ItemEntity` 去重统计与去重清理。
- 支持手动清理、预览扫描。
- 支持多清理列表（`cleanupLists` + `currentCleanupList`）。
- 根据 `doEntityDrops` 按世界跳过清理并记录报告。
- 使用标准语言文件：
  - `assets/stellarsweeper/lang/zh_cn.json`
  - `assets/stellarsweeper/lang/en_us.json`

## 命令

根命令：
- `/sweep`

所有子命令统一要求 `OP2` 权限：

- `/sweep`
- `/sweep help`
- `/sweep run`
- `/sweep preview`
- `/sweep reload`
- `/sweep save`
- `/sweep toggle auto`
- `/sweep toggle threshold`
- `/sweep set interval <ticks>`
- `/sweep set threshold <count>`
- `/sweep set radius <blocks>`
- `/sweep set y <min> <max>`
- `/sweep list`
- `/sweep list <listName>`
- `/sweep lists`
- `/sweep list-create <listName>`
- `/sweep list-delete <listName>`
- `/sweep list-use <listName>`
- `/sweep add`
- `/sweep add <itemId>`
- `/sweep remove <itemId>`
- `/sweep confirm <requestId> yes`
- `/sweep confirm <requestId> no`

## 阈值提示规则

当阈值检测触发时：

- OP2 玩家收到可点击确认消息。
- 非 OP 玩家只收到普通文本提示（不可点击）。
- 若当前无 OP2 在线：
  - 不生成可确认请求，
  - 不广播阈值交互消息，
  - 仅记录服务器日志。

## 清理安全边界

- 没有玩家在线时，`/sweep run` 不会清理任何实体，并返回 `sweep.none`。
- 控制台执行 `/sweep run` 也不会切换为全世界清理。
- 清理时按每个世界分别检查 `GameRules.RULE_DOENTITYDROPS`：
  - 关闭的世界会被跳过，
  - 跳过信息会写入清理报告。

## 配置

配置文件路径：
- `config/stellarsweeper.json`

核心字段：
- `enableAutoCleanup`
- `enableThresholdCheck`
- `cleanupInterval`
- `thresholdCheckInterval`
- `warningCooldown`
- `cleanRadius`
- `yMin`
- `yMax`
- `itemThreshold`
- `language`（仅兼容保留，不用于强制服务端语言）
- `currentCleanupList`
- `cleanupLists`

若配置文件不存在，首次启动会自动生成默认配置。

## 构建

在仓库根目录执行：

```bash
./gradlew build
./gradlew :fabric:build
./gradlew :neoforge:build
```

## 说明

- 本版本不注册任何新物品。
- 不包含 `cleanup_hoe`。
- 不实现客户端 GUI。
