abstract class LLP extends Thread{
    // any member variables
    
    // Method for checking if the current state of the thread is forbidden
    // Should return to the main run() of the thread
    protected abstract boolean check_forbidden();

    // If the state is forbidden we provide the logic to advance in the thread
    protected abstract void advance();

    // In cases where we wait for the termination condition
    protected abstract boolean exists_forbidden();
}
