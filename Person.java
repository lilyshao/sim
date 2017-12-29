import java.util.*;

public class Person extends Entity {

	protected Map<String, Integer> needs; //basic necessities a person owns
	protected Map<String, Integer> demand; //shortage of needs that need to be replenished

	protected int wage; // earning at each turn
	protected int productivity; // ability to produce at each turn
	protected int consumptionRate; //TODO: => Map, different needs with diff rates

	protected int employer; // which business the person works for, by business id

	protected boolean isEmployed;
	protected boolean isDead;

	public Person(int id) {
		// this.id = id;
		// rand = new Random();
		super(id);

		needs = new HashMap<String, Integer>();
		needs.put("food", rand.nextInt(20)+20);
		needs.put("clothing", rand.nextInt(20)+20);
		needs.put("shelter", rand.nextInt(20)+20);

		demand = new HashMap<String, Integer>();

		balance = rand.nextInt(101) + 50; // range(50-150)
		wage = rand.nextInt(3)+1;
		// productivity = rand.nextInt(5)+1;
		consumptionRate = rand.nextInt(4)+1; //range(1-4)

		isEmployed = true;
		isDead = false;
	}

	public Map<String,Integer> getNeeds() { return needs; }
	public Map<String,Integer> getDemand() { return demand; }

	public void consumeNeeds() {
		// increment each need by 1 at each step
		// TODO: different person has different level of consumptions
		for (String s : needs.keySet()) {
			needs.put(s, needs.get(s)-consumptionRate);
		}
	}

	// right now, buy needs only when amount is negative
	// TODO: specify amount to buy
	public void buyNeeds() {
		// check needs
		for (String need : needs.keySet()) {
			if (needs.get(need) <= 0) {
				demand.put(need, consumptionRate); //add need to demand
				buy(need, consumptionRate, sim.getPrices().get(need));
			}
		}
	}

	public void earnWage() {
		if (isEmployed()) { balance += wage; }
	}

	// helper method for buyNeeds
	public void buy(String need, int amount, int price) {
		// check if there's enough balance to buy
		int cost = price * amount;
		if (balance >= cost) {
			if (chooseBusiness(need, amount)==null) {
				isDead = true; // no business has the need to sell; person dies
			} else {
				// decrement the business's inventory;
				Business biz = chooseBusiness(need, amount);
				biz.sellNeed(need, amount, price);
				// increment need
				needs.put(need, needs.get(need)+amount);
				// decrement balance by cost
				balance -= cost;

				// clear the demand for this need, upon successful buying
				demand.remove(need);
			}
		}
		// else {
		// 	// for now, die
		// 	// TODO: borrow(), or persist for several steps before death
		// 	isDead = true;
		// }
	}

	// for now: select randomly a business that has enough need to sell;
	public Business chooseBusiness(String need, int amount) {
		List<Business> available = new ArrayList<Business>();
		for (Business biz : sim.getBusinesses()) {
			if (biz.getInventories().get(need) >= amount) { available.add(biz); }
		}
		if (available.size()!=0) {
			return available.get( rand.nextInt(available.size()) );
		} else { return null; }

	}

	public void setEmployer(int businessID) {
		employer = businessID;
	}

	public int employer() {
		if (!isEmployed()) {return -1;}
		else return employer;
	}

	public boolean isEmployed() {return isEmployed;}
	public boolean isDead() {return isDead;}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Person " + id + ":" + "\n");
		sb.append("current balance: " + balance + "\n");
		sb.append("wage and productivity: " + wage + " , " + productivity + "\n");
		sb.append("needs level:\n");
		for (String k : needs.keySet()) {
			sb.append(k + " : " + needs.get(k) + "\n");
		}

		if (isEmployed()) {
			sb.append("Employment status: employed by " + employer + "\n");
		} else {
			sb.append("Employment status: unemployed." + "\n");
		}
		if (isDead()) {
			sb.append("Alive: no\n");
		} else {
			sb.append("Alive: yes\n\n");
		}

		return sb.toString();
	}

	public static void main(String[] args) {
		Person p = new Person(5);
		System.out.print(p);
		System.out.println("consume needs");
		p.consumeNeeds();
		System.out.println(p);
		// p.earnWage();
		// System.out.print(p);
	}
}
