
public class ComprehensiveReceipt implements Receipt 
{
	public String showReceipt(Account account)
	{
			
			return "Comprehensive Receipt: \n User: " + account.getName() + ", ID: " + account.getID() + ", Reservations: " + account.getReservations() + ", Total Amount Due: $" + account.getTotalBalance();
	}
}
