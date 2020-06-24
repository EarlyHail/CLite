
public class ProtoType {
	Type type; // ��ȯ��
	Declarations params;

	ProtoType() {
	}

	ProtoType(Type t, Declarations p) {
		this.type = t;
		this.params = p;
	}

	public void display() {
		System.out.print(type);
		if (!params.isEmpty()) {
			System.out.print(" params : ");
			params.toString2();
		}
	}
}
