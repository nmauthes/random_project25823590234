import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.ArrayList;

public class ManagerView extends JFrame {
	private final int WIDTH = 800;
	private final int HEIGHT = 600;
	private final int TEXT_AREA_WIDTH = 20;
	private final int TEXT_AREA_HEIGHT = 30;
	private final int CALENDAR_CELL_HEIGHT = 50;
	private final int ROOMS_CELL_HEIGHT = 10;
	private final int ROOMS_NUMBER_OF_ROWS = 5;
	private final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	private final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	private final int DAYS_PER_WEEK = 7;
	private final int WEEKS_PER_MONTH = 6;

	private ReservationSystem model;
	
	private JTabbedPane managerTabs;
	private JLabel monthAndYearLabel, allReservationsLabel;
	private JButton prevMonthButton, nextMonthButton, prevYearButton, nextYearButton, saveButton, cancelReservationButton;
	private JScrollPane calendarScrollPane, roomsScrollPane;
	private JPanel calendarPanel, calendarButtonPanel, calendarInfoPanel, roomsPanel, roomsInfoPanel;
	private DefaultTableModel calendarModel, roomsModel;
	private JTable calendarTable, roomsTable;
	private JTextArea calendarInfoArea, roomsInfoArea;
	
	int selectedCalendarRow, selectedCalendarColumn;
	int selectedRoomsRow;

