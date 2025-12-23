package service;

import model.Visitor;
import java.util.List;

public interface VisitorService {
    List<Visitor> listAll();
    boolean addVisitor(Visitor v);
    boolean removeById(String id);
}

