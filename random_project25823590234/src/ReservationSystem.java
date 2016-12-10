import javax.swing.event.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.io.*;

import java.util.Arrays; // for testing

public class ReservationSystem implements java.io.Serializable { // model
	public static final int NUMBER_OF_ROOMS = 20;
	
	private ArrayList<Account> accounts;
	private ArrayList<ChangeListener> listeners;
	
	public ArrayList<Account> getAccounts() { return accounts; }
	public Calendar getCalendar() { return calendar; }
	public void addListener(ChangeListener cl) { listeners.add(cl); }
	
	private SimpleDateFormat sdf;  // for parsing Dates from Strings
	private Calendar calendar;
	private Date currentDate;
	private Date selectedDate;
	
	public ReservationSystem() throws Exception {
		accounts = new ArrayList<>();
		listeners = new ArrayList<>();
		sdf = new SimpleDateFormat("MM/dd/yyyy");
		initCalendar();
	}
	
	public void addAccount(Account account) {
		accounts.add(account);
		changeMade();
	}
	
	public boolean[] getOccupiedRooms(String checkInDate, String checkOutDate) throws Exception {
		boolean[] availableRooms = new boolean[NUMBER_OF_ROOMS]; // mark the conflicting rooms as false
	
		Date checkIn = sdf.parse(checkInDate);
		Date checkOut = sdf.parse(checkOutDate);
	
		for(Account a : accounts) { // go through all the reservations and see if there's a conflict
			for(Reservation r : a.getReservations()) {
				
				// mark occupied rooms as true, available rooms as false
				if(r.checkConflict(checkIn, checkOut)) {
					availableRooms[r.getRoomNumber()] = true; // room is occupied
				}
				else
					availableRooms[r.getRoomNumber()] = false;
			}
		}
		
		// 0-9 luxurious and 10 - 19 economic
		
		System.out.println(Arrays.toString(availableRooms));
		
		return availableRooms; // use to check what's available
	}
	
	public Account getAccountByID(int id) {
		return accounts.get(id);
	}
	
	public void findAndRemoveReservation(Reservation toRemove) { // finds account with specified reservation and removes
		for(Account a : accounts) {
			for(Reservation r : a.getReservations()) {
				if(r.equals(toRemove))
					a.removeReservation(r);
			}
		}
	}
	
	public ArrayList<Reservation> getReservationsByRoomNumber(int number) { // collects all reservations associated with specified room number
		ArrayList<Reservation> temp = new ArrayList<>();
		
		for(Account a : accounts) {
			for(Reservation r : a.getReservations()) {
				if(r.getRoomNumber() == number)
					temp.add(r);
			}
		}
		
		return temp;
	}
	
	public ArrayList<Reservation> getReservationsByDate(Date date) { // gets the reservations the current date falls between
		ArrayList<Reservation> temp = new ArrayList<>();
		
		for(Account a : accounts) {
			for(Reservation r : a.getReservations()) {
				if(date.compareTo(r.getCheckInDate()) >= 0 && date.compareTo(r.getCheckOutDate()) <= 0)
					temp.add(r);
			}
		}
		
		return temp;
	}
	
	// Collects and returns all reservations
	
	public ArrayList<Reservation> getAllReservations() {
		ArrayList<Reservation> temp = new ArrayList<>();
	
		for(Account a : accounts) {
			for(Reservation r : a.getReservations()) {
				temp.add(r);
			}
		}
		
		return temp;
	}
	
	// input validation for checkIn and checkOut dates
	public boolean checkStayValidity(String checkIn, String checkOut) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date checkInDate = null;
		Date checkOutDate = null;
		
		try {
			checkInDate = sdf.parse(checkIn);
			checkOutDate = sdf.parse(checkOut);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Date today = new Date();
		
		//checks not before current date
		if(today.before(checkInDate)||today.before(checkOutDate))
			return false;
		
		//checks less than 60 days
		int diffInDays = (int)( (checkOutDate.getTime() - checkInDate.getTime()) / (1000 * 60 * 60 * 24) );
		
		if(diffInDays >= 60)
			return false;
		
		return true;
	}
	
