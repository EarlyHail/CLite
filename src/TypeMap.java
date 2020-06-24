import java.util.*;

public class TypeMap extends HashMap<Variable, ProtoType> {

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
	public TypeMap() {
	}

	public TypeMap(Variable key, ProtoType val) {
		put(key, val);
	}

	public TypeMap onion(Variable key, ProtoType val) {
		put(key, val);
		return this;
	}

	public TypeMap onion(TypeMap t) {
		for (Variable key : t.keySet()) {
			put(key, t.get(key));
		}
		return this;
	}

	public void display() {
		System.out.print("{ ");
		for (Variable v : this.keySet()) {
			if (this.get(v) instanceof Type) {
				System.out.print(v.toString() + ":" + this.get(v).toString()+" ");
			} else {
				System.out.print("{ ");
				System.out.print(v.toString() + ", ");
				this.get(v).display();
				System.out.print(" } ");
			}
		}
		System.out.println("}");
	}
}
