import java.util.*;

public class Sim{
	protected int current; //current step

	// including dead(and removed) ones; mainly for assigning unique id
	protected int numPeopleCreated;

	//protected final double RATIO_PEOPLE_TO_BUSINESSES

	protected List<Person> people;
	protected List<Business> businesses;

	// protected int peopleGrowth; // # of new people to add to simulation

	protected Map<String, Integer> totalNeeds; //aggregate needs all the people own
	protected Map<String, Integer> totalSupply; //aggregate inventories of all businesses
	protected Map<String, Integer> totalDemand; //aggregate demand from all people

	// TODO: fluctuates based on supply v. demand
	// for now: preset
	protected Map<String, Integer> prices;

	// TODO: less arbitrary assignment
	protected int INITIAL_NUM_BUSINESSESS = 3;
	protected int INITIAL_NUM_PEOPLE = 100;

	public Sim() {
		people = new ArrayList<Person>();
		businesses = new ArrayList<Business>();
		generatePeople(INITIAL_NUM_PEOPLE);
		generateBusinesses(INITIAL_NUM_BUSINESSESS);
		numPeopleCreated = INITIAL_NUM_PEOPLE;

		// assign people, businesses to this simulation
		for (Person p : people) { p.sim = this; }
		for (Business b : businesses) { b.sim = this; }

		// assign these people to different businesses; using sublist; update employer for each person
		int factor = INITIAL_NUM_PEOPLE/INITIAL_NUM_BUSINESSESS; //7 try to assign similar # of people to each business
		for (int i=0; i<INITIAL_NUM_BUSINESSESS; i++) {
			Business biz = businesses.get(i);
			List<Person> group = people.subList(i*factor, (i+1)*factor);
			for (Person p : group) {
				p.setEmployer(biz.id);
				biz.employees.add(p);
			}
		}
		// if there are persons remain unassigned to business
		// assign them to the last business in list(businesses)
		if (INITIAL_NUM_PEOPLE%INITIAL_NUM_BUSINESSESS!=0) { //if there's unassigned person left
			List<Person> remaining = people.subList(INITIAL_NUM_BUSINESSESS*factor, people.size());
			Business lastBiz = businesses.get(businesses.size()-1);
			for (Person p : remaining) {
				p.setEmployer(lastBiz.id);
				lastBiz.employees.add(p);
			}
		}

		totalSupply = totalSupply();
		totalDemand = totalDemand();
		totalNeeds = totalNeeds();

		// set prices manually, for now
		prices = new HashMap<String, Integer>();
		prices.put("food", 1);
		prices.put("clothing", 3);
		prices.put("shelter", 30);

		current = 1; // on first step
	}

	public static void main(String[] args) {
		Sim sim1 = new Sim();
		do {
			sim1.step();
			// try {
			//    Thread.sleep(1000);
			// } catch(InterruptedException e) {
			//    e.printStackTrace();
			// }
		} while ( !sim1.step() );
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
			if (b.balance <= 0) { b.isClosed = true; }
			else {
				if (current%7==0) { b.produce(); } //produce every 10 steps
				b.payWage();
			}
			if (b.isClosed) { closed.add(b); }
		}
		businesses.removeAll(closed);

		// add people to simulation every x steps
		if (current%7==0) { addPeople(2); }

		System.out.println("There are " + people.size() + " people left, and " + businesses.size() + " businesses left.");
		System.out.println("Total demand: ");
		printMap( totalNeeds() );
		System.out.println("\nTotal supply: ");
		printMap( totalSupply() );
		System.out.println("\nAggregate needs:");
		printMap( totalNeeds() );
		System.out.println("\n");

		current++;

		// end condition: if people are all dead and businesses are all closed
		if (people.size()<=0 && businesses.size()<=0) { return true; }
		else { return false; }
	}

	// getter classes to share prices and businesses info with other classes
	public Map<String, Integer> getPrices() { return prices; }
	public List<Business> getBusinesses() { return businesses; }
	public List<Person> getPeople() { return people; }

	public void addPeople(int x) { // x - how many new people to add
		for (int i=0; i<x; i++) {
			numPeopleCreated++;
			Person p = new Person(numPeopleCreated);
			p.sim = this;
			people.add(p);

			// assign new people to businesses (randomly chosen);
			Business biz = randomBusiness();
			if (biz!=null) {
				p.setEmployer(biz.id);
				biz.employees.add(p);
			}
		}
	}

	public Business randomBusiness() {
		int size = businesses.size();
		if (size > 0) {
			return businesses.get( new Random().nextInt(size) );
		} else return null;
	}

	public void generatePeople(int x) {
		for (int i=0; i<x; i++) {
			people.add( new Person(i+1) );
		}
	}

	public void generateBusinesses(int x) {
		for (int i=0; i<x; i++) {
			businesses.add( new Business(i+1) );
		}
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
