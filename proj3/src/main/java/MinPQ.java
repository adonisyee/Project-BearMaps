import java.util.Comparator;
import java.util.PriorityQueue;


public class MinPQ {
    protected PriorityQueue<GraphDB.Node> MinPQ;
    public MinPQ() {

        Comparator<GraphDB.Node> comparator = new Comparator<GraphDB.Node>() {
            @Override
            public int compare(GraphDB.Node o1, GraphDB.Node o2) {
                Double weight1 = o1.priority;
                Double weight2 = o2.priority;
                return weight1.compareTo(weight2);
            }
//                if (o1.priority > o2.priority) {
//                    return -1;
//                } else if (o2.priority > o1.priority) {
//                    return 1;
//                } else {
//                    return 0;
//                }
//            }
        };
        MinPQ = new PriorityQueue<>(comparator);
    }
}
