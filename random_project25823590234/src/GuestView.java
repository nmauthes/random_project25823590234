import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GuestView extends JFrame {
	private final int WIDTH = 800;
	private final int HEIGHT = 550;
	private final int TEXT_AREA_WIDTH = 20;
	private final int TEXT_AREA_HEIGHT = 30;
	private final int ROOMS_NUMBER_OF_ROWS = 5;
	private final int FIELD_WIDTH = 5;
	private final int ROOMS_CELL_HEIGHT = 10;
	
	private ReservationSystem model;
	private Account activeAccount;
	private boolean[] currentlyOccupiedRooms;
	private String currentCheckInDate, currentCheckOutDate;
	
	private JButton makeReservationButton, confirmButton, cancelReservationButton;
	private JTabbedPane guestTabs;
	private JPanel reservationPanel, viewCancelPanel, reservationButtonPanel, roomNumberPanel;
	private JTextArea availableRoomsArea;
	private JTextField roomNumberField, checkInField, checkOutField;
	private JLabel availableRoomsLabel, roomNumberLabel, usernameLabel, allReservationsLabel, displayReservationsLabel;
	private JScrollPane roomsScrollPane;
	private JTable roomsTable;
	private JComboBox<String> roomTypeComboBox;
	private DefaultTableModel roomsModel;
	
	int selectedRoomsRow, selectedCalendarRow, selectedCalendarColumn;

	public GuestView(ReservationSystem model) throws Exception {
		this.model = model;
		
		setTitle("Guest View");
		setLayout(new FlowLayout());
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		activeAccount = new Account("default");							// temporary for testing purposes, remove after sign in is completed
		
		// TODO add changelistener to update text areas
		
		guestTabs = new JTabbedPane();
		
		reservationPanel = new JPanel(new BorderLayout());
		
		availableRoomsArea = new JTextArea(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
		reservationPanel.add(availableRoomsArea, BorderLayout.CENTER);
		
		reservationButtonPanel = new JPanel();
		
		availableRoomsLabel = new JLabel("Available rooms");
		reservationPanel.add(availableRoomsLabel, BorderLayout.NORTH);
		
		makeReservationButton = new JButton("Make new reservation");
		confirmButton = new JButton("Confirm?");
		
		checkInField = new JTextField(FIELD_WIDTH);
		checkOutField = new JTextField(FIELD_WIDTH);
		String[] comboOptions = { "Luxurious", "Economic" };
		roomTypeComboBox = new JComboBox<>(comboOptions);
		
		makeViewCancelTab();	//flo
		
		makeReservationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] message = {"Enter check-in date:", checkInField, "Enter check-out date:", checkOutField, "Enter room type:", roomTypeComboBox};
				
				int choice = JOptionPane.showConfirmDialog(GuestView.this, message, "Enter dates", JOptionPane.OK_CANCEL_OPTION);
				
				if(choice == JOptionPane.OK_OPTION) {
					boolean[] rooms;
					
					String checkIn = checkInField.getText();
					String checkOut = checkOutField.getText();
					String roomType = (roomTypeComboBox.getSelectedIndex() == 0) ? "L" : "E";
						
					try {
						if(model.checkStayValidity(checkIn, checkOut))
								throw new Exception();
								
						rooms = model.getOccupiedRooms(checkIn, checkOut);
						availableRoomsArea.setText(printAvailableRooms(rooms, roomType));
						currentlyOccupiedRooms = rooms;
						currentCheckInDate = checkIn;
						currentCheckOutDate = checkOut;
					}
					catch(Exception ex) {
						JOptionPane.showMessageDialog(GuestView.this, "Please enter valid date(s)", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					checkInField.setText("");
					checkOutField.setText("");
				}
			}
		});
		
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int newRoomNumber = Integer.parseInt(roomNumberField.getText());
				if(!currentlyOccupiedRooms[newRoomNumber]) {
					String roomType = (newRoomNumber > ReservationSystem.NUMBER_OF_ROOMS / 2) ? "L" : "E";
					try {
						Reservation r = new Reservation(currentCheckInDate, currentCheckOutDate, roomType, newRoomNumber);
						activeAccount.addReservation(r);
						SimpleReceipt simpleReceipt = new SimpleReceipt();
						ComprehensiveReceipt compReceipt = new ComprehensiveReceipt();
						JOptionPane.showMessageDialog(GuestView.this, simpleReceipt.showReceipt(activeAccount) + "\n" + compReceipt.showReceipt(activeAccount), "Reservation successful", JOptionPane.PLAIN_MESSAGE); // FIX BALANCE
					}
					catch(Exception ex) {
						JOptionPane.showMessageDialog(GuestView.this, "Reservation error", "An error occurred", JOptionPane.ERROR_MESSAGE);
					}
				}
				availableRoomsArea.setText("");
			}
		});
		
		
		reservationButtonPanel.add(makeReservationButton);
		reservationButtonPanel.add(confirmButton);
		reservationPanel.add(reservationButtonPanel, BorderLayout.SOUTH);
		
		roomNumberPanel = new JPanel();
		
		roomNumberLabel = new JLabel("Enter room number to reserve:");
		roomNumberField = new JTextField(FIELD_WIDTH);
		
		roomNumberPanel.add(roomNumberLabel);
		roomNumberPanel.add(roomNumberField);
		reservationPanel.add(roomNumberPanel, BorderLayout.EAST);
				
		guestTabs.addTab("New reservation", reservationPanel);
		guestTabs.addTab("View/Cancel", viewCancelPanel);
		
		add(guestTabs);
		
		setLocationRelativeTo(null);
		//setVisible(true);
	}
	
	private void makeViewCancelTab() {
		viewCancelPanel = 										new JPanel(new BorderLayout());
		cancelReservationButton = 								new JButton("Cancel selected reservation");
		allReservationsLabel = 									new JLabel("All reservations");
		DefaultListModel<Reservation> viewReservationsModel = 	new DefaultListModel<Reservation>();
		JList<Reservation> viewReservationsList = 				new JList<Reservation>(viewReservationsModel);
		JScrollPane viewReservationsScrollPane = 				new JScrollPane(viewReservationsList);
		
		// Get activeAccount's Reservations
		ArrayList<Reservation> viewReservationsAL = activeAccount.getReservations();
		
		// Add activeAccount's Reservations to viewReservationsModel
		for(int i = 0; i < viewReservationsAL.size(); i++) {
			viewReservationsModel.addElement(viewReservationsAL.get(i));
		}

		//TODO set list to allow multiple selections // get it working first
		

		// When clicked, removes selected Reservations from activeAccount's Reservations		
		cancelReservationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] cancelIndices = viewReservationsList.getSelectedIndices();
				activeAccount.printReservations();						// for testing purposes
				activeAccount.removeReservations(cancelIndices);
				activeAccount.printReservations();						// for testing purposes, remove after Make a Reservation is completed
																		// and this part is retested
			}
		});
		
		// Add components to viewCancelPanel
		viewCancelPanel.add(viewReservationsScrollPane, BorderLayout.WEST);
		viewCancelPanel.add(allReservationsLabel, BorderLayout.NORTH);
		viewCancelPanel.add(cancelReservationButton, BorderLayout.SOUTH);
	}
	
	//flo
