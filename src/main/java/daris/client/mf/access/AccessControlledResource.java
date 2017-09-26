package daris.client.mf.access;

public interface AccessControlledResource {

    public String resourceName();

    public AccessToken accessToken(Permission p);

    public boolean have(Permission p);

}
