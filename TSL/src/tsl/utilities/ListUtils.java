package tsl.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import tsl.expression.term.Term;

public class ListUtils {
	
	public static List listify(Object value) {
		ArrayList l = new ArrayList(0);
		l.add(value);
		return l;
	}
	
	public static List add(List v, Object value) {
		if (value != null) {
			if (v == null) {
				v = new ArrayList(0);
			}
			v.add(value);
		}
		return v;
	}
	
	public static List appendIfNot(List v1, List v2) {
		if (v2 != null && v1 != v2) {
			if (v1 == null) {
				v1 = new ArrayList(0);
			}
			for (Iterator i = v2.iterator(); i.hasNext();) {
				Object o = i.next();
				if (!v1.contains(o)) {
					v1.add(o);
				}
			}
		}
		return v1;
	}
	
	public static List appendNewIfNot(List v1, List v2) {
		if (v1 == null) {
			return v2;
		}
		if (v2 == null) {
			return v1;
		}
		v1 = new ArrayList(v1);
		for (Iterator i = v2.iterator(); i.hasNext();) {
			Object o = i.next();
			if (!v1.contains(o)) {
				v1.add(o);
			}
		}
		return v1;
	}
	
	public static List appendNew(List v1, List v2) {
		List rv = null;
		if (v1 != null) {
			rv = new ArrayList(v1);
		}
		if (v2 != null) {
			for (Iterator i = v2.iterator(); i.hasNext();) {
				Object o = i.next();
				rv = add(rv, o);
			}
		}
		return rv;
	}
	
	public static List append(List v1, List v2) {
		if (v1 == null) {
			return v2;
		}
		if (v2 == null) {
			return v1;
		}
		v1.addAll(v2);
		return v1;
	}
	
	// 5/16/2014
	public static List rest(List v1) {
		List rest = null;
		if (v1 != null && v1.size() > 1) {
			rest = v1.subList(1, v1.size());
		}
		return rest;
	}
	
	public static Object getFirst(List l) {
		if (l != null && !l.isEmpty()) {
			return l.get(0);
		}
		return null;
	}
	
	public static Object getLast(List l) {
		if (l != null && !l.isEmpty()) {
			return l.get(l.size()-1);
		}
		return null;
	}
	
	public static String[] listToStringArray(ArrayList<String> v) {
		if (v != null) {
			String[] array = new String[v.size()];
			v.toArray(array);
			return array;
		}
		return null;
	}
	

}
