package daris.client.mf.access;

public class LocalAccessToken extends AccessToken {

    public LocalAccessToken(String type, String name, Permission p) {
        this(type, name, p,true);
    }

    public LocalAccessToken(String type, String name, Permission p,boolean haveAccess) {
        super(type, name, p);
        super.setHaveAccess(haveAccess);
    }

}