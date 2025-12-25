package model;

import java.util.*;

/**
 * 宿舍房间实体类
 * 表示宿舍的基本信息（楼栋、房间号、房型、床位、宿舍长等）
 */
public class Room {
    private String roomNumber;    // 宿舍号，如 "101"
    private String building;       // 宿舍楼，如 "A栋"
    private String roomType;    // 房间类型
    private int totalBeds;        // 床位总数
    private int occupied;          // 已住人数
    private int availableBeds;    // 空余床位
    private String monitor;        // 宿舍长学号，可能为空
    private String phone;          // 联系电话，可能为空
    private int hygieneScore;     // 卫生评分
    private String status;     // 状态
    private String remarks;        // 备注，可能为空
    
    // 床位分配信息：床位号 -> 学生学号
    private Map<Integer, String> bedAssignment;  // 床位分配映射


    /**
     * 有参构造函数，创建一个完整的 Room 实例
     */
    public Room(String roomNumber, String building, String roomType, int totalBeds, int occupied, int availableBeds,
                String monitor, String phone, int hygieneScore, String status, String remarks) {
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
     * 简化构造函数，根据房间类型字符串自动设置床位数量（如果无法识别则使用默认 4）
     */
    public Room(String roomNumber, String building, String roomType) {
        this.roomNumber = roomNumber;
        this.building = building;
        this.roomType = roomType;
        this.totalBeds = bedCountFromRoomType(roomType);
        this.occupied = 0;
        this.availableBeds = totalBeds;
        this.hygieneScore = 100;
        this.status = "空置";
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

    public String getRoomType() {
        return roomType;
    }
    
    public void setRoomType(String roomType) {
        this.roomType = roomType;
        this.totalBeds = bedCountFromRoomType(roomType);
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

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
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
    
    // 更新房间状态（使用中文描述）
    private void updateStatus() {
        availableBeds = totalBeds - occupied;
        if (availableBeds >= totalBeds) {
            status = "空置";
        } else if (availableBeds > 0) {
            status = "有空位";
        } else {
            status = "已住满";
        }
    }

    /**
     * 根据房间类型字符串（如 "四人间"）推断床位数，如果无法识别返回默认 4
     */
    private int bedCountFromRoomType(String roomType) {
        if (roomType == null) return 4;
        roomType = roomType.trim();
        if (roomType.contains("二人")) return 2;
        if (roomType.contains("四人")) return 4;
        if (roomType.contains("六人")) return 6;
        if (roomType.contains("八人")) return 8;
        // 也支持英文写法
        if (roomType.toLowerCase().contains("double") || roomType.contains("2")) return 2;
        if (roomType.toLowerCase().contains("quad") || roomType.contains("4")) return 4;
        if (roomType.toLowerCase().contains("six") || roomType.contains("6")) return 6;
        if (roomType.toLowerCase().contains("eight") || roomType.contains("8")) return 8;
        return 4;
    }

    /**
     * 返回对象的字符串表示
     */
    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", building='" + building + '\'' +
                ", roomType='" + (roomType == null ? "" : roomType) + '\'' +
                ", totalBeds=" + totalBeds +
                ", occupied=" + occupied +
                ", availableBeds=" + availableBeds +
                ", status='" + (status == null ? "" : status) + '\'' +
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
