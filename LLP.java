import java.io.File;

abstract class LLP extends Thread{
    // any member variables
    
    // Method for checking if the current state of the thread is forbidden
    // Should return to the main run() of the thread
    protected abstract boolean check_forbidden();

    // If the state is forbidden we provide the logic to advance in the thread
    protected abstract void advance();

    // In cases where we wait for the termination condition
    protected abstract boolean exists_forbidden();

    public static boolean run_test_case(File test_file, int[] result, int number){
        int[] expected = ParseInput.parse_1D(test_file, number, "Expected");
        // compare
        if(expected.length != result.length){
            System.out.println("[CRITICAL] TEST " + Integer.toString(number) + " Failed [LENGTH MISMATCH]");
            return false;
        }
        for(int i = 0; i < expected.length; i ++){
            if(expected[i] != result[i]){
                System.out.println("[CRITICAL] TEST " + Integer.toString(number) + " Failed [VALUE MISMATCH]");
                System.out.println("\tExpected: " + Integer.toString(expected[i]) + " vs Got"+ Integer.toString(result[i]));
                return false;
            }
        }
        return true;
    }
}
