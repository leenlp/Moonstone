/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moonstone.utility;

import java.text.NumberFormat;

/**
 *
 * @author leechristensen
 */
public class ThreadUtils {

	private static long lowestFreeMemory = 1000000000000L;

	public static void printFreeMemory(String prefix) {
		printFreeMemory(prefix, true);
	}

	public static void printFreeMemory(String prefix, boolean onlyWithDrop) {
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long max = runtime.maxMemory();
		long allocated = runtime.totalMemory();
		long free = runtime.freeMemory();
		long current = (free + (max - allocated)) / 1024;
		long diff = Math.abs(current - lowestFreeMemory);
		long lowest = lowestFreeMemory;
		
		if (current < lowestFreeMemory) {
			System.out.println("\n" + 
					prefix + ": Lowest=" + lowestFreeMemory + "K, Current=" + current + "K, Diff=" + diff);
			lowestFreeMemory = current;
		}
	}
}
