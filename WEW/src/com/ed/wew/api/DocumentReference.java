package com.ed.wew.api;

import java.io.*;

/**
 * This is a handler representing one text document. The implementation of the getReader method depends on the source of the document (file in a file system, a
 * URL, a database entry etc)
 * 
 * @author Andrey Santrosyan
 * 
 */

public interface DocumentReference extends Serializable {

    /**
     * 
     * @return the name of the document
     */
    public String getName();

    /**
     * NOTE: it's better not to open the reader or any stream until this method is called.
     * 
     * Close the Reader when you are done with it!!
     * 
     * @return java.io.Reader pointing to the document.
     */
    public Reader createReader();

}
