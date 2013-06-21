package uk.co.ryft.pipeline.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class Model implements Iterable<Element> {
    
    protected List<ModelListener> mListeners;
    protected TreeSet<Element> mElements;
    
    public Model() {
        mListeners = new LinkedList<ModelListener>();
        mElements = new TreeSet<Element>();
    }
    
    public TreeSet<Element> getAllElements() {
        return mElements;
    }
    
    public void addSceneElement(Element elem) {
        mElements.add(elem);
    }
    
    public void updateSceneElement(Element elem) {
        // TODO: This.
    }
    
    public void registerListener(ModelListener l) {
        mListeners.add(l);
    }
    
    public void setUpdated() {
        notifyListeners();
    }
    
    protected void notifyListeners() {
        for (ModelListener l : mListeners) l.elementsChanged(mElements);
    }

    @Override
    public Iterator<Element> iterator() {
        return mElements.iterator();
    }

}
