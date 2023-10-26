import java.io.File;
import java.util.Scanner;

public class ParseInput {
    
    public static int[][] parse_2D(File in_file, int in_number, String in_header){
        int[][] result = null;
            try {
                Scanner reader = new Scanner(in_file);
                int curr_input_num = 0;
                while (reader.hasNextLine()){

                    String data = reader.nextLine();
                    if (data.startsWith(in_header, 0)){
                        if (curr_input_num != in_number){
                            curr_input_num++;
                            continue;
                        }
                        curr_input_num++;
                        data = data.split(":")[1];
                        data = data.trim();
                        data = data.replace("{", "");
                        data = data.replace("}", "");
                    } else {
                        continue;
                    }

                    String[] values = data.split(",");
                    int size = (int) Math.sqrt(values.length);
                    result = new int[size][size];
                    for(int i = 0; i < size; i++){
                        for(int j = 0; j < size; j++){
                            if (i == j){
                                result[i][i] = 0;
                                continue;
                            }
                            result[i][j] = Integer.parseInt(values[size * i + j].trim());
                        }
                    }
                }
                reader.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        if (result == null){
            System.out.println("[ERROR] FAILED TO FIND INPUT");
        }
        return result;
    }

    public static int[] parse_1D(File in_file, int in_number, String in_header){
        int[] result = null;
            try {
                Scanner reader = new Scanner(in_file);
                int curr_input_num = 0;
                while (reader.hasNextLine()){
                    
                    String data = reader.nextLine();

                    if (data.startsWith(in_header, 0)){
                        if (curr_input_num != in_number){
                            curr_input_num++;
                            continue;
                        }
                        curr_input_num++;
                        data = data.split(":")[1];
                        data = data.trim();
                        data = data.replace("{", "");
                        data = data.replace("}", "");
                        System.out.println(data);
                    } else {
                        continue;
                    }

                    String[] values = data.split(",");
                    int size = values.length;
                    result = new int[size];
                    for(int i = 0; i < size; i++){
                        result[i] = Integer.parseInt(values[i].trim());
                    }
                }
                reader.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        if (result == null){
            System.out.println("[ERROR] FAILED TO FIND INPUT");
        }
        return result;
    }

    public static void main(String[] args){
        File in_test = new File("./inputs/OBSTInputs.txt");
        int[] result = parse_1D(in_test, 1, "Input");
        System.out.println("PARSE TEST OBST: ");
        for(int i : result){
            System.out.print(i + " ");
        }
        System.out.println();

        File in_test2 = new File("./inputs/BellManInputs.txt");
        int[][] result2 = parse_2D(in_test2, 1, "Input");
        System.out.println("PARSE TEST BELLMAN: ");
        for(int i = 0; i < result2.length; i ++){
            for(int j = 0; j < result2.length; j++){
                System.out.print(result2[i][j] + " ");
            }
            System.out.println();
        }

         // TESTING TEAM 1 CASES
        File in_test3 = new File("./inputs/OBSTTestCasesTeam1.txt");
        int[] result3 = parse_1D(in_test3, 1, "Input");
        System.out.println("PARSE TEST OBST TEAM 1: ");
        for(int i : result3){
            System.out.print(i + " ");
        }
        System.out.println();

        File in_test4 = new File("./inputs/BellManTestCasesTeam1.txt");
        int[][] result4 = parse_2D(in_test4, 1, "Input");
        System.out.println("PARSE TEST BELLMAN TEAM 1: ");
        for(int i = 0; i < result4.length; i ++){
            for(int j = 0; j < result4.length; j++){
                System.out.print(result4[i][j] + " ");
            }
            System.out.println();
        }

    }
}
