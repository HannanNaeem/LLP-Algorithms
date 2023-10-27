import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;


import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import inputs.*;

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
        // PARSING ARG START ---------------------
        String[] parsed_args = ParseInput.parse_args(args, "Prims");
        boolean run_all = Boolean.parseBoolean(parsed_args[0]); // single test or all?
        File input_file = new File(parsed_args[1]);
        int input_number = Integer.parseInt(parsed_args[2]);
        boolean write_out = Boolean.parseBoolean(parsed_args[3]);
        boolean run_tests = Boolean.parseBoolean(parsed_args[4]);;
        File out_file = new File(parsed_args[5]);
        File test_file = new File(parsed_args[6]);
        // PARSING END -----------------------------

        int failed_counter = 0;
        if (write_out){
            try{
                out_file.delete();
            } catch (Exception e){
                
            }
        }

        do {
            int[][] graph = ParseInput.parse_2D(run_tests? test_file: input_file, input_number, "Input");
            input_number++;
            if (graph == null){
                break;
            }
            System.out.println("Running: " + (input_number-1));

            long startTime = System.nanoTime();
            int[] sol_array = parallelMST(graph);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime)/1000000.0;  //divide by 1000000 to get milliseconds.

            if (write_out){                
                try{
                    FileWriter writer = new FileWriter(out_file, true);
                    GenerateOBST.write_array1D(sol_array, sol_array.length, writer, "Output(" + Integer.toString(input_number-1) + ")");
                    writer.write("Time: " + Double.toString(duration) + " ms\n");
                    writer.close();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (run_tests){
                boolean passed = LLP.run_test_case(test_file, sol_array, input_number-1);
                if(passed){
                    System.out.println("TEST: OK");
                } else {
                    failed_counter++;
                }
            }

            System.out.println("RESULT:");
            for (int i : sol_array)
                System.out.print(i + " ");
            System.out.println("\nTime: " + Double.toString(duration));
            System.out.println();
        } while(run_all);

        if(run_tests){
            System.out.println("FAILURES: "+ Integer.toString(failed_counter));
        }
    }
    
}