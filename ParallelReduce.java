import java.lang.Math;
import java.util.concurrent.CyclicBarrier;



public class ParallelReduce extends Thread{
    int tid;
    int[] array;
    int n;
    CyclicBarrier barrier;
    int total_strides;

    public ParallelReduce(int tid, int[] array, CyclicBarrier barrier){
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


    public static void main(String[] args){

        int[] array = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18};
        int size = array.length;
        int end = (int) Math.ceil(size /2.0);
        ParallelReduce[] threads = new ParallelReduce[end];
        CyclicBarrier barrier = new CyclicBarrier(end);

        for (int i = 0; i < end; i++){
            threads[i] = new ParallelReduce(i, array, barrier);
            threads[i].start();
        }

        for (ParallelReduce thread: threads){
            try{
                thread.join();
            } catch (Exception e){
                System.out.println("Interreupted");
            }
        }
        
        System.out.println("RESULT:");
        for (int i : array)
            System.out.print(i + " ");
        System.out.println();

    }
}