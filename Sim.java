import java.util.*;

public class Sim{
	protected int current; //current step

	//TODO: protected final double RATIO_PEOPLE_TO_BUSINESSES
	protected int numBusinesses;
	protected int numPeople;

	protected List<Person> people;
	protected List<Business> businesses;

	protected Map<String, Integer> totalNeeds; //aggregate needs all the people own
	protected Map<String, Integer> totalSupply; //aggregate inventories of all businesses
	protected Map<String, Integer> totalDemand; //aggregate demand from all people

	// TODO: fluctuates based on supply v. demand
	// for now: preset
	protected Map<String, Integer> prices;

	public Sim() {
		//TODO: less arbitrary assignment
		numBusinesses = 4;
		numPeople = 30;
	}

	public void initialize() {
		people = generatePeople(numPeople);
		businesses = generateBusinesses(numBusinesses);

		// assign people, businesses to this simulation
		for (Person p : people) { p.sim = this; }
		for (Business b : businesses) { b.sim = this; }

		// assign these people to different businesses; using sublist; update employer for each person
		int factor = numPeople/numBusinesses; //7 try to assign similar # of people to each business
		for (int i=0; i<numBusinesses; i++) {
			Business biz = businesses.get(i);
			List<Person> group = people.subList(i*factor, (i+1)*factor);
			for (Person p : group) {
				p.setEmployer(biz.id);
				biz.employees.add(p);
			}
		}
		// if there are persons remain unassigned to business
		// assign them to the last business in list(businesses)
		if (numPeople%numBusinesses!=0) { //if there's unassigned person left
			List<Person> remaining = people.subList(numBusinesses*factor, people.size());
			Business lastBiz = businesses.get(businesses.size()-1);
			for (Person p : remaining) {
				p.setEmployer(lastBiz.id);
				lastBiz.employees.add(p);
			}
		}

		totalSupply = totalSupply();
		// totolDemand = totalDemand();
		totalNeeds = totalNeeds();

		// set prices manually, for now
		prices = new HashMap<String, Integer>();
		prices.put("food", 1);
		prices.put("clothing", 3);
		prices.put("shelter", 30);

		current = 1; // current step is 1
	}

	public static void main(String[] args) {
		Sim sim1 = new Sim();
		sim1.initialize();
		// System.out.println(sim1.getPeople());
		// System.out.println(sim1.getBusinesses());
		// sim1.step();

		do { sim1.step(); } while ( !sim1.step() );
		// for (int i=0; i<500; i++) {
		// 	sim1.step();
		// }
	}

	// carry out processes at each turn of step: go through each person & business
	// return true if simulation is over: if all people are dead, or all businesses closed
	public boolean step() {
		System.out.println("current step: " + current);

		List<Person> dead = new ArrayList<Person>();
		for (Person p : people) {
			// at the start of a step, if a person has 0 for a need, he dies
			// TODO: can be simplified -> demand map not empty at the start
			Map<String, Integer> ne = p.getNeeds();
			for (String n : ne.keySet()) {
				if (ne.get(n) <= 0) {
					p.isDead = true;
					dead.add(p);
					break;
				}
			}
			if (p.isDead) continue;

			p.consumeNeeds();

			p.buyNeeds();
			// if (p.isDead) { dead.add(p); }

			p.earnWage(); // ? put this before or after p.buyNeeds()?
		}
		people.removeAll(dead);

		List<Business> closed = new ArrayList<Business>();
		for (Business b : businesses) {
			b.payWage();
			if (current%5==0) { b.produce(); } //produce every 5 steps
			if (b.isClosed) { closed.add(b); }
		}
		businesses.removeAll(closed);

		current++;

		System.out.println("There are " + people.size() + " people left, and " + businesses.size() + " businesses left.");
		System.out.println("Total demand: ");
		printMap( totalNeeds() );
		System.out.println("\nTotal supply: ");
		printMap( totalSupply() );
		System.out.println("\nAggregate needs:");
		printMap( totalNeeds() );
		System.out.println("\n");

		// end condition: if people are all dead, and/or businesses are all closed
		if (people.size()<=0 || businesses.size()<=0) { return true; }
		else { return false; }
	}

	// getter classes to share prices and businesses info with other classes
	public Map<String, Integer> getPrices() { return prices; }
	public List<Business> getBusinesses() { return businesses; }
	public List<Person> getPeople() { return people; }

	public List<Person> generatePeople(int x) {
		List<Person> listPeople = new ArrayList<Person>();
		for (int i=0; i<x; i++) {
			listPeople.add( new Person(i+1) );
		}
		return listPeople;
	}

	public List<Business> generateBusinesses(int x) {
		List<Business> listBiz = new ArrayList<Business>();
		for (int i=0; i<x; i++) {
			listBiz.add( new Business(i+1) );
		}
		return listBiz;
	}

	// when a product's totalNeeds > totalSupply: increasePrice(), by how much?
	public void changePrice() { return; }

	public Map<String, Integer> totalNeeds() {
		return totalfromAll("p", "needs");
	}

	public Map<String, Integer> totalDemand() {
		return totalfromAll("p", "demand");
	}

	public Map<String, Integer> totalSupply() {
		return totalfromAll("b", "");
	}

	// helper method for totalDemand and totalNeeds
	public Map<String, Integer> totalfromAll(String whichEntity ,String whichMap) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Map<String, Integer> info = new HashMap<String, Integer>();

		if (whichEntity=="b") {
			for (Business b : businesses) {
				info = b.getInventories();
				for (String prod : info.keySet()) {
					if (map.containsKey(prod)) {
						map.put(prod, map.get(prod)+info.get(prod));
					} else {
						map.put(prod, info.get(prod));
					}
				}
			}
		} else {
			for (Person p : people) {
				if (whichMap=="needs") { info = p.getNeeds(); }
				if (whichMap=="demand") { info = p.getDemand(); }
				for (String need : info.keySet()) {
					if (map.containsKey(need)) {
						map.put(need, map.get(need)+info.get(need));
					} else {
						map.put(need, info.get(need));
					}
				}
			}
		}

		return map;
	}

	// utility function
	public void printMap(Map map) {
		System.out.print(Arrays.toString(map.entrySet().toArray()));
	}
}

// // helper method for totalDemand and totalNeeds
// public Map<String, Integer> totalfromPeople(String whichMap) {
// 	Map<String, Integer> map = new HashMap<String, Integer>();
// 	for (Person p : people) {
// 		Map<String, Integer> info;
// 		if (which=="needs") { info = p.getNeeds(); }
// 		if (which=="demand") { info = p.getDemand(); }
// 		for (String need : info.keySet()) {
// 			if (map.containsKey(need)) {
// 				map.put(need, map.get(need)+info.get(need));
// 			} else {
// 				map.put(need, info.get(need));
// 			}
// 		}
// 	}
// 	return map;
// }
//
// public Map<String, Integer> totalSupply() {
// 	Map<String, Integer> supply = new HashMap<String, Integer>();
// 	for (Business b : businesses) {
// 		Map<String, Integer> i = b.getInventories();
// 		for (String prod : i.keySet()) {
// 			if (supply.containsKey(prod)) {
// 				supply.put(prod, supply.get(prod)+i.get(prod));
// 			} else {
// 				supply.put(prod, i.get(prod));
// 			}
// 		}
// 	}
// 	return supply;
// }
