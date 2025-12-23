package service;

import model.Room;
import java.util.List;

public interface RoomService {
    /**
     * 查找所有宿舍
     * @return 宿舍列表
     */
    List<Room> findAll();

    /**
     * 新增宿舍
     * @param room 要新增的宿舍对象
     * @return 成功返回 true
     */
    boolean add(Room room);

    /**
     * 更新宿舍信息
     * @param room 更新后的宿舍对象
     * @return 成功返回 true
     */
    boolean update(Room room);

    /**
     * 根据房间号删除宿舍
     * @param roomNumber 房间号
     * @return 成功返回 true
     */
    boolean deleteByRoomNumber(String roomNumber);

    /**
     * 更新入住/退宿信息（仅改变人数和状态）
     */
    boolean updateOccupancy(String roomNumber, int occupied, int available, String status);

    /**
     * 检查房间号是否存在
     */
    boolean existsByRoomNumber(String roomNumber);
}
