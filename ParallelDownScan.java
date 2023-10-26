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
            e.printStackTrace();
        }

        if (!isEnsured[tid]){
            can[tid] = temp;
        }
        
        // Wait for all writes
        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }                
    }
    
}

public class ParallelDownScan{

    public static int[] parallelDownScan(int[] origin, int[] sumtree) {

        int size = 2 * origin.length - 1;
        int[] array = IntStream
            .generate(() -> Integer.MIN_VALUE)
            .limit(size)
            .toArray();
        boolean[] array_check = new boolean[size];

        ParallelDownScanThread[] threads = new ParallelDownScanThread[size];
        CyclicBarrier barrier = new CyclicBarrier(size);

        boolean indicator = false;
        while(!indicator) {
            for (int i = 0; i < size; i++){
                threads[i] = new ParallelDownScanThread(i, origin, sumtree, array, array_check, barrier);
                threads[i].start();
            }
            indicator = true;
            for (int i = 0; i < size; i++){
                if (!array_check[i]) {
                    indicator = false;
                } 
            }
        }

        for (ParallelDownScanThread thread: threads){
            try{
                thread.join();
            } catch (Exception e){
                System.out.println("Interreupted");
            }
        }

        return array;
    }

    public static void main(String[] args){

        int[] origin = {1,2,3,4,5,6,7,8};
        int[] up_tree = ParallelUpScan.parallelUpScan(origin);
        int[] result = parallelDownScan(origin, up_tree);
        
        System.out.println("RESULT:");
        for (int i : result)
            System.out.print(i + " ");
        System.out.println();

    }
    
}