//	private void buildRoomsTableModel() { //TODO
//		MouseAdapter m = new MouseAdapter() {
//			public void mousePressed(MouseEvent e) {
//				selectedRoomsRow = roomsTable.getSelectedRow();
//				
//				Reservation toBeCancelled = (Reservation) roomsTable.getValueAt(selectedCalendarRow, selectedCalendarColumn);
//			}
//		};
//		
//		roomsModel = new DefaultTableModel(buildRoomsArray(), null);
//		roomsTable = new JTable(roomsModel) {
//			public boolean isCellEditable(int row, int col) {
//				return false;
//			}
//		};
//		roomsTable.setRowHeight(ROOMS_CELL_HEIGHT);
//		roomsTable.setCellSelectionEnabled(true);
//		roomsTable.addMouseListener(m);
//	}
	
	//flo
//	private Reservation[][] buildRoomsArray() {
//		ArrayList<Reservation> allReservations = model.getAllReservations();
//		Reservation[][] temp = new Reservation[ROOMS_NUMBER_OF_ROWS][2];
//		
//		for(int i = 0; i < allReservations.size(); i++) {
//			temp[i][1] = allReservations.get(i);
//		}
//		
//		return temp;
//	}
	
	public void setActiveAccount(Account account) {
		activeAccount = account;
		availableRoomsArea.setText("Current user: " + activeAccount.getName());
	}
	
	public void setActiveAccount(int id) {
		activeAccount = model.getAccounts().get(id);
		availableRoomsArea.setText("Current user: " + activeAccount.getName());
	}
	
	public String printAvailableRooms(boolean[] rooms, String roomType) {
		String roomsList = "";
		int startingIndex, endingIndex;
		
		if(roomType.equalsIgnoreCase("L")) {
			startingIndex = 0;
			endingIndex = rooms.length / 2;
		}
		else {
			startingIndex = rooms.length / 2;
			endingIndex = rooms.length;
		}
		
		for(int i = startingIndex; i < endingIndex; i++) {
			if(!rooms[i]) {
				roomsList += "Room " + (i + 1) + "\n";
			}
		}
		
		if(roomsList.equals(""))
			return "There are no such rooms available";
		
		return roomsList;
	}
	
}
