# 食物志(FoodJournal) - 美食记录应用

<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261908531.png" alt="食物志应用图标" width="200">
</div>

食物志(FoodJournal) 是一款专为美食爱好者设计的移动应用，帮助用户记录、管理和分享美食体验。通过简洁直观的界面，用户可以快速记录美食信息、查看历史记录、分析消费趋势，并在地图上查找自己尝过的美食位置。

## 📱 功能特点

- **🍽️ 美食记录管理**：添加、查看、编辑和删除美食记录
- **📝 详细信息记录**：支持记录店铺名称、价格、评分、标签、日期和个人备注
- **📷 图片上传**：支持拍照或从相册选择图片
- **📊 数据统计分析**：提供消费趋势和评分分布图表
- **🗺️ 地图标记**：在地图上展示美食位置分布
- **🔍 筛选功能**：按价格、评分、标签和位置筛选记录
- **☁️ 云同步**：基于Appwrite实现数据云端存储和同步
- **👤 个人资料管理**：修改头像和个人信息

## 🔧 技术架构

食物志采用单Activity多Fragment架构，基于Java语言开发，使用以下核心技术：

- **前端界面**：基于原生Android视图组件和MaterialDesign组件
- **数据管理**：使用Repository模式管理数据访问
- **云存储**：基于Appwrite提供云存储和身份验证
- **图表展示**：使用MPAndroidChart库实现数据可视化
- **地图功能**：集成OsmDroid库和天地图API展示位置数据
- **图片处理**：使用Glide实现图片加载和缓存

## 📱 界面展示

### 01 开始界面

<div align="center">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750710.png" width="30%" alt="启动界面 - 应用品牌展示">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750751.png" width="30%" alt="登录界面 - 支持邮箱登录">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750759.png" width="30%" alt="注册界面 - 创建新账户">
</div>

### 02 主页

<div align="center">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750654.png" width="30%" alt="主页 - 美食记录卡片列表和月度统计">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750733.png" width="30%" alt="美食详情 - 查看完整美食信息和标签">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750619.png" width="30%" alt="加载状态 - 动画加载效果">
</div>

<div align="center">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750646.png" width="30%" alt="空记录状态 - 首次使用提示">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750627.png" width="30%" alt="添加美食 - 表单支持多种信息录入">
</div>

### 03 统计界面

<div align="center">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751868.png" width="30%" alt="统计列表 - 按条件筛选的美食记录">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751904.png" width="30%" alt="统计图表 - 消费趋势和评分分布可视化">
</div>

### 04 食物地图

<div align="center">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751479.png" width="30%" alt="食物地图 - 美食位置地理分布">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751409.png" width="30%" alt="地图信息窗口 - 位置标记美食简介">
</div>

### 05 个人界面

<div align="center">
<img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751704.png" width="30%" alt="个人资料 - 用户信息和头像管理">
</div>

## 💻 系统要求

- Android 8.0 (API 26) 或更高版本
- 至少100MB可用存储空间
- 网络连接（用于数据同步和地图功能）
- 相机和存储权限（用于上传图片）
- 位置权限（用于地图功能）

## 📲 安装说明

1. 下载最新版本的APK文件
2. 在设备上启用"未知来源"安装权限
3. 打开下载的APK文件并安装应用
4. 启动应用并使用邮箱注册新账户或登录现有账户

## 🔧 配置说明

在运行项目代码前，需要进行以下配置：

1. 将 `app/src/main/java/com/example/tastylog/config/AppConfig.template.java` 复制并重命名为 `AppConfig.java`
2. 注册 [Appwrite](https://appwrite.io/docs) 账户并创建新项目
3. 创建以下资源：
   - 数据库和食物记录集合
   - 存储桶（用于图片存储）
   - 用户认证服务
4. 在 `AppConfig.java` 中填入你的 Appwrite 项目 ID、数据库 ID、集合 ID 和存储桶 ID

```java
public class AppConfig {
    // Appwrite配置
    public static final String APPWRITE_ENDPOINT = "https://cloud.appwrite.io/v1";
    public static final String APPWRITE_PROJECT_ID = "YOUR_PROJECT_ID";
    public static final String APPWRITE_DATABASE_ID = "YOUR_DATABASE_ID";
    public static final String APPWRITE_COLLECTION_ID = "YOUR_COLLECTION_ID";
    public static final String APPWRITE_BUCKET_ID = "YOUR_BUCKET_ID";
}
```

## 📖 使用指南

### 账户管理
- 首次使用时需要注册新账户
- 使用邮箱和密码进行登录
- 在个人页面可以修改头像和个人信息

### 记录美食
1. 在主页点击"+"按钮添加新记录
2. 上传美食图片（拍照或从相册选择）
3. 填写店铺名称、价格、评分等信息
4. 添加标签和个人备注
5. 点击"保存"完成添加

### 查看统计
- 切换到"统计"选项卡
- 选择"列表"或"图表"查看方式
- 使用筛选功能按条件过滤数据

### 地图功能
- 切换到"地图"选项卡
- 查看所有美食记录的位置分布
- 点击标记查看美食详情

## 🧩 代码结构

```
com.example.tastylog/
├── activity/                 // 活动类
│   ├── MainActivity.java     // 主活动
│   ├── LoginActivity.java    // 登录活动
│   └── SplashActivity.java   // 启动活动
├── fragment/                 // 片段类
│   ├── HomeFragment.java     // 首页片段
│   ├── StatsFragment.java    // 统计片段
│   ├── MapFragment.java      // 地图片段
│   ├── MineFragment.java     // 个人片段
│   ├── AddFoodFragment.java  // 添加美食片段
│   └── FoodDetailFragment.java // 美食详情片段
├── adapter/                  // 适配器
│   ├── FoodCardAdapter.java  // 美食卡片适配器
│   └── FoodRecordAdapter.java // 美食记录适配器
├── data/                     // 数据层
│   └── FoodRepository.java   // 美食数据仓库
├── model/                    // 数据模型
│   └── FoodItem.java         // 美食项数据模型
└── utils/                    // 工具类
    ├── GeocodingHelper.java  // 地理编码工具
    └── FragmentUtils.java    // 片段工具
```
## 🔍 开发技术详解

### 单Activity多Fragment架构
食物志采用单Activity多Fragment架构，通过Fragment实现各个功能模块。这种架构有以下优势：
- 减少Activity切换开销，提高性能
- 简化界面切换动画和交互
- 更灵活的UI组织和复用

### 数据存储与同步
使用[Appwrite](https://appwrite.io/docs)作为后端服务：
- 用户认证和管理
- 美食记录的云端存储
- 图片存储和管理
- 实时数据同步

### 地图功能实现
基于OsmDroid和天地图API：
- 使用自定义标记展示美食位置
- 实现信息窗口显示简要信息
- 支持地图缩放和平移操作
- 自动聚焦包含所有标记的区域

### 数据可视化
使用MPAndroidChart库：
- 消费趋势折线图
- 评分分布饼图
- 自定义图表样式和交互

## ©️ 版权声明

© 2025 Lonely团队，保留所有权利

**注意**：此应用仅用于教育和演示目的。未经允许，不得复制、分发或商业使用。应用中使用的第三方库和组件版权归其各自所有者所有。

---

**开发者联系方式**：lingsou43@gmail.com

