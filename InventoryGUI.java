package inventory.com;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

class Item {
    String id;
    String name;
    int quantity;
    double price;

    public Item(String id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public double getTotalValue() {
        return quantity * price;
    }

    @Override
    public String toString() {
        return id + "," + name + "," + quantity + "," + price;
    }
}

public class InventoryGUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<Item> inventory;
    private boolean isAdmin;

 
    private final String FILE_NAME = "inventory_data.txt";

    public InventoryGUI(boolean isAdmin) {
        this.isAdmin = isAdmin;
        inventory = new ArrayList<>();
        initialize();
        loadFromFile();
    }

    private void initialize() {
    	
        frame = new JFrame("INVENTORY MANAGEMENT SYSTEM");
        
        frame.setSize(800, 500);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        String[] columns = {"ID", "NAME", "QUANTITY", "PRICE", "TOTAL VALUE"};
        tableModel = new DefaultTableModel(columns, 0);
        
        table = new JTable(tableModel);
        
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("ADD ITEM");
        JButton removeButton = new JButton("REMOVE ITEM");
        JButton updateButton = new JButton("UPDDATE ITEM");
        JButton lowStockButton = new JButton("LOW STOCK REPORT");
        JButton totalValueButton = new JButton("TOTAL VALUE");

        if (isAdmin) {
            buttonPanel.add(addButton);
            buttonPanel.add(removeButton);
            buttonPanel.add(updateButton);
            
        }
            else {
            JButton sellButton = new JButton("SELL ITEM");
            buttonPanel.add(sellButton);

            sellButton.addActionListener(e -> sellItem());
        }

        buttonPanel.add(lowStockButton);
        buttonPanel.add(totalValueButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

       
        addButton.addActionListener(e -> addItem());
        removeButton.addActionListener(e -> removeItem());
        updateButton.addActionListener(e -> updateItem());
        lowStockButton.addActionListener(e -> lowStockReport());
        totalValueButton.addActionListener(e -> totalValueReport());

        frame.setVisible(true);
    }

    
    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Item item : inventory) {
                writer.println(item.toString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "ERROR SAVING DATA!");
        }
    }

    
    private void loadFromFile() {
    	
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 4) {
                	
                    String id = parts[0];
                    
                    String name = parts[1];
                    
                    int quantity = Integer.parseInt(parts[2]);
                    
                    double price = Double.parseDouble(parts[3]);

                    Item item = new Item(id, name, quantity, price);
                    
                    inventory.add(item);
                    
                    tableModel.addRow(new Object[]{
                            id, name, quantity, price, item.getTotalValue()
                    }
                    );
                }
            }
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "ERROR LOADING DATA!");
        }
    }

    
    private void addItem() {
        try {
            String id = JOptionPane.showInputDialog("ENTER ITEM ID:");
            
            String name = JOptionPane.showInputDialog("ENTER ITEM NAME:");
            
            int quantity = Integer.parseInt(JOptionPane.showInputDialog("ENTER ITEM QUANTITY:"));
            
            double price = Double.parseDouble(JOptionPane.showInputDialog("ENTER PRICE:"));

            Item item = new Item(id, name, quantity, price);
            inventory.add(item);

            tableModel.addRow(new Object[]{
                    id, name, quantity, price, item.getTotalValue()
            });

            saveToFile(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "INVALID INPUT!");
        }
    }

    
    private void removeItem() {
        int row = table.getSelectedRow();
        
        if (row >= 0) {
            inventory.remove(row);
            tableModel.removeRow(row);
            saveToFile(); 
        } 
        else {
            JOptionPane.showMessageDialog(frame, "SELECT A ROW!");
        }
    }

    private void updateItem() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            try {
                Item item = inventory.get(row);

                int newquantity = Integer.parseInt(
                        JOptionPane.showInputDialog("NEW QUANTITY:", item.quantity)
                );
                double newPrice = Double.parseDouble(
                        JOptionPane.showInputDialog("NEW PRICE:", item.price)
                );

                item.quantity = newquantity;
                item.price = newPrice;

                tableModel.setValueAt(newquantity, row, 2);
                tableModel.setValueAt(newPrice, row, 3);
                tableModel.setValueAt(item.getTotalValue(), row, 4);

                saveToFile(); 
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "INVALID INPUT!");
            }
        }
        else {
            JOptionPane.showMessageDialog(frame, "SELECT A ROW!");
        }
    }

   
    private void sellItem() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            try {
                Item item = inventory.get(row);

                int sellquantity = Integer.parseInt(
                        JOptionPane.showInputDialog("ENTER QUANTITY TO SELL:", 1)
                );

                if (sellquantity <= item.quantity) {
                    item.quantity -= sellquantity;

                    tableModel.setValueAt(item.quantity, row, 2);
                    tableModel.setValueAt(item.getTotalValue(), row, 4);

                    saveToFile();

                    JOptionPane.showMessageDialog(frame, "SALE SUCCESSFULY!");
                }
                else {
                    JOptionPane.showMessageDialog(frame, "NOT ENOUGH STOCK!");
                }
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "INVALID INPUT!");
            }
        }
        else {
            JOptionPane.showMessageDialog(frame, "SELECT A ROW!");
        }
    }

    
    private void lowStockReport() {
        int threshold = Integer.parseInt(
                JOptionPane.showInputDialog("ENTER THRESHOD:")
        );

        StringBuilder sb = new StringBuilder("LOW STOCK:\n");

        for (Item item : inventory) {
            if (item.quantity <= threshold){
                sb.append(item.id)
                  .append(" | ")
                  .append(item.name)
                  .append(" | QUANTITY: ")
                  .append(item.quantity)
                  .append("\n");
            }
        }

        JOptionPane.showMessageDialog(frame, sb.toString());
    }


    private void totalValueReport() {
        double total = 0;
        for (Item item : inventory) {
            total += item.getTotalValue();
        }

        JOptionPane.showMessageDialog(frame, "TOTAL VALUE: $" + total);
    }
    
    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("KINDLY \n ENTER YOUR NAME: \n (ADMIN/STAFF)");
        String password = JOptionPane.showInputDialog("KINDLY \n  ENTER PASSWORD PLEASE:");

        boolean isAdmin = username.equals("ADMIN") && password.equals("ADMIN123");
        boolean isStaff = username.equals("STAFF") && password.equals("STAFF123");

        if (isAdmin || isStaff) {
            new InventoryGUI(isAdmin);
        }
        else {
            JOptionPane.showMessageDialog(null, "INVALID LOGIN! \n PLEASE TRY AGAIN(* *)");
            System.exit(0);
        }
    }
}