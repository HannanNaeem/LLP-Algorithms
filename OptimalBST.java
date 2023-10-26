import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.CyclicBarrier;
import inputs.*;

class TID{
    int i;
    int j;
    
    public TID(int i, int j){
        this.i = i;
        this.j = j;
    }
}

class OBSTInitThread extends Thread {
    int tid;
    int[][] sol_table;
    int[] freq_arr;
    int size;

    public OBSTInitThread(int tid, int[] freq_arr, int[][] sol_table, int size){
        this.tid = tid;
        this.freq_arr = freq_arr;
        this.sol_table = sol_table;
        this.size = size;
    }

    public void run(){
        // each thread initializes a row
        for (int i = tid; i < size; i++){
            if (tid == i){
                sol_table[tid][i] = freq_arr[i];
            } else {
                sol_table[tid][i] = 0;
            }
        }
    }
}

class OBSTThread extends LLP {

    // member vars
    TID tid;
    int j_start;
    int[] freq_arr;
    int size;
    int[][] sol_table;
    int sum;
    CyclicBarrier barrier;

    // Constructor
    public OBSTThread(TID tid, int[] freq_arr, int[][] sol_table, CyclicBarrier barrier){
        this.tid = tid;
        this.j_start = tid.j;
        this.freq_arr = freq_arr;
        size = freq_arr.length;
        this.sol_table = sol_table;
        this.barrier = barrier;
    }

    protected boolean check_forbidden(){
        int min = Integer.MAX_VALUE;

        for (int k = tid.i; k <= tid.j; k++){
            int left_i = tid.i;
            int left_j = k - 1;
            int left_val = 0;
            if (left_j >= 0 && left_i <= left_j){
                left_val = sol_table[left_i][left_j];
            }
            int right_i = k + 1;
            int right_j = tid.j;
            int right_val = 0;
            if (right_i < size && right_i <= right_j){
                right_val = sol_table[right_i][right_j];
            }

            if(left_val + sum + right_val < min){
                min = left_val + sum + right_val;
            }
        }

        if(sol_table[tid.i][tid.j] < min){
            return true;
        }

        return false;
        
    }

    protected void advance(){
        int min = Integer.MAX_VALUE;

        for(int k = tid.i; k <= tid.j; k++){
            int left_i = tid.i;
            int left_j = k - 1;
            int left_val = 0;
            if (left_j >= 0 && left_i <= left_j){
                left_val = sol_table[left_i][left_j];
            }
            int right_i = k + 1;
            int right_j = tid.j;
            int right_val = 0;
            if (right_i < size && right_i <= right_j){
                right_val = sol_table[right_i][right_j];
            }

            if (left_val + right_val < min){
                min = left_val + right_val;
            }
            
        }

        // add sum and update value
        sol_table[tid.i][tid.j] = min + sum;
        

    }

    // We do not need this. As the no. of steps are pre-countable
    protected boolean exists_forbidden(){
        return true;
    }

    // utility
    public void run(){
        for(int stride = 0; stride < size - 1; stride ++){
            
            tid.j = j_start + stride;
            // reading phase (check forbidden)
            boolean forbidden = false;
            if(tid.j < size){
                sum = ParallelReduce.parallel_reduce(ParallelCopy.parallel_copy(this.freq_arr, tid.i, tid.j, 10));
                forbidden = check_forbidden();
            }

            try{
                barrier.await();
            } catch (Exception e){
                e.printStackTrace();
                break;
            }

            // write
            if (tid.j < size && forbidden){
                advance();
            }

            // Sync up
            try{
                barrier.await();
            } catch (Exception e){
                e.printStackTrace();
                break;
            }

        }
    }

}

public class OptimalBST{

    public static int[][] optimal_bst(int[] freq_array){
        int size = freq_array.length;
        int[][] sol_table = new int[size][size];

        // * INIT
        OBSTInitThread[] init_threads = new OBSTInitThread[size];
        // size = rows (since square)
        for(int i = 0; i < size; i++){
            init_threads[i] = new OBSTInitThread(i, freq_array, sol_table, size);
            init_threads[i].start();
        }

        // wait for threads to finish
        for(int i = 0; i < size; i++){
            try{
                init_threads[i].join();
            } catch (Exception e){
                System.out.println("Interrupted");
            }
        }


        //* RUN THREADS
        OBSTThread[] threads = new OBSTThread[size-1]; //
        CyclicBarrier barrier = new CyclicBarrier(size-1);

        for(int i = 0; i < size-1; i++){
            threads[i] = new OBSTThread(new TID(i, i+1), freq_array, sol_table, barrier);
            threads[i].start();
        }

        // wait for threads to finish
        for(int i = 0; i < size -1; i++){
            try{
                threads[i].join();
            } catch (Exception e){
                System.out.println("Interrupted");
            }
        
        }

        return sol_table;
    }

    public static void main(String[] args){
        // PARSING ARGS START -----------------------------
        String[] parsed_args = ParseInput.parse_args(args, "OBST");
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
            int[] freq = ParseInput.parse_1D(run_tests? test_file : input_file, input_number, "Input");
            input_number++;
            if (freq == null){
                break;
            }
            System.out.println("Running: " + (input_number-1));
            
            long startTime = System.nanoTime();
            int[][] sol_table = optimal_bst(freq);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime)/1000000.0;  //divide by 1000000 to get milliseconds.
            
            // Write output and time to file
            if (write_out){                
                try{
                    FileWriter writer = new FileWriter(out_file, true);
                    GenerateOBST.write_array1D(sol_table[0], sol_table[0].length, writer, "Output(" + Integer.toString(input_number-1) + ")");
                    writer.write("Time: " + Double.toString(duration) + " ms\n");
                    writer.close();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            // Are we running test suite? Get expected output and compare
            
            if (run_tests ){
                boolean passed = LLP.run_test_case(test_file, sol_table[0], input_number-1);
                if(passed){
                    System.out.println("TEST: OK");
                } else {
                    failed_counter++;
                }
            }
            System.out.println("RESULT:");
            for(int j = 0; j < freq.length; j++){
                System.out.print(sol_table[0][j] + " ");
            }
            System.out.println("\nTime: " + Double.toString(duration));
            System.out.println();
        } while(run_all);

        if(run_tests){
            System.out.println("FAILURES: "+ Integer.toString(failed_counter));
        }
    }
}