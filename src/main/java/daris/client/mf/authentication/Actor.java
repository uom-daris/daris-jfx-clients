package daris.client.mf.authentication;

public class Actor {

    private String _actorName;

    private String _actorType;

    public String actorType() {
        return _actorType;
    }

    public String actorName() {
        return _actorName;
    }

    public Actor(String actorName, String actorType) {
        _actorType = actorType;
        _actorName = actorName;
    }

}
