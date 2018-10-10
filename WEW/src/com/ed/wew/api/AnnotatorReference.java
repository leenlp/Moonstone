package com.ed.wew.api;

/**
 * A special DocumentReference that also specifies whether this is a Primary or Secondary annotation file.
 * 
 * @author Andrey Santrosyan
 * 
 */
public interface AnnotatorReference extends DocumentReference {

    public AnnotatorType getAnnotatorType();

    // public void setAnnotatorType(AnnotatorType type);
}
