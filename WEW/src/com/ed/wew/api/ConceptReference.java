package com.ed.wew.api;

import java.io.*;

/**
 * A reference to Concept object. Will be used as a key in Maps.
 * 
 * 
 * Later might be extended to connect to Concept object from Knowledge Author.
 * 
 * @author Andrey Santrosyan
 * 
 */
public interface ConceptReference extends Serializable {
    public String getCUI();
    public String getName();

}
