package controller;

import dao.RoomDao;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import model.RoomData;
import view.RoomDetailsExpand;

//Opens RoomDetailsExpand as a modal dialog.Collects form data and saves a new room to DB.
 
public class RoomDetailsExpandController {

    private final RoomDao roomDao = new RoomDao();
    private final RoomDetailsExpand view;
    private final RoomDetailsController parent;
    private JDialog dialog;

    public RoomDetailsExpandController(RoomDetailsExpand view, RoomDetailsController parent) {
        this.view   = view;
        this.parent = parent;

        view.AddRoomListener(e -> addRoom());
        view.CancelListener(e  -> close());
    }

    private void addRoom() {
        String roomNum = view.getRoomNumber();
        String block   = view.getBlock();
        String floor   = view.getFloor();
        String type    = view.getRoomType();
        String feeText = view.getFee();


        if (roomNum.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a room number.");
            return;
        }
        if (floor.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a floor.");
            return;
        }
        if(feeText.isEmpty()){
            JOptionPane.showMessageDialog(dialog,
                "Please enter room fee.");
            return;
        }

        double fee;

        try{
            fee = Double.parseDouble(feeText);
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(dialog,
                "Fee must be a number.");
            return;
        }
        int capacity = switch (type) {
            case "Double" -> 2;
            case "Triple" -> 3;
            default       -> 1;
        };

        String facilities = String.join(", ", view.getFacilities());

        RoomData r = new RoomData();
        r.setRoomNumber(roomNum);
        r.setBlock(block);
        r.setFloor(floor);
        r.setType(type);
        r.setCapacity(capacity);
        r.setFacilities(facilities);
        r.setFee(fee);

        boolean saved = roomDao.addRoom(r);
        if (saved) {
            JOptionPane.showMessageDialog(dialog,
                    "Room " + roomNum + " added successfully!",
                    "Room Added", JOptionPane.INFORMATION_MESSAGE);
            close();
            parent.refresh();
        } else {
            JOptionPane.showMessageDialog(dialog,
                    "Failed to add room. Room number may already exist.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void open(JFrame parentFrame) {
        dialog = new JDialog(parentFrame, "Add New Room", true);
        dialog.setContentPane(view.getContentPane());
        dialog.setSize(420, 425);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) dialog.dispose();
    }
}