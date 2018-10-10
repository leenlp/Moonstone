package com.ed.wew.api;

import java.io.*;

public interface Annotation extends Serializable {

    public String getText();

    public String getSemanticType();

    public String getClassification();

    public int getStart();

    public int getEnd();

    public DocumentReference getDocumentReference();

    public ConceptReference getConceptReference();

    public AnnotatorReference getAnnotatorReference();

}