class CopyThread extends Thread{
    int tid;
    int[] array;
    int size;
    int[] copy;
    int chunk_size = 10; // how many elements to copy per thread e.g 10

    public CopyThread(int tid, int[] array, int[] copy, int chunk_size){
        this.tid = tid;
        this.array = array;
        size = array.length;
        if (chunk_size > 0){
            this.chunk_size = chunk_size;
        }
        this.copy = copy;
    }

    public void run(){
        int end = (tid * chunk_size) + chunk_size;
        if (end > size){
            end = size;
        }

        for(int i = tid * chunk_size; i < end; i++){
            copy[i] = array[i];
        }        
    }
}

public class ParallelCopy {

    public static int[] parallel_copy(int[] array, int chunk_size){
        int size = array.length;
        int[] copy = new int[array.length];
        int total_threads = (int) Math.ceil(size/(chunk_size * 1.0));

        CopyThread[] threads = new CopyThread[total_threads];

        // start threads
        for(int i = 0; i < total_threads; i++){
            threads[i] = new CopyThread(i, array, copy, chunk_size);
            threads[i].start();
        }

        // wait for all threads to finish
        for (CopyThread thread: threads){
            try{
                thread.join();
            } catch (Exception e){
                System.out.println("Interrupted");
            }
        }

        return copy;
    }

    public static void main(String[] args){
        int[] array = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
        int[] copy = ParallelCopy.parallel_copy(array, 10);
        System.out.println("Copy array:");
        for (int i : copy)
            System.out.print(i + " ");
        System.out.println();
    }

    
}
