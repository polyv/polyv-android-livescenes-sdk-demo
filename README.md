polyv-android-livescenes-sdk-demo
===

[![build passing](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![GitHub release](https://img.shields.io/badge/release-v1.17.0-blue.svg)](https://github.com/polyv/polyv-android-livescenes-sdk-demo/releases/tag/v1.17.0)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
- [polyv-android-livescenes-sdk-demo](#polyv-android-livescenes-sdk-demo)
    - [1 简介](#1-简介)
    - [2 体验 Demo](#2-体验-demo)
    - [3 文档](#3-文档)
      - [3.1 wiki文档](#31-wiki文档)
      - [3.2 Changelog](#32-changelog)
    - [4 Released 版本更新列表](#4-released-版本更新列表)
<!-- END doctoc generated TOC please keep comment here to allow auto update -->
### 1 简介
此项目是保利威 Android 多场景 Demo。

多场景项目的项目架构图如下：

![](https://polyv-repo.oss-cn-shenzhen.aliyuncs.com/android/resource/hierarchy.png)

多场景项目的文件目录结构如下：

```
|-- demo
|-- polyvLiveCommonModul （通用业务层）
|   |-- module
|   `-- ui
|-- polyvLiveCloudClassScene （观看端-云课堂场景）
|   |-- modules
|   `-- scenes
|-- polyvLiveEcommerceScene （观看端-直播带货场景）
|   |-- modules
|   `-- scenes
|-- polyvLiveStreamerScene （开播端-手机开播三分屏场景）
|   |-- modules
|   `-- scenes
|-- polyvStreamerAloneScene （开播端-手机开播纯视频场景）
|   |-- modules
|   |-- scenes
|   `-- ui
`-- polyvLiveHiClassScene （互动学堂场景）
    |-- modules
    |-- scenes
    `-- ui
```

### 2 体验 Demo

Demo [下载链接](https://www.pgyer.com/Mb6m) （密码：polyv）

### 3 文档
#### 3.1 wiki文档
可在 [Wiki 文档](https://help.polyv.net/index.html#/live/android/) 中，了解 **集成方式、项目结构、SDK能力、源码释义** 等内容
#### 3.2 Changelog
[全版本更新记录](./CHANGELOG.md)

### 4 Released 版本更新列表
以下表格反映：

1、Demo 的每个 Release 版本，所依赖的 SDK 版本

2、该 Release 版本的发版改动，所涉及到的场景（“✅ ” 表示涉及、包含该场景下的源码更新、改动）

| Github仓库Tag | 依赖SDK版本    | API文档                                                                                     | Common层 | 观看端-云课堂场景 | 观看端-直播带货场景 | 开播端-手机开播三分屏场景 | 开播端-手机开播纯视频场景 | 互动学堂场景 |
|-------------|------------|-------------------------------------------------------------------------------------------|---------|---------|----------|--------------|--------------|--------|
| 1.17.0      | 1.17.0     | [v1.17.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.17.0/index.html)         | ✅       |   ✅      |  ✅        |  ✅            |   ✅           |        |
| 1.16.4      | 1.16.4     | [v1.16.4 API](http://repo.polyv.net/android/livescenes/javadoc/1.16.4/index.html)         | ✅       |   ✅      |  ✅        |  ✅            |   ✅           |        |
| 1.16.3      | 1.16.3     | [v1.16.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.16.3/index.html)         | ✅       |         |          |              |              |        |
| 1.16.2      | 1.16.2     | [v1.16.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.16.2/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.16.1      | 1.16.1     | [v1.16.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.16.1/index.html)         | ✅       |         |          |              |              |        |
| 1.16.0      | 1.16.0     | [v1.16.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.16.0/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.15.1      | 1.15.1     | [v1.15.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.15.1/index.html)         | ✅       |         |          | ✅            | ✅            |        |
| 1.15.0      | 1.15.0     | [v1.15.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.15.0/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.14.0      | 1.14.0     | [v1.14.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.14.0/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.13.0      | 1.13.0     | [v1.13.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.13.0/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.12.2      | 1.12.2     | [v1.12.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.12.2/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.11.3      | 1.11.3     | [v1.11.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.11.3/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.11.2      | 1.11.2     | [v1.11.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.11.2/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.10.8      | 1.10.8     | [v1.10.8 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.8/index.html)         | ✅       | ✅       | ✅        |              |              |        |
| 1.10.7      | 1.10.7     | [v1.10.7 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.7/index.html)         | ✅       | ✅       | ✅        |              |              |        |
| 1.10.6      | 1.10.6     | [v1.10.6 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.6/index.html)         | ✅       | ✅       | ✅        |              |              |        |
| 1.10.5      | 1.10.5     | [v1.10.5 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.5/index.html)         | ✅       | ✅       |          | ✅            | ✅            |        |
| 1.10.4      | 1.10.4     | [v1.10.4 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.4/index.html)         | ✅       | ✅       | ✅        |              | ✅            |        |
| 1.10.4-abn  | 1.10.4-abn | [v1.10.4-abn API](http://repo.polyv.net/android/livescenes/javadoc/1.10.4-abn/index.html) | ✅       | ✅       |          |              |              |        |
| 1.10.3      | 1.10.3     | [v1.10.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.3/index.html)         | ✅       | ✅       |          |              |              |        |
| 1.10.2      | 1.10.2     | [v1.10.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.2/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.10.1.1    | 1.10.1.1   | [v1.10.1.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.1.1/index.html)     | ✅       |         |          |              |              |        |
| 1.10.1      | 1.10.1     | [v1.10.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.1/index.html)         | ✅       | ✅       | ✅        |              |              |        |
| 1.10.0      | 1.10.0     | [v1.10.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.10.0/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.9.5       | 1.9.5      | [v1.9.5 API](http://repo.polyv.net/android/livescenes/javadoc/1.9.5/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.9.4       | 1.9.4      | [v1.9.4 API](http://repo.polyv.net/android/livescenes/javadoc/1.9.4/index.html)           | ✅       | ✅       |          |              |              |        |
| 1.9.3       | 1.9.3      | [v1.9.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.9.3/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.9.1.1     | 1.9.1.1    | [v1.9.1.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.9.1.1/index.html)       | ✅       | ✅       | ✅        |              | ✅            |        |
| 1.9.1       | 1.9.1      | [v1.9.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.9.1/index.html)           | ✅       | ✅       | ✅        |              |              |        |
| 1.9.0       | 1.9.0      | [v1.9.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.9.0/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.8.3       | 1.8.3      | [v1.8.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.8.3/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.8.2       | 1.8.2      | [v1.8.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.8.2/index.html)           | ✅       | ✅       | ✅        |              |              | ✅      |
| 1.8.1       | 1.8.1      | [v1.8.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.8.1/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            | ✅      |
| 1.8.0       | 1.8.0      | [v1.8.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.8.0/index.html)           | ✅       | ✅       |          |              | ✅            | ✅      |
| 1.7.2       | 1.7.2      | [v1.7.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.7.2/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            | ✅      |
| 1.7.1       | 1.7.1      | [v1.7.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.7.1/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.7.0       | 1.7.0      | [v1.7.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.7.0/index.html)           | ✅       | ✅       |          | ✅            | ✅            | ✅      |
| 1.6.3       | 1.6.3      | [v1.6.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.6.2/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.6.0       | 1.6.0      | [v1.6.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.6.0/index.html)           | ✅       | ✅       |          | ✅            | ✅            |        |
| 1.5.3       | 1.5.3      | [v1.5.3 API](http://repo.polyv.net/android/livescenes/javadoc/1.5.3/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.5.2       | 1.5.2      | [v1.5.2 API](http://repo.polyv.net/android/livescenes/javadoc/1.5.2/index.html)           | ✅       | ✅       |          | ✅            | ✅            |        |
| 1.5.1.1     | 1.5.1.1    | [v1.5.1.1 API](http://repo.polyv.net/android/livescenes/javadoc/1.5.1/index.html)         | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.5.0       | 1.5.0      | [v1.5.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.5.0/index.html)           | ✅       | ✅       | ✅        | ✅            | ✅            |        |
| 1.4.0       | 1.4.0      | [v1.4.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.4.0/index.html)           | ✅       | ✅       | ✅        | ✅            |              |        |
| 1.3.0       | 1.3.0      | [v1.3.0 API](http://repo.polyv.net/android/livescenes/javadoc/1.3.0/index.html)           | ✅       | ✅       | ✅        |              |              |        |

更多版本更新详情，可在 [版本更新列表](./CHANGELOG.md)，了解 **对应版本更新说明**，以及 **下载源码**

