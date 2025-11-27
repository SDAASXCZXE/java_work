
package model;

/**
 * 宿舍房间实体类
 */
public class Room {
    private int id;
    private String building;
    private int capacity;

    public Room() {}

    public Room(int id, String building, int capacity) {
        this.id = id;
        this.building = building;
        this.capacity = capacity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
