package model;

import java.util.Optional;

/**
 * 宿舍房间实体类
 * 表示宿舍的基本信息（楼栋、房间号、房型、床位、宿舍长等）
 */

public class Room {
    private String room_number;    // 宿舍号，如 "101"
    private String building;       // 宿舍楼，如 "A栋"
    private RoomType room_type;    // 房间类型，如 四人间
    private int total_beds;        // 床位总数
    private int occupied;          // 已住人数
    private int available_beds;    // 空余床位
    private String monitor;        // 宿舍长，可能为空
    private String phone;          // 联系电话，可能为空
    private int hygiene_score;     // 卫生评分
    private RoomStatus status;     // 状态，如 已住满、有空位、空置
    private String remarks;        // 备注，可能为空

    // 枚举：房间类型
    /**
     * 房间类型枚举
     * SINGLE - 单人间
     * DOUBLE - 二人间
     * QUAD   - 四人间
     * SIX    - 六人间
     */
    public enum RoomType {
        SINGLE("单人间"),
        DOUBLE("二人间"),
        QUAD("四人间"),
        SIX("六人间");

        private final String description;

        RoomType(String description) {
            this.description = description;
        }

        /**
         * 返回类型的中文描述
         * @return 描述字符串，例如 "四人间"
         */
        public String getDescription() {
            return description;
        }
    }

    // 枚举：房间状态
    /**
     * 房间状态枚举
     * FULL      - 已住满
     * AVAILABLE - 有空位
     * VACANT    - 空置
     */
    public enum RoomStatus {
        FULL("已住满"),
        AVAILABLE("有空位"),
        VACANT("空置");

        private final String description;

        RoomStatus(String description) {
            this.description = description;
        }

        /**
         * 返回状态的中文描述
         * @return 描述字符串，例如 "有空位"
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * 有参构造函数，创建一个完整的 Room 实例
     * 参数顺序与数据库列对应：room_number, building, room_type, total_beds, occupied, available_beds, monitor, phone, hygiene_score, status, remarks
     */
    public Room(String room_number, String building, RoomType room_type, int total_beds, int occupied, int available_beds, String monitor, String phone, int hygiene_score, RoomStatus status, String remarks) {
        this.room_number = room_number;
        this.building = building;
        this.room_type = room_type;
        this.total_beds = total_beds;
        this.occupied = occupied;
        this.available_beds = available_beds;
        this.monitor = monitor;
        this.phone = phone;
        this.hygiene_score = hygiene_score;
        this.status = status;
        this.remarks = remarks;
    }

    // Getter / Setter（snake_case，对应数据库列）
    public String getRoom_number() { return room_number; }
    public void setRoom_number(String room_number) { this.room_number = room_number; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public RoomType getRoom_type() { return room_type; }
    public void setRoom_type(RoomType room_type) { this.room_type = room_type; }

    public int getTotal_beds() { return total_beds; }
    public void setTotal_beds(int total_beds) { this.total_beds = total_beds; }

    public int getOccupied() { return occupied; }
    public void setOccupied(int occupied) { this.occupied = occupied; }

    public int getAvailable_beds() { return available_beds; }
    public void setAvailable_beds(int available_beds) { this.available_beds = available_beds; }

    public Optional<String> getMonitor() { return Optional.ofNullable(monitor); }
    public void setMonitor(String monitor) { this.monitor = monitor; }

    public Optional<String> getPhone() { return Optional.ofNullable(phone); }
    public void setPhone(String phone) { this.phone = phone; }

    public int getHygiene_score() { return hygiene_score; }
    public void setHygiene_score(int hygiene_score) { this.hygiene_score = hygiene_score; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public Optional<String> getRemarks() { return Optional.ofNullable(remarks); }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    /**
     * 返回对象的字符串表示，便于调试和日志输出
     */
    @Override
    public String toString() {
        return "Room{" +
                "room_number='" + room_number + '\'' +
                ", building='" + building + '\'' +
                ", room_type=" + (room_type != null ? room_type.getDescription() : "") +
                ", total_beds=" + total_beds +
                ", occupied=" + occupied +
                ", available_beds=" + available_beds +
                ", monitor=" + getMonitor().orElse("无") +
                ", phone=" + getPhone().orElse("无") +
                ", hygiene_score=" + hygiene_score +
                ", status=" + (status != null ? status.getDescription() : "") +
                ", remarks=" + getRemarks().orElse("无") +
                '}';
    }
}
