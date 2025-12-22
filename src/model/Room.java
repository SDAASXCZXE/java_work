package model;

import java.util.*;

/**
 * 宿舍房间实体类
 * 表示宿舍的基本信息（楼栋、房间号、房型、床位、宿舍长等）
 */
public class Room {
    private String roomNumber;    // 宿舍号，如 "101"
    private String building;       // 宿舍楼，如 "A栋"
    private RoomType roomType;    // 房间类型
    private int totalBeds;        // 床位总数
    private int occupied;          // 已住人数
    private int availableBeds;    // 空余床位
    private String monitor;        // 宿舍长学号，可能为空
    private String phone;          // 联系电话，可能为空
    private int hygieneScore;     // 卫生评分
    private RoomStatus status;     // 状态
    private String remarks;        // 备注，可能为空
    
    // 床位分配信息：床位号 -> 学生学号
    private Map<Integer, String> bedAssignment;  // 床位分配映射

    // 房间类型枚举
    public enum RoomType {
        SINGLE("单人间", 1),
        DOUBLE("二人间", 2),
        QUAD("四人间", 4),
        SIX("六人间", 6);

        private final String description;
        private final int bedCount;

        RoomType(String description, int bedCount) {
            this.description = description;
            this.bedCount = bedCount;
        }

        public String getDescription() {
            return description;
        }
        
        public int getBedCount() {
            return bedCount;
        }
    }

    // 房间状态枚举
    public enum RoomStatus {
        FULL("已住满"),
        AVAILABLE("有空位"),
        VACANT("空置");

        private final String description;

        RoomStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 有参构造函数，创建一个完整的 Room 实例
     */
    public Room(String roomNumber, String building, RoomType roomType, int totalBeds, int occupied, int availableBeds, 
                String monitor, String phone, int hygieneScore, RoomStatus status, String remarks) {
        this.roomNumber = roomNumber;
        this.building = building;
        this.roomType = roomType;
        this.totalBeds = totalBeds;
        this.occupied = occupied;
        this.availableBeds = availableBeds;
        this.monitor = monitor;
        this.phone = phone;
        this.hygieneScore = hygieneScore;
        this.status = status;
        this.remarks = remarks;
        this.bedAssignment = new HashMap<>();
        
        // 确保床位数量一致性
        updateStatus();
    }
    
    /**
     * 简化构造函数，根据房间类型自动设置床位数量
     */
    public Room(String roomNumber, String building, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.building = building;
        this.roomType = roomType;
        this.totalBeds = roomType.getBedCount();
        this.occupied = 0;
        this.availableBeds = totalBeds;
        this.hygieneScore = 100;
        this.status = RoomStatus.VACANT;
        this.remarks = "";
        this.bedAssignment = new HashMap<>();
    }

    // Getter / Setter方法
    public String getRoomNumber() { 
        return roomNumber; 
    }
    
    public void setRoomNumber(String roomNumber) { 
        this.roomNumber = roomNumber; 
    }

    public String getBuilding() { 
        return building; 
    }
    
    public void setBuilding(String building) { 
        this.building = building; 
    }

    public RoomType getRoomType() { 
        return roomType; 
    }
    
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType;
        this.totalBeds = roomType.getBedCount();
        updateStatus();
    }

    public int getTotalBeds() { 
        return totalBeds; 
    }
    
    public void setTotalBeds(int totalBeds) { 
        this.totalBeds = totalBeds;
        updateStatus();
    }

    public int getOccupied() { 
        return occupied; 
    }
    
    public void setOccupied(int occupied) { 
        this.occupied = occupied;
        updateStatus();
    }

    public int getAvailableBeds() { 
        return availableBeds; 
    }
    
    public void setAvailableBeds(int availableBeds) { 
        this.availableBeds = availableBeds;
        updateStatus();
    }

    public String getMonitor() { 
        return monitor; 
    }
    
    public void setMonitor(String monitor) { 
        this.monitor = monitor; 
    }

    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }

    public int getHygieneScore() { 
        return hygieneScore; 
    }
    
    public void setHygieneScore(int hygieneScore) { 
        this.hygieneScore = Math.max(0, Math.min(100, hygieneScore));
    }

    public RoomStatus getStatus() { 
        return status; 
    }
    
    public void setStatus(RoomStatus status) { 
        this.status = status; 
    }

    public String getRemarks() { 
        return remarks; 
    }
    
    public void setRemarks(String remarks) { 
        this.remarks = remarks; 
    }
    
    public Map<Integer, String> getBedAssignment() {
        return Collections.unmodifiableMap(bedAssignment);
    }
    
    // 床位分配管理
    public boolean assignBed(int bedNumber, String studentId) {
        if (bedNumber < 1 || bedNumber > totalBeds) {
            return false;  // 床位号无效
        }
        if (bedAssignment.containsKey(bedNumber)) {
            return false;  // 床位已分配
        }
        bedAssignment.put(bedNumber, studentId);
        occupied++;
        updateStatus();
        return true;
    }
    
    public boolean unassignBed(int bedNumber) {
        if (bedAssignment.remove(bedNumber) != null) {
            occupied--;
            updateStatus();
            return true;
        }
        return false;
    }
    
    public String getStudentByBed(int bedNumber) {
        return bedAssignment.get(bedNumber);
    }
    
    public Optional<Integer> getBedByStudent(String studentId) {
        for (Map.Entry<Integer, String> entry : bedAssignment.entrySet()) {
            if (entry.getValue().equals(studentId)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }
    
    public boolean isStudentInRoom(String studentId) {
        return bedAssignment.containsValue(studentId);
    }
    
    // 更新房间状态
    private void updateStatus() {
        availableBeds = totalBeds - occupied;
        
        if (availableBeds == totalBeds) {
            status = RoomStatus.VACANT;
        } else if (availableBeds > 0) {
            status = RoomStatus.AVAILABLE;
        } else {
            status = RoomStatus.FULL;
        }
    }

    /**
     * 返回对象的字符串表示
     */
    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", roomType=" + roomType.getDescription() +
                ", totalBeds=" + totalBeds +
                ", occupied=" + occupied +
                ", availableBeds=" + availableBeds +
                ", status=" + status.getDescription() +
                ", monitor='" + monitor + '\'' +
                ", phone='" + phone + '\'' +
                ", hygieneScore=" + hygieneScore +
                ", remarks='" + remarks + '\'' +
                ", bedAssignment=" + bedAssignment +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(roomNumber, room.roomNumber) && Objects.equals(building, room.building);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber, building);
    }
}
