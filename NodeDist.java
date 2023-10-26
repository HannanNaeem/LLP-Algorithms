import java.util.ArrayList;
import java.util.Collections;

public class NodeDist {
        private ArrayList<Integer> node_number;
        private ArrayList<Integer> dist_value;

        // Constructor
        public NodeDist() {
            this.node_number = new ArrayList<>();
            this.dist_value = new ArrayList<>();
        }

        // Insert method
        public void insert(int nodeNumber, int distValue) {
            node_number.add(nodeNumber);
            dist_value.add(distValue);
        }

        // Return the node with the min distance
        public int remove_min() {
            if (dist_value.isEmpty()) {
                return 0; // Or throw an exception
            }

            // Find the index of the minimum dist_value
            int minIndex = dist_value.indexOf(Collections.min(dist_value));

            // Get the corresponding node_number and dist_value
            int minNodeNumber = node_number.get(minIndex);

            // Remove them from the lists
            node_number.remove(minIndex);
            dist_value.remove(minIndex);

            // Return the node number
            return minNodeNumber;
        }

        public boolean isEmpty() {
            if (node_number.isEmpty()){
                return true;
            }
            return false;
        }
    }