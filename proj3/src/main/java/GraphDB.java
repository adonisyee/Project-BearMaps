import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    protected final HashMap<Long, Node> nodes = new HashMap<>();
    protected HashSet<Long> vertices = new HashSet<>();
    protected HashMap<Long, Way> ways = new HashMap<>();
    private HashSet<Long> toClean = new HashSet<>();
    protected WordTree locationNames = new WordTree();
    protected LocationList locations = new LocationList();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }
    protected void addNode(Node n) {
        this.nodes.put(n.id, n);
        this.vertices.add(n.id);
    }
    protected void addEdge(Node n, Node neighbor) {
        n.neighbors.add(neighbor.id);
        neighbor.neighbors.add(n.id);
        //ways.put(n.id, n.neighbors);
        //ways.put(neighbor.id, neighbor.neighbors);
    }
    private void removeEdge(Node n, Node neighbor) {
        n.neighbors.remove(neighbor.id);
        neighbor.neighbors.remove(n.id);
    }

    public List<Map<String, Object>> getLocations(String locationName) {
        LinkedList<Map<String, Object>> list = new LinkedList<>();
        if (!(locationName == null)) {
            LinkedList<GraphDB.Node> l = this.locations.locations.get(locationName);
            for (GraphDB.Node n : l) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("lat", n.lat);
                temp.put("lon", n.lon);
                temp.put("name", n.tag.get("name"));
                temp.put("id", n.id);
                list.addFirst(temp);
            }
            return list;
        }
        return new LinkedList<>();
    }

    public List<String> getLocationsByPrefix(String prefix) {
        if (!(prefix == null)) {
            return this.locationNames.wordNames.get(prefix);
        }
        return new LinkedList<>();
    }

    static class Node {
        protected long id;
        protected double lat;
        protected double lon;
        protected HashMap<String, String> tag;
        protected HashSet<Long> neighbors;
        protected double priority;

        //protected double distFromStart;
        //protected Node lastVertex;

        Node(Long id) {
            this.id = id;
            this.lat = 0;
            this.lon = 0;
            this.tag = new HashMap<>();
            this.neighbors = new HashSet<>();
            this.priority = Double.POSITIVE_INFINITY;
            //this.lastVertex = null;
        }
    }

    static class Way {
        private long startID;
        protected HashMap<String, String> tag;
        protected boolean valid;
        protected ArrayList<Long> possibleWay;

        Way(long startID) {
            this.startID = startID;
            this.tag = new HashMap<>();
            this.valid = false;
            this.possibleWay = new ArrayList<>();
        }
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (long vertex : vertices) {
            if (nodes.get(vertex).neighbors.isEmpty()) {
                toClean.add(vertex);
                nodes.remove(vertex);
            }
        }
        for (long l : toClean) {
            vertices.remove(l);
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return vertices;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).neighbors;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
<<<<<<< HEAD

        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));
=======
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);
>>>>>>> 2cfc2b1dac98fb8b9d02a37db8a6a061ba4c02f5

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
<<<<<<< HEAD
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));
=======
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

>>>>>>> 2cfc2b1dac98fb8b9d02a37db8a6a061ba4c02f5
        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        long minID = java.util.Collections.max(vertices);
        Node start = nodes.get(minID);
        Node target = new Node(1L);
        addNode(target);
        vertices.remove(target.id);
        target.lon = lon;
        target.lat = lat;
        double minD = distance(target.id, start.id);
        //System.out.println("minD: " + minD);
        for (long vertex : vertices) {
            double currD = distance(target.id, vertex);
            //System.out.println("currD: " + currD);
            //System.out.println("vertex: " + vertex);
            if (currD < minD) {
                minD = currD;
                minID = vertex;
            }
        }
        nodes.remove(target.id);
        return minID;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.get(v).lat;
    }
}
