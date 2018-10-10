package com.ed.wew.api;

public class FileAnnotatorReference extends FileDocumentReference implements AnnotatorReference {

    public FileAnnotatorReference(final String directory, final String fileName) {
        super(directory, fileName);
    }

    public FileAnnotatorReference(final String fullPath) {
        super(fullPath);
    }

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
