import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;
import inputs.*;

class ParallelDownScanThread extends Thread{
    int tid;
    int[] origin; // original array A
    int[] sumtree; // the summation tree S
    int[] can; // candidate array G
    boolean[] isEnsured;
    int n;
    CyclicBarrier barrier;

    public ParallelDownScanThread(int tid, int[] origin, int[] sumtree,
            int[] candidate, boolean[] isForbidden, CyclicBarrier barrier){
        this.tid = tid;
        this.origin = origin;
        this.sumtree = sumtree;
        this.can = candidate;
        this.isEnsured = isForbidden;
        n = origin.length;
        this.barrier = barrier;
    }

    boolean check_forbidden(){

        // ensure condition for j = 1
        if (tid == 0) {
            if (can[tid] < 0){
                return false;
            }
        }

        // ensure condition for j is even
        if ((tid + 1) % 2 == 0){
            if ((can[(tid+1)/2 -1] > Integer.MIN_VALUE) 
                    && (can[tid] < can[(tid+1)/2 -1])){
                return false;
            }
        }

        // ensure condition for j is odd and j < n
        if (((tid+1) % 2 == 1) && (tid >= 1) && (tid < n-1)) {
            if (can[(tid+1)/2 -1] > Integer.MIN_VALUE) {
                if (can[tid] < sumtree[tid-1] + can[(tid)/2 -1]) {
                    return false;
                }
            }
        }

        // ensure condition for j is odd and j > n
        if (((tid+1) % 2 == 1) && (tid >= 1) && (tid > n-1)) {
            if (can[(tid+1)/2 -1] > Integer.MIN_VALUE) {
                if (can[tid] < origin[tid-n] + can[(tid)/2 -1]) {
                    return false;
                }
            }
        }

        return true;
    }

    int ensure(){

        // ensure condition for j = 1
        if (tid == 0) {
            return 0;
        }

        // ensure condition for j is even
        if ((tid + 1) % 2 == 0){
            return can[(tid+1)/2 -1];
        }

        // ensure condition for j is odd and j < n
        if (((tid+1) % 2 == 1) && (tid >= 1) && (tid < n-1)) {
            return sumtree[tid-1] + can[(tid)/2 -1];
        }

        // ensure condition for j is odd and j > n
        if (((tid+1) % 2 == 1) && (tid >= 1) && (tid > n-1)) {
            return origin[tid-n] + can[(tid)/2 -1];
        }

        return can[tid];
    }

    public void run(){

        isEnsured[tid] = check_forbidden();
        int temp = ensure();

        // wait for
        try {
            barrier.await();
        } catch (Exception e) {
            // e.printStackTrace();
        }

        if (!isEnsured[tid]){
            can[tid] = temp;
        }
        
        // Wait for all writes
        try {
            barrier.await();
        } catch (Exception e) {
            // e.printStackTrace();
        }                
    }
    
}

public class ParallelDownScan{

    public static int[] parallelDownScan(int[] origin_array, int[] sumtree) {

        int length_ext = (1 << (32 - Integer.numberOfLeadingZeros(origin_array.length - 1)));
        int size = 2 * length_ext - 1;
        int[] array = IntStream
            .generate(() -> Integer.MIN_VALUE)
            .limit(size)
            .toArray();
        int[] origin_extended = IntStream
            .generate(() -> 0)
            .limit(length_ext)
            .toArray();
        boolean[] array_check = new boolean[size];

        ParallelDownScanThread[] threads = new ParallelDownScanThread[size];
        CyclicBarrier barrier = new CyclicBarrier(size);

        for (int i = 0; i < origin_array.length; i++){
            origin_extended[i] = origin_array[i];
        }

        boolean indicator = false;
        while(!indicator) {
            for (int i = 0; i < size; i++){
                threads[i] = new ParallelDownScanThread(i, origin_extended, sumtree, array, array_check, barrier);
                threads[i].start();
            }
            indicator = true;
            for (int i = 0; i < size; i++){
                if (!array_check[i]) {
                    indicator = false;
                    break;
                } 
            }
        }

        for (ParallelDownScanThread thread: threads){
            try{
                thread.interrupt();
                thread.join();
            } catch (Exception e){
                System.out.println("Interreupted");
            }
        }

        // Compulte the final sum for each node
        int[] array_result = Arrays.copyOfRange(
            array, length_ext-1, length_ext + origin_array.length - 1);

        for (int i = 0; i < origin_array.length; i++) {
            array_result[i] += origin_array[i];
        }

        return array_result;
    }

    public static void main(String[] args){
        // PARSING ARGS START -----------------------------
        String[] parsed_args = ParseInput.parse_args(args, "scan");
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
            int[] up_tree = ParallelUpScan.parallelUpScan(freq);
            int[] result = parallelDownScan(freq, up_tree);
            long endTime = System.nanoTime();
            double duration = (endTime - startTime)/1000000.0;  //divide by 1000000 to get milliseconds.
            
            // Write output and time to file
            if (write_out){                
                try{
                    FileWriter writer = new FileWriter(out_file, true);
                    GenerateOBST.write_array1D(result, result.length, writer, "Output(" + Integer.toString(input_number-1) + ")");
                    writer.write("Time: " + Double.toString(duration) + " ms\n");
                    writer.close();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            // Are we running test suite? Get expected output and compare
            
            if (run_tests ){
                boolean passed = LLP.run_test_case(test_file, result, input_number-1);
                if(passed){
                    System.out.println("TEST: OK");
                } else {
                    failed_counter++;
                }
            }
            System.out.println("RESULT:");
            for (int i : result)
                System.out.print(i + " ");
            System.out.println("\nTime: " + Double.toString(duration));
            System.out.println();
        } while(run_all);

        if(run_tests){
            System.out.println("FAILURES: "+ Integer.toString(failed_counter));
        }
    }
    
}