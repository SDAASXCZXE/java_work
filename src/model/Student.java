
package model;

/**
 * 学生实体类
 */
public class Student {
    private int id;
    private String name;
    private String gender;
    private int roomId;

    public Student() {}  //无参构造器

    public Student(int id, String name, String gender, int roomId) {  //有参数构造器
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.roomId = roomId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
}
