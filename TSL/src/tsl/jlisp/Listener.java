package tsl.jlisp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Listener {
	JLisp jLisp = null;


	Listener(JLisp jl) {
		this.jLisp = jl;
	}

	public void readEvalPrint() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String str = "";
			boolean quit = false;
			while (!quit) {
				System.out.print("JLisp> ");
				str = in.readLine();
				if ("quit".equals(str)) {
					return;
				}
				Object rv = this.jLisp.evalString(str);
				System.out.println(rv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