	/**
	 * Sets the calendar to the current date.
	 */
	public void goToCurrent() {
		initCalendar();
	}
	
	/**
	 * Sets the calendar to the specified date.
	 * 
	 * @param date The date to go to
	 */
	public void goToDate(String date) {
		int[] parsed = parseDate(date);
		calendar.set(parsed[2], parsed[0], parsed[1]);
		
		selectedDate = calendar.getTime();
	}
	
	
	/**
	 * Sets the calendar to the specified date.
	 * 
	 * @param month Month to go to
	 * @param day Day to go to
	 * @param year Year to go to
	 */
	public void goToDate(int month, int day, int year) {
		calendar.set(year, month, day);
		
		selectedDate = calendar.getTime();
	}
	
	/**
	 * Moves the calendar forward by one month.
	 */
	public void nextMonth() {
		calendar.add(Calendar.MONTH, 1);
		selectedDate = calendar.getTime();
		changeMade();
	}
	
	/**
	 * Moves the calendar backward by one month.
	 */
	public void previousMonth() {
		calendar.add(Calendar.MONTH, -1);
		selectedDate = calendar.getTime();
		changeMade();
	}
	
	public void nextYear() {
		calendar.add(Calendar.YEAR, 1);
		selectedDate = calendar.getTime();
		changeMade();
	}
	
	public void previousYear() {
		calendar.add(Calendar.YEAR, -1);
		selectedDate = calendar.getTime();
		changeMade();
	}
	
	public int getDaysBetween(String d1, String d2) {
		Calendar temp = Calendar.getInstance();
		
		int[] d1Parsed = parseDate(d1);
		int[] d2Parsed = parseDate(d2);
		
		temp.set(d1Parsed[2], d1Parsed[0], d1Parsed[1]);
		Date date1 = temp.getTime();
		temp.set(d2Parsed[2], d2Parsed[0], d2Parsed[1]);
		Date date2 = temp.getTime();
	
		return (int) TimeUnit.MILLISECONDS.toDays(Math.abs(date2.getTime() - date1.getTime()));
	}
	
	/**
	 * Parses a String of the form MM/DD/YYYY into a format that can be used by the calendar.
	 * 
	 * @param date Date as a String
	 * @return An int array representing the numeric date
	 */
	public static int[] parseDate(String date) {
		String[] temp = date.split("/");
		int[] parsed = new int[temp.length];
		
		parsed[0] = Integer.parseInt(temp[0]) - 1;
		parsed[1] = Integer.parseInt(temp[1]);
		parsed[2] = Integer.parseInt(temp[2]);
			
		return parsed;
	}
	
	private void changeMade() {
		ChangeEvent e = new ChangeEvent(this);
		
		for(ChangeListener cl : listeners) {
			cl.stateChanged(e);
		}
	}
	
	/**
	 * Initializes the calendar to the current day.
	 */
	private void initCalendar() {
		calendar = Calendar.getInstance();
	
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		currentDate = calendar.getTime();
		selectedDate = currentDate;
	}
	/**
	 * load method will deserialize the file reservation.ser and will load the data into the Model's arraylist of accounts
	 * 
	 */
	public void load()
	{
		try {
	         FileInputStream fileIn = new FileInputStream("reservation.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	          this.accounts = (ArrayList<Account>) in.readObject();
	         in.close();
	         fileIn.close();
	         System.out.printf("Data has been loaded. \n");
	      }catch(IOException i) {
	         i.printStackTrace();
	         return;
	      }catch(ClassNotFoundException c) {
	         System.out.println("Reservation data not found.");
	         c.printStackTrace();
	         return;
	      }
	}
	/**
	 * The save method will serialize the data of the Model (the data is stored in the arrayList accounts)
	 * 
	 */
	public void save()
	{
		try {
	         FileOutputStream fileOut =
	         new FileOutputStream("reservation.ser");
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this.accounts);
	         out.close();
	         fileOut.close();
	         System.out.printf("Data has been saved. \n");
	      }catch(IOException i) 
		  {
	         i.printStackTrace();
	      }
	}
	
}