	public ManagerView(ReservationSystem model) {
		this.model = model;
		
		ChangeListener cl = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateTableModel();
				highlightSelectedCell();
				updateLabels();
			}
		};
		model.addListener(cl);
		
		setTitle("Manager View");
		setLayout(new FlowLayout());
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		
		managerTabs = new JTabbedPane();
	
		calendarPanel = new JPanel(new BorderLayout()); // adds components of calendar panel
		
		monthAndYearLabel = new JLabel();
		updateLabels();
		
		buildCalendarTableModel();
		buildCalendarPanel();
		
		calendarInfoPanel = new JPanel(new BorderLayout());
		
		calendarInfoArea = new JTextArea(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
		saveButton = new JButton("Save all"); // TODO add functionality
		calendarInfoPanel.add(calendarInfoArea, BorderLayout.CENTER);
		calendarInfoPanel.add(saveButton, BorderLayout.SOUTH);
		calendarPanel.add(calendarInfoPanel, BorderLayout.EAST);
		
		roomsPanel = new JPanel(new BorderLayout()); // adds components of rooms panel
		
		buildRoomsTableModel();
		buildRoomsTablePanel();

		roomsInfoPanel = new JPanel(new BorderLayout());
		
		roomsInfoArea = new JTextArea(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
		cancelReservationButton = new JButton("Cancel selected reservation");
		roomsInfoPanel.add(roomsInfoArea, BorderLayout.EAST);
		roomsInfoPanel.add(cancelReservationButton, BorderLayout.SOUTH);
		roomsPanel.add(roomsInfoPanel);
		
		managerTabs.addTab("Calendar", calendarPanel);
		managerTabs.addTab("Rooms", roomsPanel);
		add(managerTabs);
		
		setLocationRelativeTo(null);
		//setVisible(true);
	}
	
	private void buildRoomsTablePanel() {
		allReservationsLabel = new JLabel("All reservations");
		
		roomsScrollPane = new JScrollPane(roomsTable);
		
		roomsPanel.add(allReservationsLabel, BorderLayout.NORTH);
		roomsPanel.add(roomsScrollPane, BorderLayout.WEST);
	}
	
	private void buildRoomsTableModel() { //TODO
		MouseAdapter m = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				selectedRoomsRow = roomsTable.getSelectedRow();
				
				Reservation toBeCancelled = (Reservation) roomsTable.getValueAt(selectedCalendarRow, selectedCalendarColumn);
			}
		};
		
		roomsModel = new DefaultTableModel(buildRoomsArray(), null);
		roomsTable = new JTable(roomsModel) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		roomsTable.setRowHeight(ROOMS_CELL_HEIGHT);
		roomsTable.setCellSelectionEnabled(true);
		roomsTable.addMouseListener(m);
	}
	
	private Reservation[][] buildRoomsArray() {
		ArrayList<Reservation> allReservations = model.getAllReservations();
		Reservation[][] temp = new Reservation[ROOMS_NUMBER_OF_ROWS][2];
		
		for(int i = 0; i < allReservations.size(); i++) {
			temp[i][1] = allReservations.get(i);
		}
		
		return temp;
	}
	
	private void buildCalendarPanel() {
		prevMonthButton = new JButton("<<");
		nextMonthButton = new JButton(">>");
		prevYearButton = new JButton("<");
		nextYearButton = new JButton(">");
		
		prevMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.previousMonth();
			}
		});
		nextMonthButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.nextMonth();
			}
		});
		prevYearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.previousYear();
			}
		});
		nextYearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.nextYear();
			}
		});
		
		calendarPanel.add(monthAndYearLabel, BorderLayout.NORTH);
		
		calendarScrollPane = new JScrollPane(calendarTable);
		calendarPanel.add(calendarScrollPane, BorderLayout.CENTER);
		
		calendarButtonPanel = new JPanel();
		calendarButtonPanel.add(prevYearButton);
		calendarButtonPanel.add(prevMonthButton);
		calendarButtonPanel.add(nextMonthButton);
		calendarButtonPanel.add(nextYearButton);
		calendarPanel.add(calendarButtonPanel, BorderLayout.SOUTH);
	}
	
	private void buildCalendarTableModel() {
		MouseAdapter m = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				selectedCalendarRow = calendarTable.getSelectedRow();
				selectedCalendarColumn = calendarTable.getSelectedColumn();
				
				int day = (int) calendarTable.getValueAt(selectedCalendarRow, selectedCalendarColumn);
				model.goToDate(model.getCalendar().get(Calendar.MONTH), day, model.getCalendar().get(Calendar.YEAR));
			}
		};
		
		calendarModel = new DefaultTableModel(buildMonthArray(), DAYS);
		calendarTable = new JTable(calendarModel) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		calendarTable.setRowHeight(CALENDAR_CELL_HEIGHT);
		calendarTable.setCellSelectionEnabled(true);
		calendarTable.addMouseListener(m);
		highlightSelectedCell();
	}
	
	private Integer[][] buildMonthArray() {
		Integer[][] temp = new Integer[WEEKS_PER_MONTH][DAYS_PER_WEEK];
		
		int month = model.getCalendar().get(Calendar.MONTH);
		int year = model.getCalendar().get(Calendar.YEAR);
		Calendar tempCal = Calendar.getInstance();
		tempCal.set(year, month, 1);
		
		int firstDay = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
		int day = 1;
		
		for(int i = 0; i < WEEKS_PER_MONTH; i++) {
			for(int j = 0; j < DAYS_PER_WEEK; j++) {
				if((i != 0 || j >= firstDay) && (day <= tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)))
					temp[i][j] = day++;
					
				if(day == model.getCalendar().get(Calendar.DAY_OF_MONTH) + 1) {
					selectedCalendarRow = i;
					selectedCalendarColumn = j;
				}
			}
		}
		return temp;
	}
	
	private void updateLabels() {
		monthAndYearLabel.setText(MONTHS[model.getCalendar().get(Calendar.MONTH)] + " " + model.getCalendar().get(Calendar.YEAR));
	}
	
	private void updateTableModel() {
		Integer[][] temp = buildMonthArray();
	
		calendarModel = new DefaultTableModel(temp, DAYS);
		calendarTable.setModel(calendarModel);
	}
	
	private void highlightSelectedCell() {
		for(int i = 0; i < calendarModel.getRowCount(); i++) {
			for(int j = 0; j < calendarModel.getColumnCount(); j++) {
				Integer val = (Integer) calendarModel.getValueAt(i, j);
				
				if(val != null && val == model.getCalendar().get(Calendar.DAY_OF_MONTH))
					calendarTable.changeSelection(i, j, false, false);
			}
		}
	}
}
