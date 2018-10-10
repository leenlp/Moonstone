package com.ed.wew.api;

import java.io.Reader;
import java.util.Comparator;

@Deprecated
public class DocumentImpl implements DocumentReference {
    private Reader reader;
    private String name;
    private String text;

    // Lee
    public DocumentImpl() {}

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Reader createReader() {
        return reader;
    }

    public void setReader(final Reader reader) {
        this.reader = reader;
    }
    
    public static class DocumentImplSorter implements Comparator {
    	public int compare(Object o1, Object o2) {
    		DocumentImpl d1 = (DocumentImpl) o1;
    		DocumentImpl d2 = (DocumentImpl) o2;
    		return d1.getName().compareTo(d2.getName());
    	}
    }

}
