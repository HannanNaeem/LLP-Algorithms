import java.util.HashSet;
import java.util.concurrent.CyclicBarrier;

class BMInitThread extends Thread{
    int tid;
    int chunk_size;
    int[] sol_array;
    boolean[] forbidden_array;

    public BMInitThread(int tid, int[] sol_array, boolean[] forbidden_array, int chunk_size){
        this.tid = tid;
        this.sol_array = sol_array;
        this.forbidden_array = forbidden_array;
        this.chunk_size = 10;
        if (chunk_size > 0){
            this.chunk_size = chunk_size;
        }
    }

    public void run(){
        int start = tid * chunk_size;
        int end = start + chunk_size;
        if (end > sol_array.length){
            end = sol_array.length;
        }

        for (int i = start; i < end; i ++){
            sol_array[i] = Integer.MAX_VALUE;
            forbidden_array[i] = true;
        }

        if (tid == 0){
            sol_array[0] = 0;
            forbidden_array[0] = false;
        }
    }
}

class BellManThread extends LLP{

    int tid;
    int[] array;
    int n;
    int[][] graph;
    int v;
    boolean[] isForbidden;
    CyclicBarrier barrier;
    HashSet<Integer> preds;

    public BellManThread(int tid, boolean[] isForbidden, int[] array, int[][] graph, CyclicBarrier barrier){
        this.tid = tid;
        this.array = array;
        n = array.length;
        this.graph = graph;
        v = graph.length;
        this.isForbidden = isForbidden;
        preds = get_preds();
        this.barrier = barrier;
    }

    private HashSet<Integer> get_preds(){

        // scan columns to get predecessors
        HashSet<Integer> result = new HashSet<Integer>();
        for(int i = 0; i < v; i++){
            if (graph[i][tid] != 0){
                result.add(i);
            }
        }
        return result;
    }

    protected boolean check_forbidden(){
        // for all predeessors of j
        for(int i : preds){
            if (array[i] == Integer.MAX_VALUE){
                return true;
            }
            if (array[tid] > array[i] + graph[i][tid]){
                return true;
            }
        }

        return false;
    }

    protected void advance(){
        int min = Integer.MAX_VALUE;
        for(int i: preds){
            int val = array[i] + graph[i][tid];
            if(array[i] == Integer.MAX_VALUE){
                val = Integer.MAX_VALUE;
            }
            min = Math.min(min, val);
        }
        array[tid] = min;
    }

    protected boolean exists_forbidden(){

        for(boolean b : isForbidden){
            if(b){
                return true;
            }
        }
        return false;
    }

    public void run(){

        isForbidden[tid] = check_forbidden();

        while(exists_forbidden()){

            isForbidden[tid] = check_forbidden();

            try{
                barrier.await();
            } catch (Exception e){
                e.printStackTrace();
                break;
            }

            if (isForbidden[tid]){
                advance();
            }

        }
        
    }

}

public class BellManFord {

    public static int[] bellman_ford(int[][] graph){

        int[] sol_array = new int[graph.length];
        boolean[] forbidden_array = new boolean[graph.length];
        int chunk_size = 10;
        int total_threads = (int) Math.ceil((graph.length)/(chunk_size * 1.0));
        BMInitThread init_threads[] = new BMInitThread[total_threads];
        
        //* PARALLEL INIT */
        for(int i = 0; i < total_threads; i++){
            init_threads[i] = new BMInitThread(i, sol_array, forbidden_array, chunk_size);
            init_threads[i].start();
        }

        BellManThread[] threads = new BellManThread[sol_array.length];
        CyclicBarrier barrier = new CyclicBarrier(sol_array.length - 1, null);

        for (int i = 1; i < sol_array.length; i++){
            threads[i] = new BellManThread(i, forbidden_array, sol_array, graph, barrier);
            threads[i].start();
        }

        for (int i = 1; i < sol_array.length; i++){
            try{
                threads[i].join();
            } catch (Exception e){
                System.out.println("Interreupted");
            }
        }

        return sol_array;

    }

    public static void main(String[] args){
        int[][] graph = {{0, 73, 25, 85, 63}, {73, 0, 55, 6, 79}, {25, 55, 0, 61, 82}, {85, 6, 61, 0, 55}, {63, 79, 82, 55, 0}};
        int[] sol_array = BellManFord.bellman_ford(graph);
        System.out.println("RESULT:");
        for (int i : sol_array)
            System.out.print(i + " ");
        System.out.println();
    }
}