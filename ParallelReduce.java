import java.lang.Math;
import java.util.concurrent.CyclicBarrier;



public class ParallelReduce extends Thread{
    int tid;
    int[] array;
    int n;
    CyclicBarrier barrier;

    public ParallelReduce(int tid, int[] array, CyclicBarrier barrier){
        this.tid = tid;
        this.array = array;
        n = array.length;
        this.barrier = barrier;
    }

    public static int binlog( int bits ) // returns 0 for bits=0 https://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }

    public void run(){
        for (int stride = 1; stride <= binlog(n); stride++){

            // read phase
            int val1 = array[2* tid];
            int val2 = array[2*tid + 1];

            try {
            
                barrier.await();

            } catch (Exception e) {

                e.printStackTrace();
                break;
            }

            // Write phase

            if (tid < (int) n / Math.pow(2, stride)){
                array[tid] = val1 + val2;

            }

            try {
            
                barrier.await();

            } catch (Exception e) {

                e.printStackTrace();
                break;
            }

        }
    }


    public static void main(String[] args){

        int[] array = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        int size = array.length;

        int end = size /2;
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