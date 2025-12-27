项目讲解文档（中文，面向讲解/教学）

说明：
- 这份文档只作讲解使用，不会修改源码。文档放在 docs/ 下，遵循你要求“不要改我的任何代码”。
- 我用通俗语言介绍项目结构、每个包与关键类的职责、常见用例（学生端/管理员端流程）、如何在演示时讲解或演示某些功能、以及常见问题排查步骤。

目录
- 项目概览
- 如何运行（快速演示步骤）
- 数据库与配置
- 包与关键类说明（model / dao / service / ui / util / scripts / uimodel / test）
- 关键流程（学生注册->管理员同步 / 添加宿舍->分配 / 考勤流程 / 请假/假期/维修/访客流程 / 退宿换宿流程）
- 常见问题与排查（连接、时间格式、唯一约束）
- 给讲解者的演示脚本（5-10分钟快速演示与 15-20 分钟深入演示）

一、项目概览（1-2 句）
这是一个面向高校/宿舍管理的桌面应用（Java Swing），包含学生、宿舍、考勤、请假、假期登记、维修、访客、退宿/换宿等功能。后端通过 JDBC 连接 MySQL（连接逻辑在 util/DBUtil.java）。前端基于 Swing，各模块以面板（Panel）形式组织，便于在主界面用 Tab 展示。

二、如何运行（快速演示）
1. 打开 IDE（IntelliJ IDEA）并导入该项目（已有 java_work.iml）。
2. 确认数据库可用：检查文件 src/util/DBUtil.java 中的 URL、USER、PASSWORD，按需修改为本地数据库或测试数据库。不要把真实密码公开。
3. 在数据库执行 scripts 下的建表脚本：
   - scripts/create_leave_table.sql（请假）
   - scripts/create_room_change_table.sql（退宿/换宿）
   - 以及其他脚本（如果有）例如 create_holiday_table.sql 等
4. 启动程序：运行 src/App.java（Main），或在 IDE 中运行主类。
5. 登录（若有登录组件），进入管理端或学生端，演示功能。

三、数据库与配置要点
- JDBC 入口：src/util/DBUtil.java，返回 Connection。应用中所有 DAO 都用这个方法获取连接。
- SQL 脚本：位于 scripts/，包括创建表的语句。执行这些脚本以保证表存在。
- 字段映射：DAO 层以字符串/日期/时间戳等类型与数据库字段映射；模型类（model 包）使用 Java 类型（LocalDate/LocalDateTime/String 等）。

四、包与关键类说明（按包列出，便于讲解时逐层展开）

1) model（实体层）
 - Student.java：学生实体，包含学号(sno)、姓名、班级、宿舍号、楼栋等基本信息。讲解时说明模型仅为内存对象，持久化由 DAO 完成。
 - Room.java：宿舍实体，包含 roomNumber、building、roomType、totalBeds、occupied、availableBeds、monitor、phone、hygieneScore、status、remarks 等字段。
 - Attendance.java：考勤实体，含日期、时间、状态(enum AttendanceStatus)、备注等。
 - Holiday.java：假期登记实体（离校/返校），使用内部枚举 HolidayType, HolidayStatus；包含离校/返校时间、登记时间、联系方式等。
 - LeaveRequest.java：请假实体（病假/事假），为新增模块使用字符串表示类型和状态，包含开始/结束日期、事由、联系人等。
 - RoomChange.java：退宿/换宿申请实体，包含旧宿舍、新宿舍、原因、审批信息（status 为枚举）等。
 - Repair/Visitor 等模型：对应故障报修、访客登记等功能。

讲解要点：展示 model 时，指出每个字段的含义与数据库字段的对应关系（可在 DAO 注释处查到实际列名）。

