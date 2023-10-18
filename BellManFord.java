import java.util.HashSet;

public class BellManFord extends Thread{

    int tid;
    int[] array;
    int n;
    int[][] graph;
    int v;
    boolean[] isForbidden;
    HashSet<Integer> preds;

    public BellManFord(int tid, boolean[] isForbidden, int[] array, int[][] graph){
        this.tid = tid;
        this.array = array;
        n = array.length;
        this.graph = graph;
        v = graph.length;
        this.isForbidden = isForbidden;
        preds = get_preds();
    }

    public HashSet<Integer> get_preds(){

        // scan columns to get predecessors
        HashSet<Integer> result = new HashSet<Integer>();
        for(int i = 0; i < v; i++){
            if (graph[i][tid] != 0){
                result.add(i);
            }
        }
        return result;
    }

    public boolean check_forbidden(){
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

    public void advance(){
        int min = Integer.MAX_VALUE;
        for(int i: preds){
            min = Math.min(min, array[i] + graph[i][tid]);
        }
        array[tid] = min;
    }

    public boolean exists_forbidden(){

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

            if (isForbidden[tid]){
                advance();
            }
        }
        
        
    }




    public static void main(String[] args){

        int[][] graph = {{0,1,5,0,0},{0,0,1,0,1},{0,0,0,1,0},{0,0,0,0,1},{0,0,0,0,0}};
        int[] sol_array = {0,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
        boolean[] forbidden_array = {false, true, true, true, true};
        // while j is forbidden launch a thread to advance

        BellManFord[] threads = new BellManFord[sol_array.length];

        for (int i = 1; i < sol_array.length; i++){
            threads[i] = new BellManFord(i, forbidden_array, sol_array, graph);
            threads[i].start();
        }

        for (int i = 1; i < sol_array.length; i++){
            try{
                threads[i].join();
            } catch (Exception e){
                System.out.println("Interreupted");
            }
        }

        System.out.println("RESULT:");
        for (int i : sol_array)
            System.out.print(i + " ");
        System.out.println();

    }
    
}
