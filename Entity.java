// abstract base class for Person, Business
import java.util.*;

public abstract class Entity {
	protected Sim sim; // which simulation the person belongs to
	protected Random rand;
	protected int id;
	protected int balance;

	public Entity(int id) {
		this.id = id;
		rand = new Random();
	}
}
