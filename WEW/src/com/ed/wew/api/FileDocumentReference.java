package com.ed.wew.api;

import java.io.*;

public class FileDocumentReference implements DocumentReference {
    private String directory;
    private String name;
    private static final String sep = "/";

    public FileDocumentReference(final String fullPath) {

        name = fullPath.substring(fullPath.lastIndexOf(FileDocumentReference.sep) + 1);
        directory = fullPath.substring(0, fullPath.lastIndexOf(FileDocumentReference.sep));
    }

    public FileDocumentReference(final String directory, final String fileName) {
        this.directory = directory;
        name = fileName;
    }

    @Override
    public Reader createReader() {
        try {
            return new FileReader(directory + FileDocumentReference.sep + name);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDirectory() {
        return directory;
    }

    // public void setDirectory(final String directory) {
    // this.directory = directory;
    // }

    @Override
    public String getName() {
        return name;
    }

    // public void setName(final String name) {
    // this.name = name;
    // }

}
