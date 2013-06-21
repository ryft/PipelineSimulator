package uk.co.ryft.pipeline.model;

import java.util.TreeSet;

public interface ModelListener {
    
    public void elementsChanged(TreeSet<Element> elems);

}
