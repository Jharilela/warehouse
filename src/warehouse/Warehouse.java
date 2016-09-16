package warehouse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;




public class Warehouse {

    /**
     * @param args the command line arguments
     */
     static boolean editable = false;
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String login = login();
        if(login.equals("Admin"))
        {
            setEditable(true);
        MainPage mp = new MainPage();
        mp.setVisible(true);
        mp.setLocationRelativeTo(null);
        mp.setFocusable(true);
        }
        else if(login.equals("Guest"))
        {
            setEditable(false);
        MainPage mp = new MainPage();
        mp.setVisible(true);
        mp.setLocationRelativeTo(null);
        mp.setFocusable(true);
        }
    }
    
    public static void setEditable(boolean val)
    {
        editable = val;
    }
    
    public static String login() throws FileNotFoundException, IOException
    {
        String result="";
        
   FileReader fr = new FileReader("password.rtf");
   BufferedReader br = new BufferedReader(fr);
   String lastLine = "";
   String str="";
   while((str=br.readLine())!=null){
      str=str.trim();
      lastLine=str;
   }
        String word="";
      for(int i =0;i<lastLine.length();i++)
      {
          if((lastLine.charAt(i))==' ')
          {
          word="";   
          }
          else
          {
          word+=lastLine.charAt(i);  
          }
      }
        word= word.substring(0, word.length()-1);
   br.close();

        
        JToggleButton admin  = new JToggleButton("admin"); 
        JToggleButton guest  = new JToggleButton("guest");
        JPasswordField password = new JPasswordField();
        
         admin.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
        if(abstractButton.getModel().isSelected())
        {
            guest.setSelected(false);
        }
        else if ((abstractButton.getModel().isSelected()==false)&&(guest.isSelected()==false))
                {
                 admin.setSelected(true);   
                }
      }
    });
         guest.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
        if(abstractButton.getModel().isSelected())
        {
            admin.setSelected(false);
            password.setText(null);
        }
                else if ((abstractButton.getModel().isSelected()==false)&&(guest.isSelected()==false))
                {
                 admin.setSelected(true);   
                }
      }
    });
        
        final JComponent[] inputs = new JComponent[] {
                admin,
                guest,
		new JLabel("Password"),
		password
        };
        JOptionPane.showMessageDialog(null, inputs, "Login", JOptionPane.PLAIN_MESSAGE, null);
        if(password.getText().equals(word) && admin.isSelected())
        {
            System.out.println("Admin");
        return "Admin";
        }
        else if (password.getText().equals(word)==false && admin.isSelected())
        {
         JOptionPane.showMessageDialog(null, "Password is incorrect; Please try again", "Admin Password", JOptionPane.ERROR_MESSAGE);
         return login();
        }
        else if(guest.isSelected())
        {
            return "Guest";
        }
        else if(password.getText().matches("[a-zA-Z]+") && admin.isSelected()==false && guest.isSelected()==false)
        {
            JOptionPane.showMessageDialog(null, "Choose a User", "Login", JOptionPane.ERROR_MESSAGE);
            return login();
        }
        return result;
        
    }
    
}
