# 记账本应用

一个美观实用的安卓记账应用，帮助用户轻松管理个人财务。

## 功能特点

- 轻松记录收入和支出，支持多种类别分类
- 直观的财务概览，显示总收入、总支出和结余
- 按日、周、月、年查看财务统计分析
- 自定义收入和支出类别
- 支持多种货币格式
- 支持暗黑模式及其他主题设置
- 支持负债管理与记录
- 支持CSV格式数据导入导出（UTF-8 with BOM编码）
- 支持导出原始SQL数据库文件（开发者模式）

## 技术栈

- Java 17
- Android MVVM架构
- Room数据库
- LiveData和ViewModel
- Material Design界面组件
- MPAndroidChart图表库

## 如何构建

本项目使用Gradle 8.13进行构建，在Windows 10环境下开发。

1. 克隆项目到本地
2. 使用Gradle命令行构建项目：
   ```
   gradle build
   ```
3. 或使用Gradle包装器：
   ```
   ./gradlew build
   ```

## 开发者功能

应用包含一些仅供开发者使用的功能：

1. **导出SQL数据库文件**：在JizhangApplication.java中将`ENABLE_EXPORT_SQL`设置为true可以在设置界面显示导出SQL文件按钮，方便开发者调试和数据迁移。

## 屏幕截图

- 主页面：显示收支概览和最近交易记录
- 添加交易：简单直观的交易录入界面
- 统计分析：图表展示收支分布和趋势
- 设置页面：自定义应用行为和外观

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/jizhang/
│   │   ├── model/                     # 数据模型和数据库相关类
│   │   │   ├── AppDatabase.java       # Room数据库定义
│   │   │   ├── Category.java          # 分类数据模型
│   │   │   ├── CategoryDao.java       # 分类数据访问对象
│   │   │   ├── CategoryRepository.java # 分类仓库
│   │   │   ├── CategorySummary.java   # 分类统计模型
│   │   │   ├── DateConverter.java     # 日期类型转换器
│   │   │   ├── DebtTypeConverter.java # 负债类型转换器
│   │   │   ├── Transaction.java       # 交易数据模型
│   │   │   ├── TransactionDao.java    # 交易数据访问对象
│   │   │   ├── TransactionRepository.java # 交易仓库
│   │   │   └── TransactionTypeConverter.java # 交易类型转换器
│   │   ├── ui/                        # 界面相关类
│   │   │   ├── activity/              # 活动相关类
│   │   │   ├── debt/                  # 负债相关界面
│   │   │   │   ├── AddDebtActivity.java # 添加负债活动
│   │   │   │   ├── AddDebtRepaymentActivity.java # 添加还款活动
│   │   │   │   ├── DebtDetailsActivity.java # 负债详情活动
│   │   │   │   ├── DebtDetailsFragment.java # 负债详情片段
│   │   │   │   └── DebtDetailsViewModel.java # 负债详情视图模型
│   │   │   ├── expense/               # 支出相关界面
│   │   │   │   ├── AddExpenseActivity.java # 添加支出活动
│   │   │   │   ├── CategoryAdapter.java # 类别适配器
│   │   │   │   └── AddExpenseViewModel.java # 支出视图模型
│   │   │   ├── helper/                # 辅助类
│   │   │   │   └── SwipeToDeleteCallback.java # 滑动删除回调
│   │   │   ├── income/                # 收入相关界面
│   │   │   │   ├── AddIncomeActivity.java # 添加收入活动
│   │   │   │   └── AddIncomeViewModel.java # 收入视图模型
│   │   │   ├── settings/              # 设置相关界面
│   │   │   │   ├── SettingsActivity.java # 设置活动
│   │   │   │   └── CategoryManagementActivity.java # 类别管理活动
│   │   │   └── stats/                 # 统计相关界面
│   │   ├── JizhangApplication.java    # 应用类（包含功能开关）
│   │   ├── MainActivity.java          # 主活动
│   │   ├── MainViewModel.java         # 主视图模型
│   │   └── TransactionAdapter.java    # 交易适配器（支持编辑和删除）
│   ├── res/                           # 资源文件
│   │   ├── drawable/                  # 图形资源
│   │   ├── layout/                    # 布局文件
│   │   │   ├── activity_main.xml      # 主界面布局
│   │   │   ├── activity_settings.xml  # 设置界面布局
│   │   │   ├── dialog_transaction_details.xml # 交易详情对话框布局
│   │   │   └── item_transaction.xml   # 交易列表项布局
│   │   ├── menu/                      # 菜单文件
│   │   ├── values/                    # 值资源
│   │   │   ├── colors.xml             # 颜色定义
│   │   │   ├── strings.xml            # 字符串资源
│   │   │   └── styles.xml             # 样式定义
│   │   └── xml/                       # XML配置文件
│   │       └── file_paths.xml         # 文件路径配置（用于分享导出文件）
│   └── AndroidManifest.xml            # 应用清单
└── build.gradle                       # 应用级别的构建配置
``` 
