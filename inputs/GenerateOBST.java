import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateOBST{
    static File file_out = new File("OBSTInputs.txt");
    static Random rand = new Random();

    private static void populate(int[] array, int size){
        for(int i = 0; i < size; i ++){
            array[i] = (rand.nextInt(5000) + 1) % 101;
        }
    }

    private static void write_array(int [] array, int size, FileWriter writer) throws IOException{
            writer.write("Input: {");
            for(int i = 0; i < size; i ++){
                writer.write(Integer.toString(array[i]));
                if (i != size -1){
                    writer.write(",");
                }
            }
            writer.write("}" + "\n");
    }

    public static void main(String[] args){
        // Expect start and end sizes and test for each size if not use default values
        int start_size = 10;
        int end_size = 100;
        int each_size_test = 5;
        int[] input;

        try {
            FileWriter writer = new FileWriter(file_out, true); 


            for(int size = start_size; size <= end_size; size = size * 10){
                for(int i = 0; i < each_size_test; i++){
                    // declare a new array
                    input = new int[size];
                    // populate it
                    populate(input, size);
                    // write to file
                    write_array(input, size, writer);
                }
            }

            writer.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
