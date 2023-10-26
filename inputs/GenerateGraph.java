import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateGraph{
    static File file_out = new File("graphInputs.txt");
    static Random rand = new Random();

    private static void populate(int[][] array, int size){
        for(int i = 0; i < size; i ++){
            for(int j = i; j < size; j++) {
                if (i == j){
                    array[i][i] = -1;
                    continue;
                }
                array[i][j] = (rand.nextInt(200) + 1) % 201;
                array[j][i] = array[i][j];
            }
        }
    }

    private static void write_array(int[][] array, int size, FileWriter writer) throws IOException{
        writer.write("Input: {");
        for(int i = 0; i < size; i ++){
                writer.write("{");
                for (int j = 0; j < size; j++){

                    writer.write(Integer.toString(array[i][j]));
                    
                    if (j != size -1){
                        writer.write(",");
                    }
                }
                writer.write("}");
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
        int[][] input;

        try {
            FileWriter writer = new FileWriter(file_out, true); 


            for(int size = start_size; size <= end_size; size = size * 10){
                for(int i = 0; i < each_size_test; i++){
                    // declare a new array
                    input = new int[size][size];
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