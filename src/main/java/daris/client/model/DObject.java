package daris.client.model;

public interface DObject {

    public static enum Type {
        PROJECT, SUBJECT, EX_METHOD, STUDY, DATASET;

        @Override
        public String toString() {
            return name().toLowerCase().replace('_', '-');
        }

        public static Type fromString(String type) {
            if (type != null) {
                Type[] vs = values();
                for (Type v : vs) {
                    if (v.toString().equalsIgnoreCase(type)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    String cid();

    String name();

    String description();
    
    Type type();
}
