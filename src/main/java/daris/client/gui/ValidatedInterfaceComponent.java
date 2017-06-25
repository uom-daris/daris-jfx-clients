package daris.client.gui;

import java.util.ArrayList;
import java.util.List;

import daris.util.IsValid;
import daris.util.MustBeValid;
import daris.util.StateChangeListener;
import daris.util.Validity;

public abstract class ValidatedInterfaceComponent implements MustBeValid, StateChangeListener, InterfaceComponent {

    private List<MustBeValid> _mbvs;
    private List<StateChangeListener> _scls;

    public ValidatedInterfaceComponent() {
    }

    protected void addMustBeValid(MustBeValid mbv) throws Throwable {
        addMustBeValid(mbv, true);
    }

    protected void addMustBeValid(MustBeValid mbv, boolean notify) throws Throwable {
        if (_mbvs == null) {
            _mbvs = new ArrayList<MustBeValid>(2);
        }
        mbv.addChangeListener(this);
        _mbvs.add(mbv);
        if (notify) {
            notifyOfChangeInState();
        }
    }

    protected void removeMustBeValid(MustBeValid mbv) throws Throwable {
        if (_mbvs == null) {
            return;
        }
        mbv.removeChangeListener(this);
        _mbvs.remove(mbv);
        notifyOfChangeInState();
    }

    protected void removeAllMustBeValid() throws Throwable {
        if (_mbvs == null) {
            return;
        }
        _mbvs.clear();
        notifyOfChangeInState();
    }

    @Override
    public void addChangeListener(StateChangeListener listener) {
        if (_scls == null) {
            _scls = new ArrayList<StateChangeListener>(2);
        }
        _scls.add(listener);
    }

    @Override
    public void removeChangeListener(StateChangeListener listener) {
        if (_scls == null) {
            return;
        }
        _scls.remove(listener);
    }

    @Override
    public Validity valid() {
        if (_mbvs == null) {
            return IsValid.INSTANCE;
        }
        for (MustBeValid mbv : _mbvs) {
            Validity v = mbv.valid();
            if (!v.valid()) {
                return v;
            }
        }
        return IsValid.INSTANCE;
    }

    @Override
    public boolean changed() {
        if (_mbvs == null) {
            return false;
        }
        for (MustBeValid mbv : _mbvs) {
            if (mbv.changed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void notifyOfChangeInState()  {
        if (_scls == null) {
            return;
        }
        for (StateChangeListener cl : _scls) {
            cl.notifyOfChangeInState();
        }
    }

}
