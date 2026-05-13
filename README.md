# 精准用药管理驾驶舱

> 南京中医药大学 · 第十二模块课程项目  
> 基于 Spring Boot 的慢性病患者用药依从性监测与可视化系统

---

## 项目背景

针对长期服药患者的依从性管理需求，本项目构建了一个集处方管理、服药追踪、统计分析、漏服预警于一体的 Web 看板系统。核心目标是以数据可视化的方式呈现患者的用药行为模式，帮助医护人员快速评估干预效果，辅助患者自我管理。

系统采用 **Spring Boot + MyBatis + Thymeleaf** 架构，前端以 **Apache ECharts** 驱动多维度数据图表，支持桌面端与移动端响应式布局。

---

## 功能概览

### 驾驶舱看板
- **患者概览**：达标率环形图、活跃计划数、待处理事项、健康状态评级
- **依从性趋势**：折线图展示完成率变化曲线，含 7 日均线和 80% 达标参考线
- **今日处方**：当前日期的活跃用药计划卡片
- **用药时间线**：当日服药记录状态流（已服 / 漏服 / 补服 / 待服）
- **关键指标**：打卡率、达标率、漏服率、连续打卡天数（含历史最长）
- **时段雷达图**：早 / 中 / 晚三个时段的完成率对比
- **药品依从排行**：按药品维度的服药率柱状图
- **漏服行动卡**：近 30 天漏服记录及补服建议

### 独立视图（侧边栏导航）
- **处方计划视图**：当前用户全部活跃计划，含药品名称、剂量、频次、时段
- **药品目录视图**：全院药品基本信息（规格、厂家、分类、通用名）
- **漏服提醒视图**：漏服记录详情与严重程度分级（高优先级 / 需确认 / 提醒）
- **用户档案视图**：个人基本信息、过敏史、病史

### 交互特性
- 支持多用户切换（默认 4 位测试患者）
- 趋势图周期切换（7 天 / 14 天 / 30 天）
- 关键指标数值入场动画
- 三档响应式断点（≥1280px / ≥820px / 移动端）
- 侧边栏五视图单页切换，无整页刷新

---

## 技术架构

| 层次 | 技术选型 | 版本 |
|------|---------|------|
| 应用框架 | Spring Boot | 3.4.5 |
| 持久层 | MyBatis (XML Mapper) | 3.0.5 |
| 模板引擎 | Thymeleaf | — |
| 数据可视化 | Apache ECharts | 5.4.3 |
| 图标库 | Lucide | latest |
| 关系数据库 | MySQL | 8.0 |
| 缓存中间件 | Redis | — |
| 消息中间件 | Apache RocketMQ | — |
| 对象存储 | MinIO | — |
| 构建工具 | Maven | — |
| 运行环境 | Java 17 | — |

中间件（Redis、RocketMQ、MinIO）通过 Docker 统一部署于内网虚拟机，数据库运行于非标准端口。

---

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.6+
- Docker 环境已启动（确保 MySQL、Redis、RocketMQ、MinIO 容器运行中）
- 数据库 `twelfth_module` 已建表并导入测试数据

### 启动步骤

```bash
# 1. 克隆项目
git clone <repo-url>
cd twelfth_module

# 2. 编译运行
mvn spring-boot:run

# 3. 访问
# 浏览器打开 http://localhost:8080
```

根路径 `/` 自动重定向至 `/statistics` 驾驶舱页面。

### 配置说明

核心配置文件 `src/main/resources/application.yml`（已纳入 `.gitignore`，需自行创建）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<db-host>:<db-port>/twelfth_module
    username: <your-username>
    password: <your-password>
  data:
    redis:
      host: <redis-host>
      port: 6379

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

rocketmq:
  namesrv-addr: <rocketmq-host>:9876

minio:
  endpoint: http://<minio-host>:9000
  access-key: <your-access-key>
  secret-key: <your-secret-key>

server:
  port: 8080
