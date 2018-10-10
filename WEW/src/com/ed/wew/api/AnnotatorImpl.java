package com.ed.wew.api;

@Deprecated
public class AnnotatorImpl extends DocumentImpl implements AnnotatorReference {

    AnnotatorType type;

    @Override
    public AnnotatorType getAnnotatorType() {
        return type;
    }

    public void setAnnotatorType(final AnnotatorType type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return type == AnnotatorType.Primary;
    }

}