2) dao（数据访问层）与 dao.impl（JDBC 实现）
 - 每个功能模块（Attendance/Holiday/Room/Student/Repair/Visitor/RoomChange/Leave）有对应的 DAO 接口，接口定义了基本的 CRUD 方法。例如：
   - HolidayDao.java：findByStudentId/addHoliday/deleteById
   - RoomChangeDao.java：findByStudentId/addRoomChange/deleteById
 - 实现类（dao.impl/*.java）使用 JDBC（PreparedStatement）执行 SQL，利用 util.DBUtil.getConnection() 获取连接。
 - 读取时间字段时常用 ResultSet.getTimestamp(...) 转为 LocalDateTime；写入时用 PreparedStatement.setTimestamp(...)
 - 讲解时注意：DAO 实现里有若干用反射设置私有字段（例如设置模型的 registerTime/updateTime/status），可向听众解释这是为了在不改变模型构造函数的前提下还原数据库时间字段。

3) service（业务层）与 service.impl
 - Service 层封装 DAO 的调用，并捕获异常返回默认值（例如返回 false 或空列表），这简化了 UI 层的错误处理。示例如：HolidayService/HolidayServiceImpl
 - 讲解要点：Service 层是业务逻辑的合适位置（校验、事务、冲突检测），但现在实现主要为转发到 DAO 并处理异常。

4) ui（界面层）
 - StudentDashboardPanel.java：学生主面板，包含多个 tab（个人资料、故障报修、请假、假期、退宿/换宿等）
 - StudentHolidayPanel.java：学生端假期登记面板，包含提交对话框（JSpinner 支持时间选择）、表格展示、删除功能。
 - StudentLeavePanel.java：学生端请假面板（病假/事假），与假期面板类似但独立数据表与 DAO/Service。
 - RoomPanel.java：管理员端宿舍管理（表格 + 我新增的柱状统计图），带增删改、入住退宿、导出等操作。
 - AttendancePanel.java：管理员端考勤管理，我已在此面板添加了按日期与按学号查询按钮；包含实时统计显示。
 - 其余面板：StudentPanel、VisitorPanel、RepairPanel 等负责各自模块的交互。

讲解提示：界面层应保持薄（仅做展示与表单校验），复杂逻辑放在 Service 层或 DAO 层。演示时可演示：提交申请 -> Service -> DAO -> DB -> UI 刷新（同时可在数据库中观察变化）。

5) util
 - DBUtil.java：数据库连接工具，注意配置 URL/USER/PASSWORD。
 - RefreshCenter.java：项目中的事件中心（可用于模块间刷新通知），向听众说明类似发布/订阅模式的用途。

6) scripts
 - SQL 建表脚本放在 scripts/ 下，执行这些脚本以创建必要的表。

7) uimodel（前端模型）
 - User/UserManager/UserType：用于登录/用户管理，区分管理员/学生等权限。

8) test
 - 提示：仓库中有 src/test/test.java，可扩展成单元测试或集成测试（JUnit）。

五、关键流程讲解（把复杂业务拆成 3-6 个步骤，便于现场演示）

示例 A：学生提交请假（学生端） -> 管理员审核（管理端）
1. 学生在 StudentLeavePanel 填写请假表单（类型、起始/截止日期、事由、联系人等）。
2. 前端校验日期顺序与必填项，然后构建 LeaveRequest 实体并调用 LeaveService.addLeave(l).
3. LeaveService 调用 LeaveDaoImpl.addLeave(...)，插入数据库表 leave_request。
4. 学生端收到成功反馈并刷新列表；管理员端可通过 RoomChange/Leave 管理面板查看新申请并审批（若实现审批功能）。

示例 B：管理员登记考勤并按日期/学号查询
1. 管理员在 AttendancePanel 点击“手动登记”，输入学号，系统从 StudentService 获取学生信息。
2. 构建 Attendance 实体并调用 AttendanceService.add(a) 将记录写入数据库。
3. 管理员可使用工具栏的“按日期查询”选择日期或使用“学号查询”快速定位某学生的考勤记录，UI 异步调用 Service 并渲染表格。

示例 C：宿舍管理与图表（演示我新增的图表）
1. 管理端打开 RoomPanel，表格显示所有宿舍。
2. loadRoomsFromDB 聚合每栋楼的床位与已住人数，计算入住率并传给 ChartPanel。
3. ChartPanel 使用 Java2D 绘制柱状图，展示每栋楼的占用情况。讲解时可演示新增宿舍改变数据并点击刷新统计。

六、常见问题与排查（讲解时给听众的实用技巧）
- 无法连接数据库：检查 `src/util/DBUtil.java` 的 URL/user/password；检查数据库是否允许远程连接或网络策略（防火墙/云 DB 白名单）。
- 日期格式错误：前端部分用 JSpinner + DateEditor 保证日期格式，DAO 使用 java.sql.Date/ Timestamp 转换。
- 唯一性约束：例如学号/宿舍号需唯一，Service/DAO 层应在插入前校验，或在 DB 中加唯一索引并在捕获 SQLException 后友好提示。
- 枚举与字符串：项目中一些状态字段使用枚举（model 中），有些新模块使用字符串。讲解时说明两者的优缺点（可读性 vs 可扩展性/兼容性）。

七、给讲解者的 10 分钟演示脚本（节奏建议）
0-1 分钟：项目目的与主要功能点（2-3 句）
1-3 分钟：项目结构（按包简单说明）
3-6 分钟：演示学生端提交请假 -> 表内新增记录（实操：提交一个请假，展示表格刷新）
6-8 分钟：演示管理员考勤查询（按日期/学号）与实时统计
8-10 分钟：演示宿舍管理与柱状图（新增一个宿舍或修改入住数，刷新统计）

八、扩展建议（演示后可讨论）
- 把枚举替换为数据库友好的字符串 code 并在 UI 做映射（利于跨语言/跨系统集成）。
- 为 Service 层加事务/并发控制（例如防止重复提交或重叠请假）。
- 引入单元测试（JUnit）和集成测试（使用内存数据库或测试库）以保证变更安全。
- 若要更漂亮的图表，可考虑引入 JFreeChart 或 JavaFX（需要额外依赖与修改 GUI 框架）。

文档结束 — 如需我把每个 Java 文件逐行注释成教学注释（例如在每个类上方添加长注释并解释每个方法），我可以替你生成一份补充文档，或在 docs/ 下生成单独的每文件讲解（避免修改源码）。请明确：
- 你是否希望把注释直接写到源代码文件里？（你之前强调“不要改我的任何代码” —— 若你同意，我可以修改源文件并插入注释；否则我将只生成 docs/ 下的每文件讲解文档。）

附注：我没有修改任何源码，只创建了本 docs 文件。如需我进一步生成更详细的 per-file 教学文档（每个类一个 Markdown），我会继续生成到 docs/ 下。