```

---

## API 接口

| 方法 | 端点 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/statistics/all` | `userId`, `trendDays?`, `topDrugLimit?` | 聚合统计（summary + trend + topDrugs + missedAlerts + 今日计划/记录） |
| GET | `/api/statistics/missedAlerts` | `userId`, `startDate`, `endDate` | 漏服提醒列表 |
| GET | `/api/plans` | `userId` | 用户活跃处方计划 |
| GET | `/api/drugs` | — | 药品目录全量 |
| GET | `/api/users/{id}` | — | 用户档案详情 |

响应格式统一为：

```json
{
  "success": true,
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1715587200000
}
```

---

## 项目结构

```
twelfth_module/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/org/example/twelfth_module/
    │   │   ├── TwelfthModuleApplication.java    # 启动类
    │   │   ├── controller/
    │   │   │   ├── StatisticsController.java    # 统计聚合 + 漏服提醒
    │   │   │   ├── PlanController.java          # 处方计划
    │   │   │   ├── DrugController.java          # 药品目录
    │   │   │   ├── UserController.java          # 用户档案
    │   │   │   └── PageController.java          # 页面路由
    │   │   ├── service/
    │   │   │   ├── StatisticsService.java       # 统计服务接口
    │   │   │   └── impl/
    │   │   │       └── StatisticsServiceImpl.java # 统计逻辑实现
    │   │   ├── mapper/                          # MyBatis Mapper 接口
    │   │   ├── entity/                          # 实体 (User/Drug/MedicationPlan/MedicationRecord)
    │   │   └── dto/                             # 数据传输对象 (DailyStats/DrugUsageStat/MissedAlert/StatisticsSummary)
    │   └── resources/
    │       ├── application.yml                  # 应用配置（gitignore）
    │       ├── mapper/                          # MyBatis XML 映射文件
    │       ├── templates/statistics/
    │       │   └── index.html                   # Thymeleaf 驾驶舱页面
    │       └── static/
    │           ├── js/statistics.js             # 前端逻辑（原生 JS + ECharts）
    │           └── img/                         # 静态资源
    └── test/                                    # 单元测试
```

---

## 数据库设计

### 核心表

| 表名 | 说明 | 关键字段 |
|------|------|------|
| `user` | 患者信息 | id, username, real_name, allergy_history, medical_history |
| `drug` | 药品目录 | id, drug_code, drug_name, specification, manufacturer, category |
| `medication_plan` | 用药计划 | id, user_id, drug_id, dosage, frequency, time_slot, scheduled_time, start_date, end_date |
| `medication_record` | 服药记录 | id, user_id, plan_id, drug_id, scheduled_time, actual_time, status, is_on_time, record_date |

### 服药记录状态码

| 状态值 | 含义 |
|--------|------|
| 0 | 待服 |
| 1 | 已服 |
| 2 | 漏服 |
| 3 | 补服 |

---

## 设计说明

- **色彩体系**：以鼠尾草绿（`#174f47`）为主色调，配合薄荷绿（`#d2eee6`）与纸白（`#fffaf2`），契合医疗场景的洁净感与信赖感
- **环形达标率**：纯 CSS `conic-gradient` 实现，无第三方 UI 组件依赖
- **缓存策略**：关键指标数字采用 `requestAnimationFrame` 缓动动画，提升交互质感
- **响应式布局**：CSS Grid 三档自适应，断点 1280px / 820px，移动端自动切换单列布局
- **动效控制**：遵循 `prefers-reduced-motion` 媒体查询，尊重用户系统无障碍偏好

---

## 开发记录

| 提交 | 日期 | 内容 |
|------|------|------|
| `20c5fe1` | 2025-03 | 项目初始化，Docker 环境搭建 |
| `794ac24` | 2025-04 | 前后端基本功能完成 |
| `bb31e3d` | 2025-04 | 代码优化，消除警告 |
| `c7d4038` | 2025-04 | 前端 UI 重构美化 |
| `3febabd` | 2025-05 | 功能完善，代码优化，冗余清理 |

---

## 待完善

- Redis / RocketMQ / MinIO 配置已预留，业务层集成尚未实现
- 当前为单数据源直连模式，后续可引入缓存层降低数据库压力
- 用户认证与权限控制模块待补充

---

## License

本项目为南京中医药大学课程作业，仅供学习参考。
