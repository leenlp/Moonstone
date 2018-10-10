package tsl.tsllisp;

import java.util.Vector;

public class Test {

	public static void main(String[] args) {
		Vector v = new Vector(0);
		v.add(1f);
		v.add(2f);
		try {
			Object rv = TLisp.getTLisp().applyJLet("'(jlet (x y) (+ x y))", v);
			System.out.println("Result=" + rv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
