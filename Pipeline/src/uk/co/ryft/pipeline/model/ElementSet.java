
package uk.co.ryft.pipeline.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class ElementSet implements List<Element>, Serializable {

    private static final long serialVersionUID = 8570770847982116049L;
    private List<Element> mElements;
    
    public ElementSet() {
        mElements = new LinkedList<Element>();
    }
    
    @Override
    public boolean add(Element object) {
        return mElements.add(object);
    }

    @Override
    public void add(int location, Element object) {
        mElements.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends Element> arg0) {
        return mElements.addAll(arg0);
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends Element> arg1) {
        return mElements.addAll(arg1);
    }

    @Override
    public void clear() {
        mElements.clear();
    }

    @Override
    public boolean contains(Object object) {
        return mElements.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return mElements.contains(arg0);
    }

    @Override
    public Element get(int location) {
        return mElements.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return mElements.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return mElements.isEmpty();
    }

    @Override
    public Iterator<Element> iterator() {
        return mElements.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return lastIndexOf(object);
    }

    @Override
    public ListIterator<Element> listIterator() {
        return mElements.listIterator();
    }

    @Override
    public ListIterator<Element> listIterator(int location) {
        return mElements.listIterator(location);
    }

    @Override
    public Element remove(int location) {
        return mElements.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return mElements.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        return mElements.removeAll(arg0);
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        return mElements.retainAll(arg0);
    }

    @Override
    public Element set(int location, Element object) {
        return mElements.set(location, object);
    }

    @Override
    public int size() {
        return mElements.size();
    }

    @Override
    public List<Element> subList(int start, int end) {
        return mElements.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return mElements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return mElements.toArray(array);
    }

}
