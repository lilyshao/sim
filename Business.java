import java.util.*;

public class Business extends Entity {

	protected Map<String, Integer> inventories; //product types and quantities
	protected List<Person> employees;
	// owner?

	protected boolean isClosed;

	protected static final int INITIAL_BALANCE = 5000;

	public Business(int id) {
		// this.id = id;
		// this.sim = sim;
		// rand = new Random();
		super(id);

		employees = new ArrayList<Person>();
		balance = INITIAL_BALANCE;
		isClosed = false;

		inventories = new HashMap<String, Integer>();
		inventories.put("food", rand.nextInt(50)+50);
		inventories.put("clothing", rand.nextInt(50)+50);
		inventories.put("shelter", rand.nextInt(50)+50);
	}

	public Map<String, Integer> getInventories() { return inventories; }

	// correspond to Person's buyNeeds
	public void sellNeed(String item, int amount, int price) {
		// decrease inventory of the product
		inventories.put(item, inventories.get(item)-amount);
		// increase earning/balance:
		balance += amount * price;
	}

	public void payWage() {
		for (Person p : employees) {
			balance -= p.wage;
			if (balance <= 0) {
				// for now: close down
				// // isClosed = true;
				break;

				// TODO: borrow(), or persist a few steps before closing
			}
		}
	}

	// TODO: not implemented yet
	// called in produce()
	// for now: cost proportional to product amount
	public void buyMaterial() { return; }

	// for now, produce fixed amount (# of employees) of each need at each turn
	// TODO: vary based on demand, employee productivity, etc.
	public void produce() {
		int rate = employees.size();
		for (String item : inventories.keySet()) {
			inventories.put(item, inventories.get(item)+rate);
		}
	}

	public List<Integer> employeeIDs() {
		List<Integer> list = new ArrayList<Integer>();
		for (Person p : employees) { list.add(p.id); }
		return list;
	}

	// TODO: hire()

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Business " + id + " :\n");
		sb.append("inventories:\n");
		for (String k : inventories.keySet()) {
			sb.append(k + " : " + inventories.get(k) + "\n");
		}
		sb.append("employees (by their ids):\n");
		sb.append(employeeIDs() + "\n");
		sb.append("current balance: " + balance + "\n");
		return sb.toString();
	}

	// public static void main(String[] args) {
	//
	// }
}
