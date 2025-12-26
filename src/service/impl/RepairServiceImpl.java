package service.impl;

import dao.RepairDao;
import dao.impl.RepairDaoImpl;
import model.Repair;
import service.RepairService;

import java.util.List;

/**
 * 报修业务实现（委托 DAO）
 */
public class RepairServiceImpl implements RepairService {

    private final RepairDao repairDao = new RepairDaoImpl();

    @Override
    public List<Repair> listByStudent(String studentId) {
        try {
            return repairDao.findByStudentId(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public boolean addRepair(Repair repair) {
        try {
            return repairDao.addRepair(repair);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(String id) {
        try {
            return repairDao.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
