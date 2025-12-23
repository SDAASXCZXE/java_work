package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 访客管理类
 */
public class VisitorManager {
    private static final String VISITOR_DATA_FILE = "visitors.dat";
    private List<Visitor> visitors;
    private static VisitorManager instance;

    private VisitorManager() {
        visitors = new ArrayList<>();
        loadVisitors();
    }

    public static synchronized VisitorManager getInstance() {
        if (instance == null) {
            instance = new VisitorManager();
        }
        return instance;
    }

    /**
     * 登记新访客
     */
    public boolean registerVisitor(Visitor visitor) {
        // 生成访客ID
        String visitorId = "V" + System.currentTimeMillis();
        visitor.setVisitorId(visitorId);

        visitors.add(visitor);
        return saveVisitors();
    }

    /**
     * 更新访客信息
     */
    public boolean updateVisitor(Visitor updatedVisitor) {
        for (int i = 0; i < visitors.size(); i++) {
            if (visitors.get(i).getVisitorId().equals(updatedVisitor.getVisitorId())) {
                visitors.set(i, updatedVisitor);
                return saveVisitors();
            }
        }
        return false;
    }

    /**
     * 删除访客记录
     */
    public boolean deleteVisitor(String visitorId) {
        Visitor visitor = getVisitorById(visitorId);
        if (visitor != null) {
            visitors.remove(visitor);
            return saveVisitors();
        }
        return false;
    }

    /**
     * 根据ID获取访客
     */
    public Visitor getVisitorById(String visitorId) {
        for (Visitor visitor : visitors) {
            if (visitor.getVisitorId().equals(visitorId)) {
                return visitor;
            }
        }
        return null;
    }

    /**
     * 根据姓名搜索访客
     */
    public List<Visitor> searchVisitorByName(String name) {
        List<Visitor> results = new ArrayList<>();
        String searchName = name.toLowerCase();

        for (Visitor visitor : visitors) {
            if (visitor.getVisitorName().toLowerCase().contains(searchName)) {
                results.add(visitor);
            }
        }
        return results;
    }

    /**
     * 根据证件号搜索访客
     */
    public List<Visitor> searchVisitorByIdNumber(String idNumber) {
        List<Visitor> results = new ArrayList<>();
        String searchId = idNumber.toLowerCase();

        for (Visitor visitor : visitors) {
            if (visitor.getIdNumber().toLowerCase().contains(searchId)) {
                results.add(visitor);
            }
        }
        return results;
    }

    /**
     * 获取今日访客
     */
    public List<Visitor> getTodayVisitors() {
        List<Visitor> results = new ArrayList<>();
        Date today = new Date();
        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(today);

        for (Visitor visitor : visitors) {
            String visitDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(visitor.getVisitTime());
            if (visitDate.equals(todayStr)) {
                results.add(visitor);
            }
        }
        return results;
    }

    /**
     * 获取当前在楼访客
     */
    public List<Visitor> getCurrentVisitors() {
        List<Visitor> results = new ArrayList<>();

        for (Visitor visitor : visitors) {
            if (visitor.isStillInBuilding()) {
                results.add(visitor);
            }
        }
        return results;
    }

    /**
     * 获取所有访客
     */
    public List<Visitor> getAllVisitors() {
        return new ArrayList<>(visitors);
    }

    /**
     * 获取指定宿舍的访客记录
     */
    public List<Visitor> getVisitorsByDormitory(String dormitory) {
        List<Visitor> results = new ArrayList<>();

        for (Visitor visitor : visitors) {
            if (visitor.getDormitory().equals(dormitory)) {
                results.add(visitor);
            }
        }
        return results;
    }

    /**
     * 获取指定学生的访客记录
     */
    public List<Visitor> getVisitorsByStudent(String studentName) {
        List<Visitor> results = new ArrayList<>();

        for (Visitor visitor : visitors) {
            if (visitor.getVisitedStudent().equals(studentName)) {
                results.add(visitor);
            }
        }
        return results;
    }

    /**
     * 统计今日访客数量
     */
    public int countTodayVisitors() {
        return getTodayVisitors().size();
    }

    /**
     * 统计本周访客数量
     */
    public int countThisWeekVisitors() {
        Date now = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        Date startOfWeek = cal.getTime();

        int count = 0;
        for (Visitor visitor : visitors) {
            if (visitor.getVisitTime().after(startOfWeek)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计本月访客数量
     */
    public int countThisMonthVisitors() {
        Date now = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();

        int count = 0;
        for (Visitor visitor : visitors) {
            if (visitor.getVisitTime().after(startOfMonth)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计当前在楼访客数量
     */
    public int countCurrentVisitors() {
        return getCurrentVisitors().size();
    }

    /**
     * 从文件加载访客数据
     */
    @SuppressWarnings("unchecked")
    private void loadVisitors() {
        File file = new File(VISITOR_DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                visitors = (List<Visitor>) ois.readObject();
            } catch (Exception e) {
                System.err.println("加载访客数据失败: " + e.getMessage());
                visitors = new ArrayList<>();
            }
        }
    }

    /**
     * 保存访客数据到文件
     */
    private boolean saveVisitors() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(VISITOR_DATA_FILE))) {
            oos.writeObject(visitors);
            return true;
        } catch (Exception e) {
            System.err.println("保存访客数据失败: " + e.getMessage());
            return false;
        }
    }
}