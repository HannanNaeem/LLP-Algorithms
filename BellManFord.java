import java.util.HashSet;
import java.util.concurrent.CyclicBarrier;
import java.io.File;
import java.io.FileWriter;
import inputs.*;

class BMInitThread extends Thread{
    int tid;
    int chunk_size;
    int[] sol_array;
    boolean[] forbidden_array;

    public BMInitThread(int tid, int[] sol_array, boolean[] forbidden_array, int chunk_size){
        this.tid = tid;
        this.sol_array = sol_array;
        this.forbidden_array = forbidden_array;
        this.chunk_size = 10;
        if (chunk_size > 0){
            this.chunk_size = chunk_size;
        }
    }

    public void run(){
        int start = tid * chunk_size;
        int end = start + chunk_size;
        if (end > sol_array.length){
            end = sol_array.length;
        }

        for (int i = start; i < end; i ++){
            sol_array[i] = Integer.MAX_VALUE;
            forbidden_array[i] = true;
        }

        if (tid == 0){
            sol_array[0] = 0;
            forbidden_array[0] = false;
        }
    }
}

class BellManThread extends LLP{

    int tid;
    int[] array;
    int n;
    int[][] graph;
    int v;
    boolean[] isForbidden;
    CyclicBarrier barrier;
    HashSet<Integer> preds;

    public BellManThread(int tid, boolean[] isForbidden, int[] array, int[][] graph, CyclicBarrier barrier){
        this.tid = tid;
        this.array = array;
        n = array.length;
        this.graph = graph;
        v = graph.length;
        this.isForbidden = isForbidden;
        preds = get_preds();
        this.barrier = barrier;
    }

    private HashSet<Integer> get_preds(){

        // scan columns to get predecessors
        HashSet<Integer> result = new HashSet<Integer>();
        for(int i = 0; i < v; i++){
            if (graph[i][tid] != 0){
                result.add(i);
            }
        }
        return result;
    }

    protected boolean check_forbidden(){
        // for all predeessors of j
        for(int i : preds){
            if (array[i] == Integer.MAX_VALUE){
                return true;
            }
            if (array[tid] > array[i] + graph[i][tid]){
                return true;
            }
        }

        return false;
    }

    protected void advance(){
        int min = Integer.MAX_VALUE;
        for(int i: preds){
            int val = array[i] + graph[i][tid];
            if(array[i] == Integer.MAX_VALUE){
                val = Integer.MAX_VALUE;
            }
            min = Math.min(min, val);
        }
        if (min > 0){
            array[tid] = min;
        }
    }

    protected boolean exists_forbidden(){

        for(boolean b : isForbidden){
            if(b){
                return true;
            }
        }
        return false;
    }

    public void run(){

        isForbidden[tid] = check_forbidden();
    
        try{
            barrier.await();
        } catch (Exception e){
            e.printStackTrace();
        }
        // Always sync up before, accessing forbidden array: everyone must see the same state
        while(exists_forbidden()){

            try{
                barrier.await();
            } catch (Exception e){
                e.printStackTrace();
                break;
            }

            isForbidden[tid] = check_forbidden();

            if (isForbidden[tid]){
                advance();
            }

            try{
                barrier.await();
            } catch (Exception e){
                e.printStackTrace();
                break;
            }

        }
        
    }

}

public class BellManFord {

    public static int[] bellman_ford(int[][] graph){

        int[] sol_array = new int[graph.length];
        boolean[] forbidden_array = new boolean[graph.length];
        int chunk_size = 10;
        int total_threads = (int) Math.ceil((graph.length)/(chunk_size * 1.0));
        BMInitThread init_threads[] = new BMInitThread[total_threads];
        
        //* PARALLEL INIT */
        for(int i = 0; i < total_threads; i++){
            init_threads[i] = new BMInitThread(i, sol_array, forbidden_array, chunk_size);
            init_threads[i].start();
        }

        BellManThread[] threads = new BellManThread[sol_array.length];
        CyclicBarrier barrier = new CyclicBarrier(sol_array.length - 1, null);

        for (int i = 1; i < sol_array.length; i++){
            threads[i] = new BellManThread(i, forbidden_array, sol_array, graph, barrier);
            threads[i].start();
        }

        for (int i = 1; i < sol_array.length; i++){
            try{
                threads[i].join();
            } catch (Exception e){
                System.out.println("Interreupted");
            }
        }

        return sol_array;

    }

    public static void main(String[] args){
        // PARSING ARG START ---------------------
        String[] parsed_args = ParseInput.parse_args(args, "BellManFord");
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
            int[] sol_array = BellManFord.bellman_ford(graph);
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