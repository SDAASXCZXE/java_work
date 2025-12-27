package dao;

import model.Room;

import java.util.List;

public interface RoomDao {
    /**
     * 查询所有宿舍记录
     * @return 宿舍列表
     */
    List<Room> findAll();

    /**
     * 插入一条宿舍记录
     * @param room 要插入的 Room 对象
     * @return 成功返回 true，失败返回 false
     */
    boolean insert(Room room);

    /**
     * 根据 roomNumber 更新宿舍记录（整体更新）
     * @param room 包含更新后数据的 Room 对象
     * @return 成功返回 true，失败返回 false
     */
    boolean update(Room room);

    /**
     * 根据楼栋与房间号联合删除宿舍记录，避免仅按房间号误删其他楼栋的同号房间
     * @param building 楼栋，如 "A栋"
     * @param roomNumber 房间号
     * @return 成功返回 true，失败返回 false
     */
    boolean deleteByBuildingAndRoom(String building, String roomNumber);

    /**
     * 更新入住/退宿后的已住人数、空余床位与状态
     * @param roomNumber 房间号
     * @param occupied 更新后的已住人数
     * @param available 更新后的空余床位
     * @param status 更新后的状态文本
     * @return 成功返回 true，失败返回 false
     */
    boolean updateOccupancy(String roomNumber, int occupied, int available, String status);

    /**
     * 检查房间号是否已存在
     * @param roomNumber 房间号
     * @return 如果房间号存在，返回 true；否则返回 false
     */
    boolean existsByRoomNumber(String roomNumber);
}
