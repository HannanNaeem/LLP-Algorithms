import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;


class ParallelUpScanThread extends Thread{
    int tid;
    int[] origin; // original array A
    int[] can; // candidate array G
    boolean[] isEnsured;
    int n;
    CyclicBarrier barrier;

    public ParallelUpScanThread(int tid, int[] origin,
            int[] candidate, boolean[] isForbidden, CyclicBarrier barrier){
        this.tid = tid;
        this.origin = origin;
        this.can = candidate;
        this.isEnsured = isForbidden;
        n = origin.length;
        this.barrier = barrier;
    }

    boolean check_forbidden(){
        if (tid < n / 2 - 1) {
                if (can[2*(tid+1)-1] > Integer.MIN_VALUE && 
                        can[2*(tid+1)] > Integer.MIN_VALUE) {
                    if (can[tid] < can[2*(tid+1)-1] + can[2*(tid+1)]) {
                        return false;
                    }
                }
            } else {
                if (can[tid] < 
                        origin[2*(tid+1)-n] + origin[2*(tid+1)-n+1]) {
                    return false;
                }
            }
        return true;
    }

    int ensure(){
        // if (isEnsured[tid]) {return can[tid];}
        if (tid < n / 2 - 1) {
            if (can[2*(tid+1)-1] > Integer.MIN_VALUE && 
                    can[2*(tid+1)] > Integer.MIN_VALUE) {
                return (can[2*(tid+1)-1] + can[2*(tid+1)]);
            }
        } else {
            return (origin[2*(tid+1)-n] + origin[2*(tid+1)-n+1]);
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

public class ParallelUpScan{

    public static int[] parallelUpScan(int[] origin) {

        int size = origin.length - 1;
        int[] array = IntStream
            .generate(() -> Integer.MIN_VALUE)
            .limit(size)
            .toArray();
        boolean[] array_check = new boolean[size];

        ParallelUpScanThread[] threads = new ParallelUpScanThread[size];
        CyclicBarrier barrier = new CyclicBarrier(size);

        boolean indicator = false;
        while(!indicator) {
            for (int i = 0; i < size; i++){
                threads[i] = new ParallelUpScanThread(i, origin, array, array_check, barrier);
                threads[i].start();
            }
            indicator = true;
            for (int i = 0; i < size; i++){
                if (!array_check[i]) {
                    indicator = false;
                } 
            }
        }

        for (ParallelUpScanThread thread: threads){
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
        int[] result = parallelUpScan(origin);
        
        System.out.println("RESULT:");
        for (int i : result)
            System.out.print(i + " ");
        System.out.println();

    }
    
}