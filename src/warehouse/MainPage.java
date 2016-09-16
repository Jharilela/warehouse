/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package warehouse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.*;

/**
 *
 * @author J
 */
public class MainPage extends javax.swing.JFrame {

    /**
     * Creates new form MainPage
     */
    ArrayList<String> tbNames = new ArrayList();
    boolean editable = Warehouse.editable;
    Vector selectedCells = new Vector<int[]>();

    public void setEditable()
    {
        AddButton.setEnabled(editable);
        EditButton.setEnabled(editable);
        DeleteButton.setEnabled(editable);
        SendButton.setEnabled(editable);
        if(editable)
            User.setText("Admin");
        else
            User.setText("Guest");
    }
    
    public MainPage() {
        initComponents();
        setEditable();
        tableNames();
        database("GOODS");
    }


    
    public void tableNames() {
        String url = "jdbc:derby://localhost:1527/warehouseDb";
        String userid = "root";
        String password = "Password";

        try (Connection connection = DriverManager.getConnection(url, userid, password)) {
            DatabaseMetaData md = connection.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = md.getTables(null, null, "%", types);
            while (rs.next()) {
                tbNames.add(rs.getString("TABLE_NAME"));
            }

            //Collections.reverse(tbNames);
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (String item : tbNames) {
                model.addElement(item);
            }
            ComboBox.setModel(model);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }

    public void database(String tbName) {
        ArrayList columnNames = new ArrayList();
        ArrayList data = new ArrayList();

        //  Connect to an MySQL Database, run query, get result set
        String url = "jdbc:derby://localhost:1527/warehouseDb";
        String userid = "root";
        String password = "Password";
        String sql = "SELECT * FROM " + tbName;
        
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DefaultTableModel model = new DefaultTableModel(0, 0) {

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Java SE 7 has try-with-resources
        // This will ensure that the sql objects are closed when the program 
        // is finished with them
        try (
        Connection connection = DriverManager.getConnection(url, userid, password);
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            model.setColumnCount(columns);
            //  Get column names
            for (int i = 1; i <= columns; i++) {
                columnNames.add(md.getColumnName(i));
            }

            //  Get row data
            while (rs.next()) {
                ArrayList row = new ArrayList(columns);
                for (int i = 1; i <= columns; i++) {
                    row.add(rs.getObject(i));

                }

                data.add(row);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Create Vectors and copy over elements from ArrayLists to them
        // Vector is deprecated but I am using them in this example to keep 
        // things simple - the best practice would be to create a custom defined
        // class which inherits from the AbstractTableModel class
        Vector columnNamesVector = new Vector();
        Vector dataVector = new Vector();

        for (int i = 0; i < data.size(); i++) {
            ArrayList subArray = (ArrayList) data.get(i);
            Vector subVector = new Vector();
            for (int j = 0; j < subArray.size(); j++) {
                subVector.add(subArray.get(j));

            }
            model.addRow(subVector);
            dataVector.add(subVector);
        }

        for (int i = 0; i < columnNames.size(); i++) {
            columnNamesVector.add(columnNames.get(i));
        }

        //  Create table with database data    
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        Table.setRowSorter(sorter);
        Table.setColumnSelectionAllowed(false);
        Table.setModel(model);
        JTableHeader th = Table.getTableHeader();
        TableColumnModel tcm = th.getColumnModel();
        for (int i = 0; i < columnNames.size(); i++) {
            tcm.getColumn(i).setHeaderValue(columnNames.get(i));
        }
        th.repaint();
    }

    public ArrayList runSQL(String querry) {
        ArrayList<String> result = new ArrayList();
        String url = "jdbc:derby://localhost:1527/warehouseDb";
        String userid = "root";
        String password = "Password";

        try (Connection connection = DriverManager.getConnection(url, userid, password)) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(querry);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return result;
    }

    public void execute(String querry) {
        String url = "jdbc:derby://localhost:1527/warehouseDb";
        String userid = "root";
        String password = "Password";

        try (Connection connection = DriverManager.getConnection(url, userid, password)) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(querry);
            System.out.println("Executed");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public String AddMenu() {
        JComboBox itemName = new JComboBox();
        JTextField colorCode = new JTextField();
        JTextField rollLength = new JTextField();
        JTextArea comment = new JTextArea();

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ArrayList<String> b = runSQL("SELECT DISTINCT ITEM_NAME FROM GOODS");
        Collections.reverse(b);
        for (String goods : b) {
            model.addElement(goods);
        }
        model.addElement("Add new");
        itemName.setModel(model);

        final JComponent[] inputs = new JComponent[]{
            new JLabel("Item Name"),
            itemName,
            new JLabel("Color Code"),
            colorCode,
            new JLabel("Roll length"),
            rollLength,
            new JLabel("Comment"),
            comment
        };

        JTextField NewItemName = new JTextField(20);

        itemName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = itemName.getSelectedItem();
                if (obj.toString().equals("Add new")) {

                    final JComponent[] inputs = new JComponent[]{
                        new JLabel("Item Name"),
                        itemName,
                        new JLabel("New Item Name"),
                        NewItemName,
                        new JLabel("Color Code"),
                        colorCode,
                        new JLabel("Roll length"),
                        rollLength,
                        new JLabel("Comment"),
                        comment
                    };
                    JOptionPane.getRootFrame().dispose();
                    JOptionPane.showMessageDialog(null, inputs, "Add to database", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });

        JOptionPane.showMessageDialog(null, inputs, "Add to database", JOptionPane.PLAIN_MESSAGE);

        if (JOptionPane.CLOSED_OPTION == 1) {
            System.out.println("closed");
        }
        if(colorCode.getText().equals("") && rollLength.getText().equals(""))
        {
            return "null";
        }
        else if (colorCode.getText().matches("\\d+") == false) {
            JOptionPane.showMessageDialog(null, "Please enter the Color Code", "Add to database", JOptionPane.ERROR_MESSAGE);
            return AddMenu();
        } else if (rollLength.getText().matches("[0-9.]*") == false) {
            JOptionPane.showMessageDialog(null, "Please enter a valid Roll length", "Add to database", JOptionPane.ERROR_MESSAGE);

            return AddMenu();
        } 
        else {
            String comm = "";
            if (comment.getText().isEmpty()) {
                comm = null;
            } else {
                comm = "'" + comment.getText().trim() + "'";
            }
            Object object = itemName.getSelectedItem();
            String itemNameFinal = "";
            if (NewItemName.getText().isEmpty() == false && object.toString().equals("Add new")) {
                itemNameFinal = NewItemName.getText();
            } else {
                itemNameFinal = itemName.getSelectedItem().toString();
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date date = new java.util.Date();
            return ("'"
                    + itemNameFinal + "', '"
                    + colorCode.getText() + "', "
                    + Double.parseDouble(rollLength.getText()) + ", '"
                    + dateFormat.format(date) + "', "
                    + comm);
        }
    }

    public Object[] EditMenu()
    {
        JComboBox itemName = new JComboBox();
        JTextField colorCode = new JTextField();
        JTextField rollLength = new JTextField();
        JTextField date = new JTextField();
        JTextArea comment = new JTextArea();

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        ArrayList<String> b = runSQL("SELECT DISTINCT ITEM_NAME FROM GOODS");
        Collections.reverse(b);
        for (String goods : b) {
            model.addElement(goods);
        }
        model.addElement("Add new");
        itemName.setModel(model);
        itemName.setSelectedItem(Table.getValueAt(Table.getSelectedRow(), 0));
        colorCode.setText(Table.getValueAt(Table.getSelectedRow(), 1).toString());
        rollLength.setText(Table.getValueAt(Table.getSelectedRow(), 2).toString());
        date.setText(Table.getValueAt(Table.getSelectedRow(), 3).toString());
        date.setEditable(false);
        date.setToolTipText("Not editable");
        String comm1="";
            if(Table.getValueAt(Table.getSelectedRow(), 4)==null)
                comm1="";
            else
                comm1 = Table.getValueAt(Table.getSelectedRow(), 4).toString();
        
        comment.setText(comm1);

        final JComponent[] inputs = new JComponent[]{
            new JLabel("Item Name"),
            itemName,
            new JLabel("Color Code"),
            colorCode,
            new JLabel("Roll length"),
            rollLength,
            new JLabel("Date"),
            date,
            new JLabel("Comment"),
            comment
        };

        JTextField NewItemName = new JTextField(20);
        JTextField ret = new JTextField("");
        JTextField res = new JTextField("");
        
        itemName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = itemName.getSelectedItem();
                if (obj.toString().equals("Add new")) {

                    final JComponent[] inputs = new JComponent[]{
                        new JLabel("Item Name"),
                        itemName,
                        new JLabel("New Item Name"),
                        NewItemName,
                        new JLabel("Color Code"),
                        colorCode,
                        new JLabel("Roll length"),
                        rollLength,
                        new JLabel("Date"),
                        date,
                        new JLabel("Comment"),
                        comment
                    };
                    JOptionPane.getRootFrame().dispose();
                    res.setText("exit");
                    int res1 = JOptionPane.showConfirmDialog(null, inputs, "Add to database", JOptionPane.OK_CANCEL_OPTION);
                    if (res1!=0) {
            ret.setText("return");
                    }
                }
            }
        });
        int d = JOptionPane.showConfirmDialog(null, inputs, "Add to database", JOptionPane.OK_CANCEL_OPTION);
        
        if (ret.getText().equals("return")||d>0) {
            System.out.println("cancel edit");
            Object [] result = new Object[5];
            result[0]="null";
            result[1]="null";
            result[2]="null";
            result[3]="null";
            result[4]="null";
            return (result);
        }
        else if (colorCode.getText().matches("\\d+") == false) {
            JOptionPane.showMessageDialog(null, "Please enter the Color Code", "Add to database", JOptionPane.ERROR_MESSAGE);
            return EditMenu();
        } else if (rollLength.getText().trim().matches("[0-9.]*") == false) {
            JOptionPane.showMessageDialog(null, "Please enter a valid Roll length", "Add to database", JOptionPane.ERROR_MESSAGE);
            return EditMenu();
        } 
        else {
            String comm = "";
            if (comment.getText().isEmpty()) {
                comm = null;
            } 
            else {
                comm = "'" + comment.getText().trim() + "'";
            }
            Object object = itemName.getSelectedItem();
            String itemNameFinal = "";
            
            if (NewItemName.getText().isEmpty() == false && object.toString().equals("Add new")) {
                itemNameFinal = NewItemName.getText();
            } else {
                itemNameFinal = itemName.getSelectedItem().toString();
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Object [] result = new Object[5];
            result[0]=itemNameFinal;
            result[1]=colorCode.getText();
            result[2]=Double.parseDouble(rollLength.getText());
            result[3]=dateFormat.format(Table.getValueAt(Table.getSelectedRow(), 3));
            result[4]=comm;
            return (result);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        top = new javax.swing.JPanel();
        AddButton = new javax.swing.JButton();
        EditButton = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();
        SendButton = new javax.swing.JButton();
        ComboBox = new javax.swing.JComboBox();
        User = new javax.swing.JLabel();
        topBg = new javax.swing.JLabel();
        bottom = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        bottomBg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1280, 800));
        getContentPane().setLayout(null);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1);
        jPanel1.setBounds(0, 0, 0, 0);

        top.setLayout(null);

        AddButton.setBackground(new java.awt.Color(0, 0, 0));
        AddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warehouse/add.png"))); // NOI18N
        AddButton.setToolTipText("Add");
        AddButton.setBorder(null);
        AddButton.setBorderPainted(false);
        AddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });
        top.add(AddButton);
        AddButton.setBounds(10, 30, 60, 60);

        EditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warehouse/edit.png"))); // NOI18N
        EditButton.setToolTipText("Edit");
        EditButton.setBorderPainted(false);
        EditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditButtonActionPerformed(evt);
            }
        });
        top.add(EditButton);
        EditButton.setBounds(90, 30, 60, 60);

        DeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warehouse/delete.png"))); // NOI18N
        DeleteButton.setToolTipText("Delete");
        DeleteButton.setBorder(null);
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonActionPerformed(evt);
            }
        });
        top.add(DeleteButton);
        DeleteButton.setBounds(160, 30, 60, 60);

        SendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warehouse/send.png"))); // NOI18N
        SendButton.setToolTipText("Send");
        SendButton.setBorder(null);
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });
        top.add(SendButton);
        SendButton.setBounds(1120, 30, 130, 50);

        ComboBox.setMaximumRowCount(16);
        ComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ComboBox.setToolTipText("Chose a database");
        ComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComboBoxActionPerformed(evt);
            }
        });
        top.add(ComboBox);
        ComboBox.setBounds(566, 40, 170, 27);

        User.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        User.setForeground(new java.awt.Color(255, 255, 255));
        User.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        User.setText("User");
        top.add(User);
        User.setBounds(630, 10, 45, 16);

        topBg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warehouse/seamless-black-wood-texture-design-inspiration-2-1.jpg"))); // NOI18N
        top.add(topBg);
        topBg.setBounds(0, 0, 1280, 100);

        getContentPane().add(top);
        top.setBounds(0, 0, 1280, 100);

        bottom.setLayout(null);

        Table.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        Table.setColumnSelectionAllowed(true);
        Table.setRowHeight(20);
        Table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                TableMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(Table);
        Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jScrollPane2.setViewportView(jScrollPane1);

        bottom.add(jScrollPane2);
        jScrollPane2.setBounds(40, 30, 1190, 600);

        bottomBg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/warehouse/1seamless_wood_texture_32.jpg"))); // NOI18N
        bottom.add(bottomBg);
        bottomBg.setBounds(0, 0, 1280, 700);

        getContentPane().add(bottom);
        bottom.setBounds(0, 100, 1280, 700);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        String val = AddMenu();
        System.out.println(val);
        /*execute("INSERT INTO ROOT.GOODS (ITEM_NAME, COLOR_CODE, ROLL_LENGTH, ENTRY_DATE, COMMENT)" +
         "	VALUES ('Manis Interior', '1', 73.0, '2015-10-17', NULL)");*/
        if(val!="null"){
        execute("INSERT INTO ROOT.GOODS (ITEM_NAME, COLOR_CODE, ROLL_LENGTH, ENTRY_DATE, COMMENT)"
                + "	VALUES (" + val + ")");
        database(ComboBox.getSelectedItem().toString());
        }
    }//GEN-LAST:event_AddButtonActionPerformed

    private void EditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditButtonActionPerformed
        if(Table.getSelectedRow()!=-1 && Table.getSelectedColumn()!=-1)
        {
        Object [] val = EditMenu();
        
         String comm1 ="";
            if(val[4]==null)
                comm1="";
            else
                comm1 = ", COMMENT "+"= "+val[4]+"";
            
         String comm2 ="";
            if(Table.getValueAt(Table.getSelectedRow(), 4)==null)
                comm2="IS NULL";
            else
                comm2 = "= '"+Table.getValueAt(Table.getSelectedRow(), 4).toString()+"'";
        
        if(val[0]!="null"){
            System.out.println("UPDATE ROOT.GOODS "
                + "SET ITEM_NAME= '"+val[0]+ "' "+
                ", COLOR_CODE= '"+val[1]+ "' "+
                ", ROLL_LENGTH="+val[2]+
                ", ENTRY_DATE= '"+val[3]+"' "+
                comm1
                + " WHERE ITEM_NAME= '"+Table.getValueAt(Table.getSelectedRow(), 0)+"' "+
                ", COLOR_CODE= '"+Table.getValueAt(Table.getSelectedRow(), 1)+ "' "+
                ", ROLL_LENGTH= "+Table.getValueAt(Table.getSelectedRow(), 2)+
                ", ENTRY_DATE= '"+Table.getValueAt(Table.getSelectedRow(), 3)+"' "+
                ", COMMENT "+comm2
        );
            execute("UPDATE ROOT.GOODS "
                + "SET ITEM_NAME='"+val[0]+ "' "+
                ", COLOR_CODE='"+val[1]+ "' "+
                ", ROLL_LENGTH="+val[2]+
                ", ENTRY_DATE='"+val[3]+"' "+
                comm1
                + "WHERE  ITEM_NAME='"+Table.getValueAt(Table.getSelectedRow(), 0)+"' "+
                "AND COLOR_CODE='"+Table.getValueAt(Table.getSelectedRow(), 1)+ "' "+
                "AND ROLL_LENGTH="+Table.getValueAt(Table.getSelectedRow(), 2)+
                "AND ENTRY_DATE='"+Table.getValueAt(Table.getSelectedRow(), 3)+"' "+
                "AND COMMENT "+comm2
        );
            
        /*UPDATE Customers
SET ContactName='Alfred Schmidt', City='Hamburg'
WHERE CustomerName='Alfreds Futterkiste';*/
        database(ComboBox.getSelectedItem().toString());
        }
        }
        else
        {
            JOptionPane.showMessageDialog(null,"Please choose a row","Edit Menu",JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_EditButtonActionPerformed

    private void ComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboBoxActionPerformed
        JComboBox cb = (JComboBox) evt.getSource();
        String tbName = (String) cb.getSelectedItem();
        database(tbName);
    }//GEN-LAST:event_ComboBoxActionPerformed

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
            
            String fromDB =ComboBox.getSelectedItem().toString();
            String toDB ="";
            if(fromDB.equals(ComboBox.getItemAt(0)))
            toDB=ComboBox.getItemAt(1).toString();
            else
            toDB=ComboBox.getItemAt(0).toString();    
            System.out.println("from DB : "+fromDB);
            System.out.println("to DB : " +toDB);
            
        
                    
            if(Table.getSelectedRow()!=-1 && Table.getSelectedColumn()!=-1)
        {
            if(fromDB.equals("GOODS"))
            {
                String comm1 ="";
            if(Table.getValueAt(Table.getSelectedRow(), 4)==null)
                comm1="NULL";
            else
                comm1 = "'"+Table.getValueAt(Table.getSelectedRow(), 4).toString()+"'";
            
                String comm2 ="";
            if(Table.getValueAt(Table.getSelectedRow(), 4)==null)
                comm2="IS NULL";
            else
                comm2 = "= '"+Table.getValueAt(Table.getSelectedRow(), 4).toString()+"'";

            JTextField customerName = new JTextField(20);
            final JComponent[] inputs = new JComponent[]{
            new JLabel("Send to Customer \n "),
                new JLabel("ITEM_NAME = "   +Table.getValueAt(Table.getSelectedRow(), 0)+" \n "),
                new JLabel("COLOR_CODE = "   +Table.getValueAt(Table.getSelectedRow(), 1)+" \n "),
                new JLabel("ROLL_LENGTH = "   +Table.getValueAt(Table.getSelectedRow(), 2)+" \n"),
                new JLabel(" "),
                new JLabel("Customer Name : "),
                customerName
        };
            
            int out = JOptionPane.showConfirmDialog(null, inputs, "Add to database", JOptionPane.OK_CANCEL_OPTION);
                    
            if (out==0) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
java.util.Date date = new java.util.Date();
DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
java.util.Date time = new java.util.Date();


            execute("INSERT INTO ROOT.HISTORY "
                    + "(ITEM_NAME, COLOR_CODE, ROLL_LENGTH, ENTRY_DATE, SEND_DATE, SEND_TIME, CUSTOMER_NAME, COMMENTS)" +
         "	VALUES ("
                    + "'"   +Table.getValueAt(Table.getSelectedRow(), 0)+"'"
                    + ", '"   +Table.getValueAt(Table.getSelectedRow(), 1)+"'"
                    + ", "   +Table.getValueAt(Table.getSelectedRow(), 2)+""
                    + ", '"   +Table.getValueAt(Table.getSelectedRow(), 3)+"'"
                    + ", '"   +dateFormat.format(date)+"'"
                    + ", '"   +dateFormat2.format(time)+"'"
                    + ", '"   +customerName.getText()+"'"
                    + ", "+comm1+")"
            );
            /*INSERT INTO ROOT.HISTORY (ITEM_NAME, COLOR_CODE, ROLL_LENGTH, ENTRY_DATE, SEND_DATE, SEND_TIME, CUSTOMER_NAME, COMMENTS) 
	VALUES ('Dim Out 4', '2', 76.0, '2015-10-12', '2015-10-19', '00:52:19', 'Bill', NULL);*/

            execute("DELETE FROM ROOT.GOODS WHERE " +
             " ITEM_NAME = '"   +Table.getValueAt(Table.getSelectedRow(), 0)+"' AND " +
             " COLOR_CODE = '"   +Table.getValueAt(Table.getSelectedRow(), 1)+"' AND " +
             " ROLL_LENGTH = "   +Table.getValueAt(Table.getSelectedRow(), 2)+" AND " +
             " ENTRY_DATE = '"   +Table.getValueAt(Table.getSelectedRow(), 3)+"' AND " +
             " COMMENT "+comm2
        );
            database(ComboBox.getSelectedItem().toString());
                    }
            
            
            }
            
            
            else if(fromDB.equals("HISTORY"))
            {
                String comm1 ="";
            if(Table.getValueAt(Table.getSelectedRow(), 7)==null)
                comm1="NULL";
            else
                comm1 = "'"+Table.getValueAt(Table.getSelectedRow(), 7).toString()+"'";
            
                String comm2 ="";
            if(Table.getValueAt(Table.getSelectedRow(), 7)==null)
                comm2="IS NULL";
            else
                comm2 = "= '"+Table.getValueAt(Table.getSelectedRow(), 7).toString()+"'";
            
            String s = "Take back good \n" +
                    "ITEM_NAME = "   +Table.getValueAt(Table.getSelectedRow(), 0)+" \n" +
             "COLOR_CODE = "   +Table.getValueAt(Table.getSelectedRow(), 1)+" \n" +
             "ROLL_LENGTH = "   +Table.getValueAt(Table.getSelectedRow(), 2)+"";

            
            int out = JOptionPane.showConfirmDialog(null, s, "Add to database", JOptionPane.OK_CANCEL_OPTION);
            
            if (out==0) {
            execute("INSERT INTO ROOT.GOODS "
                    + "(ITEM_NAME, COLOR_CODE, ROLL_LENGTH, ENTRY_DATE, COMMENT)" +
         "	VALUES ("
                    + "'"   +Table.getValueAt(Table.getSelectedRow(), 0)+"'"
                    + ", '"   +Table.getValueAt(Table.getSelectedRow(), 1)+"'"
                    + ", "   +Table.getValueAt(Table.getSelectedRow(), 2)+""
                    + ", '"   +Table.getValueAt(Table.getSelectedRow(), 3)+"'"
                    + ", "+comm1+")"
            );
            execute("DELETE FROM ROOT.HISTORY WHERE " +
             " ITEM_NAME = '"   +Table.getValueAt(Table.getSelectedRow(), 0)+"' AND " +
             " COLOR_CODE = '"   +Table.getValueAt(Table.getSelectedRow(), 1)+"' AND " +
             " ROLL_LENGTH = "   +Table.getValueAt(Table.getSelectedRow(), 2)+" AND " +
             " ENTRY_DATE = '"   +Table.getValueAt(Table.getSelectedRow(), 3)+"' AND " +
             " SEND_DATE = '"   +Table.getValueAt(Table.getSelectedRow(), 4)+"' AND " +
             " SEND_TIME = '"   +Table.getValueAt(Table.getSelectedRow(), 5)+"' AND " +
             " CUSTOMER_NAME = '"   +Table.getValueAt(Table.getSelectedRow(), 6)+"' AND " +
             " COMMENTS "+comm2
        );
            database(ComboBox.getSelectedItem().toString());
            }
            }
        }
            else
            {
                JOptionPane.showMessageDialog(null,"Please chose row to transfer","Transfer",JOptionPane.INFORMATION_MESSAGE);
            }
    }//GEN-LAST:event_SendButtonActionPerformed

    private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
        String comm ="";
            if(Table.getValueAt(Table.getSelectedRow(), 4)==null)
                comm="IS NULL";
            else
                comm = "= '"+Table.getValueAt(Table.getSelectedRow(), 4).toString()+"'";
            
            String s = "Do you want to delete the following from " +ComboBox.getSelectedItem().toString() + "\n"+
             "ITEM_NAME = "   +Table.getValueAt(Table.getSelectedRow(), 0) +"\n" +
             "COLOR_CODE = "   +Table.getValueAt(Table.getSelectedRow(), 1)+ "\n" +
             "ROLL_LENGTH = "   +Table.getValueAt(Table.getSelectedRow(), 2)+ "\n" +
             "ENTRY_DATE = "   +Table.getValueAt(Table.getSelectedRow(), 3)+"\n" +
             "COMMENT "+comm;
                    
        int delete = JOptionPane.showConfirmDialog(null, s, "Delete", JOptionPane.OK_CANCEL_OPTION);
        
        if(delete==0 && Table.getSelectedRow()!=-1 && Table.getSelectedColumn()!=-1)
        {
            execute("DELETE FROM ROOT."+ComboBox.getSelectedItem().toString()+" WHERE " +
             " ITEM_NAME = '"   +Table.getValueAt(Table.getSelectedRow(), 0)+"' AND " +
             " COLOR_CODE = '"   +Table.getValueAt(Table.getSelectedRow(), 1)+"' AND " +
             " ROLL_LENGTH = "   +Table.getValueAt(Table.getSelectedRow(), 2)+" AND " +
             " ENTRY_DATE = '"   +Table.getValueAt(Table.getSelectedRow(), 3)+"' AND " +
             " COMMENT "+comm
        );
        }
        /*DELETE FROM ROOT.GOODS WHERE ITEM_NAME = 'Dim out 4' AND COLOR_CODE = '2' AND ROLL_LENGTH = 66.8 AND ENTRY_DATE = '2015-10-17' AND COMMENT IS NULL;*/

        database(ComboBox.getSelectedItem().toString());
    }//GEN-LAST:event_DeleteButtonActionPerformed

    private void TableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableMouseClicked

    }//GEN-LAST:event_TableMouseClicked

    private void TableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TableMouseReleased

    }//GEN-LAST:event_TableMouseReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws ClassNotFoundException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddButton;
    private javax.swing.JComboBox ComboBox;
    private javax.swing.JButton DeleteButton;
    private javax.swing.JButton EditButton;
    private javax.swing.JButton SendButton;
    public static javax.swing.JTable Table;
    public javax.swing.JLabel User;
    public javax.swing.JPanel bottom;
    public javax.swing.JLabel bottomBg;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel top;
    private javax.swing.JLabel topBg;
    // End of variables declaration//GEN-END:variables
}
