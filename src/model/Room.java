
package model;

import java.util.Date;

/**
 * 宿舍房间实体类
 */
public class Room {
    private int id; // 宿舍ID
    private String building; // 楼栋
    private String roomNumber; // 宿舍号
    private String roomType; // 房间类型
    private int capacity; // 容纳人数
    private int currentCount; // 当前人数
    private String manager; // 管理员
    private String managerPhone; // 管理员电话
    private String status; // 宿舍状态
    private Date createDate; // 创建日期

    public Room() {}

    public Room(int id, String building, String roomNumber, String roomType, int capacity, int currentCount, String manager, String managerPhone, String status, Date createDate) {
        this.id = id;
        this.building = building;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.currentCount = currentCount;
        this.manager = manager;
        this.managerPhone = managerPhone;
        this.status = status;
        this.createDate = createDate;
    }

    // getter和setter方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", building='" + building + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", currentCount=" + currentCount +
                ", manager='" + manager + '\'' +
                ", managerPhone='" + managerPhone + '\'' +
                ", status='" + status + '\'' +
                ", createDate=" + createDate +
                '}';
    }
}
