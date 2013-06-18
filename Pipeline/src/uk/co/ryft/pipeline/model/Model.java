package uk.co.ryft.pipeline.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Model implements Iterable<Element> {
    
    protected List<Listener> listeners;
    protected List<Element> elements;
    
    public Model() {
        listeners = new LinkedList<Listener>();
        elements = new LinkedList<Element>();
    }
    
    public void addSceneElement(Element elem) {
        elements.add(elem);
    }
    
    public void registerListener(Listener l) {
        listeners.add(l);
    }
    
    public void setUpdated() {
        notifyListeners();
    }
    
    protected void notifyListeners() {
        for (Listener l : listeners)
            l.elementsChanged(elements);
    }

    @Override
    public Iterator<Element> iterator() {
        return elements.iterator();
    }

}
