package model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤管理类
 */
public class AttendanceManager {
    private static final String ATTENDANCE_DATA_FILE = "attendance.dat";
    private List<Attendance> attendances;
    private static AttendanceManager instance;

    private AttendanceManager() {
        attendances = new ArrayList<>();
        loadAttendances();
    }

    public static synchronized AttendanceManager getInstance() {
        if (instance == null) {
            instance = new AttendanceManager();
        }
        return instance;
    }

    /**
     * 添加考勤记录
     */
    public boolean addAttendance(Attendance attendance) {
        // 设置ID
        attendance.setId(attendances.size() + 1);
        attendances.add(attendance);
        return saveAttendances();
    }

    /**
     * 批量添加考勤记录
     */
    public boolean addAttendances(List<Attendance> attendanceList) {
        for (Attendance attendance : attendanceList) {
            attendance.setId(attendances.size() + 1);
            attendances.add(attendance);
        }
        return saveAttendances();
    }

    /**
     * 更新考勤记录
     */
    public boolean updateAttendance(Attendance updatedAttendance) {
        for (int i = 0; i < attendances.size(); i++) {
            if (attendances.get(i).getId() == updatedAttendance.getId()) {
                attendances.set(i, updatedAttendance);
                return saveAttendances();
            }
        }
        return false;
    }

    /**
     * 删除考勤记录
     */
    public boolean deleteAttendance(int id) {
        Attendance attendance = getAttendanceById(id);
        if (attendance != null) {
            attendances.remove(attendance);
            // 重新编号
            renumberAttendances();
            return saveAttendances();
        }
        return false;
    }

    /**
     * 根据ID获取考勤记录
     */
    public Attendance getAttendanceById(int id) {
        for (Attendance attendance : attendances) {
            if (attendance.getId() == id) {
                return attendance;
            }
        }
        return null;
    }

    /**
     * 根据学号搜索考勤记录
     */
    public List<Attendance> searchByStudentId(String studentId) {
        return attendances.stream()
                .filter(a -> a.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    /**
     * 根据学生姓名搜索考勤记录
     */
    public List<Attendance> searchByStudentName(String name) {
        return attendances.stream()
                .filter(a -> a.getStudentName().contains(name))
                .collect(Collectors.toList());
    }

    /**
     * 获取今日考勤记录
     */
    public List<Attendance> getTodayAttendances() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());

        return attendances.stream()
                .filter(a -> {
                    String dateStr = sdf.format(a.getAttendanceDate());
                    return dateStr.equals(today);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取指定日期的考勤记录
     */
    public List<Attendance> getAttendancesByDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return attendances.stream()
                .filter(a -> {
                    String dateStr = sdf.format(a.getAttendanceDate());
                    return dateStr.equals(date);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取指定宿舍楼的考勤记录
     */
    public List<Attendance> getAttendancesByBuilding(String building) {
        return attendances.stream()
                .filter(a -> a.getBuilding().equals(building))
                .collect(Collectors.toList());
    }

    /**
     * 获取指定状态的考勤记录
     */
    public List<Attendance> getAttendancesByStatus(AttendanceStatus status) {
        return attendances.stream()
                .filter(a -> a.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 获取异常考勤记录
     */
    public List<Attendance> getAbnormalAttendances() {
        return attendances.stream()
                .filter(Attendance::isAbnormal)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有考勤记录
     */
    public List<Attendance> getAllAttendances() {
        return new ArrayList<>(attendances);
    }

    /**
     * 统计今日考勤数据
     */
    public Map<String, Integer> getTodayStatistics() {
        List<Attendance> todayAttendances = getTodayAttendances();

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", todayAttendances.size());
        stats.put("normal", (int) todayAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.NORMAL).count());
        stats.put("late", (int) todayAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE).count());
        stats.put("absent", (int) todayAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count());
        stats.put("leave", (int) todayAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LEAVE).count());

        return stats;
    }

    /**
     * 统计本月考勤数据
     */
    public Map<String, Object> getMonthStatistics() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String currentMonth = sdf.format(new Date());

        List<Attendance> monthAttendances = attendances.stream()
                .filter(a -> {
                    String month = sdf.format(a.getAttendanceDate());
                    return month.equals(currentMonth);
                })
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", monthAttendances.size());
        stats.put("normal", (int) monthAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.NORMAL).count());
        stats.put("late", (int) monthAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE).count());
        stats.put("absent", (int) monthAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count());
        stats.put("leave", (int) monthAttendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LEAVE).count());

        // 计算出勤率
        if (monthAttendances.size() > 0) {
            int normalCount = (int) stats.get("normal");
            double attendanceRate = (normalCount * 100.0) / monthAttendances.size();
            stats.put("attendanceRate", String.format("%.1f%%", attendanceRate));
        } else {
            stats.put("attendanceRate", "0%");
        }

        return stats;
    }

    /**
     * 获取晚归高峰时段
     */
    public String getLatePeakTime() {
        List<Attendance> lateAttendances = getAttendancesByStatus(AttendanceStatus.LATE);

        if (lateAttendances.isEmpty()) {
            return "无晚归记录";
        }

        // 统计各时段的晚归人数
        Map<Integer, Integer> hourCount = new HashMap<>();
        for (Attendance a : lateAttendances) {
            int hour = a.getCheckInHour();
            hourCount.put(hour, hourCount.getOrDefault(hour, 0) + 1);
        }

        // 找到人数最多的时段
        int maxHour = Collections.max(hourCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        int count = hourCount.get(maxHour);

        return String.format("%d:00-%d:59 (%d人)", maxHour, maxHour, count);
    }

    /**
     * 获取重点关注宿舍
     */
    public List<String> getFocusDormitories() {
        // 统计各宿舍的异常次数
        Map<String, Integer> dormAbnormalCount = new HashMap<>();

        for (Attendance a : attendances) {
            if (a.isAbnormal()) {
                String dorm = a.getDormitory();
                dormAbnormalCount.put(dorm, dormAbnormalCount.getOrDefault(dorm, 0) + 1);
            }
        }

        // 按异常次数排序，取前3个
        return dormAbnormalCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 重新编号考勤记录
     */
    private void renumberAttendances() {
        for (int i = 0; i < attendances.size(); i++) {
            attendances.get(i).setId(i + 1);
        }
    }

    /**
     * 从文件加载考勤数据
     */
    @SuppressWarnings("unchecked")
    private void loadAttendances() {
        File file = new File(ATTENDANCE_DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                attendances = (List<Attendance>) ois.readObject();
            } catch (Exception e) {
                System.err.println("加载考勤数据失败: " + e.getMessage());
                attendances = new ArrayList<>();
            }
        }
    }

    /**
     * 保存考勤数据到文件
     */
    private boolean saveAttendances() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ATTENDANCE_DATA_FILE))) {
            oos.writeObject(attendances);
            return true;
        } catch (Exception e) {
            System.err.println("保存考勤数据失败: " + e.getMessage());
            return false;
        }
    }
}