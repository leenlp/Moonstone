package com.ed.wew.api;

public class ConceptImpl implements ConceptReference {

    private String cui = null;
    private String name = null;

    // Lee
    public ConceptImpl(final String name, final String cui) {
        this.name = name;
        this.cui = cui;
    }

    @Override
    public String getCUI() {
        return cui;
    }

    public void setCui(final String cui) {
        this.cui = cui;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String str = "(Concept: Name=" + name + ",CUI=" + cui + ")";
        return str;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        String aThis = name;
        String aThat = ((ConceptReference)obj).getName();
        return aThis == null ? aThat == null : aThis.equals(aThat);
    }

}
