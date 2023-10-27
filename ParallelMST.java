import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;


import java.util.List;
import java.io.File;
import java.util.ArrayList;


class ParallelMSTThread extends Thread{
    int pre;
    int pred;
    int [][] graph;
    int[] shortest_dist;
    List<Integer> discovering_node; // as 'Q'
    List<Integer> fixed_node; // as 'R'
    int[] result; // as 'T'

    int[] can; // candidate array G
    boolean[] isEnsured;
    int n;
    CyclicBarrier barrier;

    

    public ParallelMSTThread(int pre, int pred, int[][] graph, 
            List<Integer> discovering_node, List<Integer> fixed_node, 
            int[] result, int[] shortest_dist,
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
            if ((graph[a][i] < result) && (graph[a][i] != -1)) {
                result = graph[a][i];
            }
        }
        return result;
    }


    public void run(){

        if (minWeightEdge(pre) >= graph[pre][pred]) {
            isEnsured[pred] = true;
            result[pred] = graph[pred][pre];
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
            // e.printStackTrace();
        }                
    }
    
}

public class ParallelMST{

    public static int[] parallelMST(int[][] graph) {

        int size = graph.length;
        int[] g = IntStream
            .generate(() -> -1)
            .limit(size)
            .toArray();
        int[] result = IntStream
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

        ParallelMSTThread[] threads = new ParallelMSTThread[size * size];
        CyclicBarrier barrier = new CyclicBarrier(size);

        waiting_node.insert(0, shortest_dist[0]);

        while(!waiting_node.isEmpty()) {

            int target = waiting_node.remove_min();

            if (! array_check[target]){
                fixed_node.add(target);
                array_check[target] = true;
                if (g[target] != -1) {
                    result[target] = graph[target][g[target]];
                }

                while (! fixed_node.isEmpty()){

                    List<Integer> process_pre = new ArrayList<>();
                    List<Integer> process_pred = new ArrayList<>();
                    for (int i : fixed_node){
                        for (int j = 0; j < size; j++){
                            if ((i != j) 
                                    && (!array_check[j]) 
                                    && (graph[i][j] != -1)) {
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
                            if (thread != null){
                                thread.interrupt();
                                thread.join();
                            }
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
        // PARSING ARGS START -----------------------------
        boolean run_all = true; // single test or all?
        File input_file = new File("./inputs/PrimsInput.txt");
        int input_number = 0;
        boolean write_out = false;

        for(int i = 0; i < args.length; i++){
            if (i == 0){
                // Expect file path
                input_file = new File(args[i]);
                System.out.println("File set to " + args[i]);

            }

            if (i == 1){
                // Expect single/s or all
                if(args[i].startsWith("-s")){
                    run_all = false;
                    System.out.println("run all  " + run_all);

                }
            }

            if (!run_all && i == 2){
                // Followed by single expect input/test number
                try{
                    input_number = Integer.parseInt(args[i]);
                    System.out.println("input number  " + args[i]);

                    if (input_number < 0){
                        throw new Exception("Test/Input number cannot be less than 0");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
            if(args[i].startsWith("-o")){
                write_out = true;
            }
        }
        // PARSING END -----------------------------

        do {
            int[][] graph = ParseInput.parse_2D(input_file, input_number, "Input");
            input_number++;
            if (graph == null){
                break;
            }
            System.out.println("Running: " + (input_number-1));

            int[] sol_array = parallelMST(graph);

            System.out.println("RESULT:");
            for (int i : sol_array)
                System.out.print(i + " ");
            System.out.println();
            System.out.println();
        } while(run_all);

    }
    
}