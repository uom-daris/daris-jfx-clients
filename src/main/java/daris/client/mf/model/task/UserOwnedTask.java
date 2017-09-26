package daris.client.mf.model.task;

import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.mf.authentication.UserRef;

public class UserOwnedTask extends Task {
    private UserRef _user;

    protected UserOwnedTask(Element xe) throws Throwable {
        super(xe);
        XmlDoc.Element ue = xe.element("user");
        if (ue == null) {
            _user = null;
        } else {
            _user = new UserRef(ue);
        }
    }

    public UserRef owner() {
        return _user;
    }

}
