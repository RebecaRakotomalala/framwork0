package mg.itu.prom16;

import java.util.HashMap;

public class CustomSession {
    private HashMap<String, Object> values;

    public CustomSession() {
        this.values = new HashMap<>();
    }

    public CustomSession(HashMap<String, Object> values) {
        this.values = values;
    }

    public void add(String key, Object value) {
        if (!values.containsKey(key)) {
            values.put(key, value);
        } else {
            System.out.println("Key already exists. Use update() to modify the value.");
        }
    }

    public void remove(String key) {
        if (values.containsKey(key)) {
            values.remove(key);
        } else {
            System.out.println("Key does not exist.");
        }
    }

    public void update(String key, Object newValue) {
        if (values.containsKey(key)) {
            values.put(key, newValue);
        } else {
            System.out.println("Key does not exist. Use add() to add the key-value pair.");
        }
    }

    public Object get(String key) {
        return values.get(key);
    }
}
