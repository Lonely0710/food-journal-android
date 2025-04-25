<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202504070132246.jpg" width="200" alt="应用图标">
</div>

<p align="center">
  <img alt="license" src="https://img.shields.io/github/license/Lonely0710/food-journal-Android" />
  <img alt="stars" src="https://img.shields.io/github/stars/Lonely0710/food-journal-Android" />
  <img alt="forks" src="https://img.shields.io/github/forks/Lonely0710/food-journal-Android" />
  <img alt="issues" src="https://img.shields.io/github/issues/Lonely0710/food-journal-Android" />
  <!-- 如果你的项目有发布版本，可以取消下面这行的注释 -->
  <!-- <img alt="release" src="https://img.shields.io/github/v/release/Lonely0710/food-journal-Android" /> -->
  <!-- 如果你的项目有总下载量统计，可以取消下面这行的注释 -->
  <!-- <img alt="downloads" src="https://img.shields.io/github/downloads/Lonely0710/food-journal-Android/total" /> -->
</p>

# 食物志 - Food Journal

> 一个精致的美食记录与发现平台，用数据讲述你的美食故事

## 🌟 功能全景

### 📱 核心功能
| 模块     | 特性                          | 技术亮点                    |
| -------- | ----------------------------- | --------------------------- |
| **记录** | 店铺/价格/评分/标签/位置/图片 | Material Design 3表单       |
| **统计** | 趋势图表/分布分析/热力图规划  | MPAndroidChart + 自定义渲染 |
| **地图** | 标记聚类/位置导航/地理围栏    | OsmDroid + 天地图API        |
| **同步** | 多端实时同步/离线优先         | Appwrite SDK + Room         |

## 🖼 界面展示

### 🔐 身份验证流程
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750710.png" width="30%" alt="品牌启动页">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750751.png" width="30%" alt="邮箱登录界面"> 
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750759.png" width="30%" alt="用户注册界面">
  
  应用启动与身份验证流程
</div>

---

### 🏠 核心交互
<div align="center">
  <div>
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750654.png" width="30%" alt="主页卡片列表">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750733.png" width="30%" alt="美食详情展示">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750619.png" width="30%" alt="加载动画效果">
  </div>
  
  <div style="margin-top:20px">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750646.png" width="30%" alt="空状态提示">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261750627.png" width="30%" alt="新增记录表单">
  </div>
  
  主页功能与数据录入交互
</div>

---

### 📈 数据分析
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202504070131202.png" width="30%" alt="筛选统计列表">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751904.png" width="30%" alt="可视化图表">
  
  数据统计与分析视图
</div>

---

### 🌍 地理分布
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751479.png" width="30%" alt="美食地图概览">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751409.png" width="30%" alt="位置标记详情">
  
  地理位置标记与探索
</div>

---

### 👤 用户管理
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202503261751704.png" width="30%" alt="个人资料管理">
  
  账户与个人信息管理
</div>

## 🏗️ 技术架构

### 系统设计
```text
架构模式: 单Activity多Fragment
开发语言: Java
核心组件:
  ├── 数据层: Repository模式 + Appwrite SDK
  ├── 展示层: ViewBinding + LiveData
  ├── 业务层: 模块化Fragment设计
  └── 工具层: Glide图片处理 + MPAndroidChart
```
## ⚙️ 配置指南
### 环境要求
- Android Studio Flamingo+

- Java 11+

- Appwrite服务实例

### 云服务配置
1. 复制配置文件模板：
   ```bash
   cp app/src/main/java/com/example/tastylog/config/AppConfig.template.java \
      app/src/main/java/com/example/tastylog/config/AppConfig.java
   ```
2. 配置Appwrite参数：
   ```java
   public class AppConfig {
       public static final String APPWRITE_PROJECT_ID = "your_project_id";
       public static final String APPWRITE_DATABASE_ID = "food_journal";
       // 其他配置项...
   }
   ```
3. 初始化云资源：
   ```bash
   # 创建存储桶
   appwrite storage createBucket --name food-images --permission read
   ```

## 📦 安装与使用

### APK安装
```bash
adb install app/release/foodjournal-v1.0.0.apk
```

### 开发构建
```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease
```

## 🌱 贡献指引
欢迎通过以下方式参与项目：
- 在Issues报告问题或建议
- 提交Pull Request时请：
  - 遵循现有代码风格
  - 更新相关文档
  - 添加必要的单元测试

## 📜 许可协议
本项目基于 [MIT License](LICENSE) 开源，允许自由使用和修改，但需保留原始版权声明。

---

<details>
<summary>📮 联系维护者</summary>

**核心开发者**：Lonely团队  
**电子邮箱**：lingsou43@gmail.com  
**技术栈咨询**：欢迎提交Issue讨论  
**路线图**：  
- [x] 基础功能实现 (2025 Q1)  
- [ ] 热力图分析 (2025 Q3)  
- [ ] AI美食推荐 (2026 Q1)  
</details>