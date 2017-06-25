package daris.util;

public interface CanChange {

    boolean changed();

    void addChangeListener(StateChangeListener cl);

    void removeChangeListener(StateChangeListener cl);

}
