import java.lang.Math;
import java.util.concurrent.CyclicBarrier;



class ParallelReduceThread extends Thread{
    int tid;
    int[] array;
    int n;
    CyclicBarrier barrier;
    int total_strides;

    public ParallelReduceThread(int tid, int[] array, CyclicBarrier barrier){
        this.tid = tid;
        this.array = array;
        n = array.length;
        this.barrier = barrier;
        total_strides = (int) Math.ceil(Math.log(n) / Math.log(2));
    }

    public void run(){
        for (int stride = 1; stride <= total_strides; stride++){

            // read phase
            int val1 = array[2* tid];
            int val2 = 0;
            if (2*tid + 1 < (int) Math.ceil(n / Math.pow(2.0, stride - 1))){
                val2 = array[2*tid + 1];
            }
            
            // Synchronize threads
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            // Write phase
            if (tid < (int) Math.ceil(n / Math.pow(2.0, stride))){
                array[tid] = val1 + val2;
            }

            // Wait for all writes
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

        }
    }

}

public class ParallelReduce {

    public static int parallel_reduce(int[] array){
        
        int size = array.length;
        int end = (int) Math.ceil(size /2.0);
        ParallelReduceThread[] threads = new ParallelReduceThread[end];
        CyclicBarrier barrier = new CyclicBarrier(end);

        for (int i = 0; i < end; i++){
            threads[i] = new ParallelReduceThread(i, array, barrier);
            threads[i].start();
        }

        for (ParallelReduceThread thread: threads){
            try{
                thread.join();
            } catch (Exception e){
                System.out.println("Interrupted");
            }
        }
        
        return array[0];
    }


    public static void main(String[] args){

        int[] array = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
        ParallelReduce.parallel_reduce(array);
        System.out.println("RESULT: "+ array[0]);
        System.out.println("\nFinal state:");
        for (int i : array)
            System.out.print(i + " ");
        System.out.println();
    }
}