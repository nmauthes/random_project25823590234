import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HotelTester {	
	public static void main(String[] args) throws Exception {
		ReservationSystem rs = new ReservationSystem();
		GuestView g = new GuestView(rs);
		ManagerView m = new ManagerView(rs);
		
		Reservation res1 = new Reservation("12/05/2016", "12/15/2016", "L", 1); // testing conflict check
		Reservation res2 = new Reservation("12/10/2016", "12/30/2016", "L", 2);
		System.out.println(res1.checkConflict(res2));
		
		buildLoginFrame(rs, g, m);
	}
	
	private static void buildLoginFrame(ReservationSystem rs, GuestView g, ManagerView m) {
		final int FIELD_WIDTH = 5;
		
		JTextField signupField;
		JLabel signupLabel;
		
		JFrame loginFrame = new JFrame(); // creates login prompt for guest
		
		loginFrame.setLayout(new FlowLayout());
		loginFrame.setResizable(false);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton managerButton = new JButton("Manager");
		JButton guestButton = new JButton("Guest");
		
		managerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m.setVisible(true);
				g.dispose();
			}
		});
		
		signupField = new JTextField(FIELD_WIDTH);
		signupLabel = new JLabel("Enter guest name or ID:");
		
		guestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] fields = { signupLabel, signupField };
				String[] buttons = { "Sign up", "Sign in" };
				int choice = JOptionPane.showOptionDialog(loginFrame, fields, "Guest login", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, null);
				
					if(choice == 0) { // add new user
						String name = signupField.getText();
						Account newAccount = new Account(name);
						rs.addAccount(newAccount);
					}
					else {
						// find user by id
					}
						
				
				g.setVisible(true);
				m.dispose();
			}
		});
		
		loginFrame.add(managerButton);
		loginFrame.add(guestButton);
		
		loginFrame.pack();
		loginFrame.setLocationRelativeTo(null);
		loginFrame.setVisible(true);
	}
}
