import java.util.HashMap;
import java.util.LinkedList;

public class LocationList {
    protected HashMap<String, LinkedList<GraphDB.Node>> locations;
    LocationList() {
        this.locations = new HashMap<>();
    }

    public void addLocation(String location, GraphDB.Node n) {
        String cleaned = GraphDB.cleanString(location);
        if (this.locations.containsKey(location)) {
            LinkedList<GraphDB.Node> l = this.locations.get(location);
            l.addFirst(n);
        } else {
            LinkedList<GraphDB.Node> startsWith = new LinkedList<>();
            startsWith.addFirst(n);
            this.locations.put(location, startsWith);
        }



    }
}
