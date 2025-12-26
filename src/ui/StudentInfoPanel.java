package ui;

import model.Student;
import javax.swing.*;
import java.awt.*;

public class StudentInfoPanel extends JPanel {
    public StudentInfoPanel(Student student) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("基本信息"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        String[][] info = {
                {"学号：", student.getSno()},
                {"姓名：", student.getName()},
                {"性别：", student.getGender()},
                {"班级：", student.getClazz()},
                {"专业：", student.getMajor()},
                {"宿舍：", student.getBuilding() + " " + student.getRoomNumber()}
        };

        for (int i = 0; i < info.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            add(new JLabel(info[i][0]), gbc);
            gbc.gridx = 1;
            add(new JLabel(info[i][1]), gbc);
        }
    }
}