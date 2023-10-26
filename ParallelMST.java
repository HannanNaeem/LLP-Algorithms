import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;


import java.util.List;
import java.util.ArrayList;


class ParallelMSTThread extends Thread{
    int pre;
    int pred;
    int [][] graph;
    int[] shortest_dist;
    List<Integer> discovering_node; // as 'Q'
    List<Integer> fixed_node; // as 'R'
    List<Integer[]> result; // as 'T'

    int[] can; // candidate array G
    boolean[] isEnsured;
    int n;
    CyclicBarrier barrier;

    

    public ParallelMSTThread(int pre, int pred, int[][] graph, 
            List<Integer> discovering_node, List<Integer> fixed_node, 
            List<Integer[]> result, int[] shortest_dist,
            int[] candidate, boolean[] isEnsured, CyclicBarrier barrier){
        this.pre = pre;
        this.pred = pred;
        this.graph = graph;

        this.discovering_node = discovering_node;
        this.fixed_node = fixed_node;
        this.result = result;
        this.shortest_dist = shortest_dist;

        this.can = candidate;
        this.isEnsured = isEnsured;

        n = candidate.length;
        this.barrier = barrier;
    }

    public int minWeightEdge(int a) {
        int result = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            if (graph[a][i] < result) {
                result = graph[a][i];
            }
        }
        return result;
    }


    public void run(){

        if (minWeightEdge(pre) >= graph[pre][pred]) {
            isEnsured[pred] = true;
            result.add(new Integer[] {pred, pre});
            fixed_node.add(pred);
        }
        else if (shortest_dist[pred] > graph[pre][pred]) {
            shortest_dist[pred] = graph[pre][pred];
            can[pred] = pre;
            boolean isCollected = true;
            for (int i : discovering_node){
                if (i == pred){
                    isCollected = false;
                }   
            }
            if (isCollected) {
                discovering_node.add(pred);
            }
        }
        
        // Wait for all writes
        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }                
    }
    
}

public class ParallelMST{

    static final int PINF = Integer.MAX_VALUE;

    public static List<Integer[]> parallelMST(int[][] graph) {

        int size = graph.length;
        int[] g = IntStream
            .generate(() -> -1)
            .limit(size)
            .toArray();
        int[] shortest_dist = IntStream
            .generate(() -> Integer.MAX_VALUE)
            .limit(size)
            .toArray();
        boolean[] array_check = new boolean[size];
        
        NodeDist waiting_node = new NodeDist(); // as 'H'
        List<Integer> discovering_node = new ArrayList<Integer>(); // as 'Q'
        List<Integer> fixed_node = new ArrayList<Integer>(); // as 'R'
        List<Integer[]> result = new ArrayList<>(); // as 'T'

        ParallelMSTThread[] threads = new ParallelMSTThread[size * size];
        CyclicBarrier barrier = new CyclicBarrier(size);

        waiting_node.insert(0, shortest_dist[0]);

        while(!waiting_node.isEmpty()) {

            int target = waiting_node.remove_min();

            if (! array_check[target]){
                fixed_node.add(target);
                array_check[target] = true;
                if (g[target] != -1) {
                    result.add(new Integer[] {target, g[target]});
                }

                while (! fixed_node.isEmpty()){

                    List<Integer> process_pre = new ArrayList<>();
                    List<Integer> process_pred = new ArrayList<>();
                    for (int i : fixed_node){
                        for (int j = 0; j < size; j++){
                            if ((i != j) 
                                    && (!array_check[j]) 
                                    && (graph[i][j] < PINF)) {
                                process_pre.add(i);
                                process_pred.add(j);
                            }
                        }
                    }

                    fixed_node.clear();

                    for (int i = 0; i < process_pre.size(); i++) {
                        threads[i] = new ParallelMSTThread(process_pre.get(i), process_pred.get(i), graph, discovering_node,
                        fixed_node, result, shortest_dist, g, array_check, barrier);
                        threads[i].start();
                    }

                    for (ParallelMSTThread thread: threads){
                        try{
                            thread.interrupt();
                            thread.join();
                        } catch (Exception e){
                            System.out.println("Interreupted");
                        }
                    }   
                }             

                for (int z : discovering_node) {
                    if(! array_check[z]){
                        waiting_node.insert(z, shortest_dist[z]);
                    }
                }
            }
        }

        return result;

    }

    public static void main(String[] args){

        int[][] graph = {{PINF, 5, 4, PINF, PINF},
                         {5, PINF, 3, 7, PINF},
                         {4, 3, PINF, 9, 11},
                         {PINF, 7, 9, PINF, 2},
                         {PINF, PINF, 11, 2, PINF}};
        List<Integer[]> result = parallelMST(graph);

        System.out.println("RESULT:");
        for (Integer[] i : result) {
            for (int j : i) {
                System.out.print(j + " ");
            }
            System.out.println();
        }    

    }
    
}