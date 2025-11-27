
package ui;

import model.Room;
import service.RoomService;
import service.impl.RoomServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {

    private RoomService service = new RoomServiceImpl();
    private JTextArea area;

    public RoomPanel() {
        setLayout(new BorderLayout());

        JButton load = new JButton("加载宿舍信息");
        area = new JTextArea();

        load.addActionListener(e -> loadRooms());

        add(load, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }

    private void loadRooms() {
        List<Room> list = service.findAll();
        area.setText("");

        for (Room r : list) {
            area.append(r.getId() + " - " + r.getBuilding() + " - capacity:" + r.getCapacity() + "\n");
        }
    }
}
