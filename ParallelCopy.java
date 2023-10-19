class CopyThread extends Thread{
    int tid;
    int[] array;
    int start;
    int end; // start and end are inclusive
    int size;
    int[] copy;
    int chunk_size = 10; // how many elements to copy per thread e.g 10

    public CopyThread(int tid, int[] array, int start, int end, int[] copy, int chunk_size){
        this.tid = tid;
        this.array = array;
        size = array.length;
        this.start = start;            
        this.end = end;
        if (chunk_size > 0){
            this.chunk_size = chunk_size;
        }
        this.copy = copy;

    }

    public void run(){

        int t_start = (tid * chunk_size) + start;
        if (t_start < start){
            t_start = start;
        }
        int t_end = t_start + chunk_size;
        // System.out.println(t_start + " " + t_end);

        if (t_end > end){
            t_end = end;
        } 

        // System.out.println(t_start + " " + t_end);
        for(int i = t_start; i <= t_end; i++){
            copy[i - start] = array[i];
        }        
    }
}

public class ParallelCopy {

    public static int[] parallel_copy(int[] array, int start, int end, int chunk_size){
        int size = array.length;
        if (start > end || start < 0 || start > size){
            start = 0;
        }
        if (end < start || end < 0 || end > size){
            end = size -1;
        }
        int[] copy = new int[end - start + 1];
        int total_threads = (int) Math.ceil((end - start + 1)/(chunk_size * 1.0));

        CopyThread[] threads = new CopyThread[total_threads];

        // start threads
        for(int i = 0; i < total_threads; i++){
        // System.out.println(start + " " + end);
            threads[i] = new CopyThread(i, array, start, end, copy, chunk_size);
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
        int[] copy = ParallelCopy.parallel_copy(array, 10, 15, 10);
        System.out.println("Copy array:");
        for (int i : copy)
            System.out.print(i + " ");
        System.out.println();
    }

    
}
