import java.util.*;

public class State implements Cloneable {
	// Defines the set of variables and their associated values
	// that are active during interpretation
	Environment gamma;
	Memory mu;
	int a;
	int slink;
	int dlink;

	public State() {
		this.gamma = new Environment();
		this.mu = new Memory(Value.mkValue(Type.UNUSED), 20);
		this.a = 0;
		this.slink = 0;
		this.dlink = 0;
	}

	public State(State sigmag) {
		this.gamma = (Environment) sigmag.gamma.clone();
		this.mu = (Memory) sigmag.mu.clone();
		this.a = sigmag.a;
		this.slink = sigmag.slink;
		this.dlink = sigmag.dlink;
	}

	public State(Variable key, Value val) {
		this.gamma = new Environment();
		this.mu = new Memory(Value.mkValue(Type.UNUSED), 20);
		this.a = 0;
		this.slink = 0;
		this.dlink = 0;
		this.gamma.add(new Pair(key, a));
		this.mu.set(a, val);
		a++;
	}

	public State minus(int num) {
		for (int i = 0; i < num; i++) {
//			gamma.remove(this.a-i-1);
			gamma.remove(gamma.size() - 1);
		}
		return this;
	}

	public State allocate(Declarations ds) {
		for (Declaration d : ds) {
			Value val = null;
			if (d.t == Type.INT)
				val = new IntValue();
			if (d.t == Type.BOOL)
				val = new BoolValue();
			if (d.t == Type.CHAR)
				val = new CharValue();
			if (d.t == Type.FLOAT)
				val = new FloatValue();
			gamma.add(new Pair(d.v, a));
			mu.set(a, val);
			a++;
		}
		return this;
	}

	public State deallocate(Declarations ds) {
		for (Declaration d : ds) {
			for (Pair p : gamma) {
				if (p.var.toString().equals(d.v.toString())) {
					int addr = p.addr;
					gamma.remove(p);
					mu.set(addr, new IntValue());
					a--;
					break;
				}
			}
		}
		return this;
	}

	public State onion(Variable key, Value val) {
		boolean found = false;
		for (Pair p : gamma) {
			if (p.var.toString().equals(key.toString())) {
				found = true;
				mu.set(p.addr, val);
				break;
			}
		}
		if (found == false) {
			gamma.add(new Pair(key, a));
			mu.set(a, val);
			a++;
		}
		return this;
	}

	public State onion(State t) {
		// 현재 객체에 t를 update 해야함, t가 최신
		boolean found = false;
		for (Pair p : t.gamma) {
			found = false;
			for (Pair thisP : this.gamma) {
				if (thisP.var.toString().equals(p.var.toString())) {
					if (!t.mu.get(p.addr).toString().equals("undef")) {
						mu.set(thisP.addr, t.mu.get(p.addr));
						found = true;

					}
				}
			}
			if (found == false) {
				gamma.add(new Pair(p.var, a));
				mu.set(a, t.mu.get(p.addr));
				a++;
			}
		}
		return this;
	}

	public int findAddrByVar(Variable var) {
		for (Pair temp : gamma) {
			if (temp.var.toString().equals(var.toString())) {
				return temp.addr;
			}
		}
		return -1;
	}

	public void display() {
		for (int i = 0; i < gamma.size(); i++) {
			System.out.print("<" + gamma.get(i).var + ", " + gamma.get(i).addr + ">");
		}
		System.out.println();
		for (int i = 0; i < mu.size(); i++) {
			System.out.print("<" + i + ", " + mu.get(i) + ">");
		}
		System.out.println();
	}
}

class Environment extends ArrayList<Pair> {// hashmap으롷
	Environment() {
	}
}

class Pair {
	Variable var;
	int addr;

	Pair(Variable var, int addr) {
		this.var = var;
		this.addr = addr;
	}
}

class Memory extends ArrayList<Value> {
	int size;

	Memory(Value v, int s) {
		size = s;
		for (int i = 0; i < size; i++) {
			this.add(v);
		}
	}
}
