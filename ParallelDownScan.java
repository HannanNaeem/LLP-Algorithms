import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;


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
        boolean run_all = true; // single test or all?
        File input_file = new File("./inputs/ScanInput.txt");
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
            int[] freq = ParseInput.parse_1D(input_file, input_number, "Input");
            input_number++;
            if (freq == null){
                break;
            }
            System.out.println("Running: " + (input_number-1));
            int[] up_tree = ParallelUpScan.parallelUpScan(freq);
            int[] result = parallelDownScan(freq, up_tree);

            System.out.println("Final State:");
            for(int j = 0; j < result.length; j++){
                System.out.print(result[j] + " ");
            }
            System.out.println();
            System.out.println();
        } while(run_all);

    }
    
}