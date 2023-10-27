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

        return result;
    }

    public static String[] parse_args(String[] args, String algo){
        boolean run_all = true; // single test or all?
        String input_file = "./inputs/"+algo+"Inputs.txt";
        int input_number = 0;
        boolean write_out = false;
        boolean run_tests = false;
        String out_file = "./outputs/"+algo+"Output.txt";
        String test_file = "./tests/"+algo+"Tests.txt";
        // PARSING ARGS START -----------------------------
        for(int i = 0; i < args.length; i++){
            if (i == 0 && (args[i].compareTo("-o")) != 0 && (args[i].compareTo("-t")) != 0){
                // Expect file path
                input_file = args[i];
                System.out.println("File set to " + args[i]);

            }

            if (i == 1){
                // Expect single/s or all
                if(args[i].startsWith("-s")){
                    run_all = false;
                    System.out.println("run all  " + run_all);

                }
            }

            if (!run_all && i == 2){
                // Followed by single expect input/test number
                try{
                    input_number = Integer.parseInt(args[i]);
                    System.out.println("input number  " + args[i]);

                    if (input_number < 0){
                        throw new Exception("Test/Input number cannot be less than 0");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
            if(args[i].startsWith("-o")){
                write_out = true;
            }

            if (args[i].startsWith("-t")){
                run_tests = true;
                if(i + 1 < args.length){
                    test_file = args[i + 1];
                    i++;
                }
            }
        }
        String[] parsed = {Boolean.toString(run_all), input_file, Integer.toString(input_number), Boolean.toString(write_out), Boolean.toString(run_tests), out_file, test_file};
        return parsed;
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
        int[][] result4 = parse_2D(in_test4, 2, "Input");
        System.out.println("PARSE TEST BELLMAN TEAM 1: ");
        for(int i = 0; i < result4.length; i ++){
            for(int j = 0; j < result4.length; j++){
                System.out.print(result4[i][j] + " ");
            }
            System.out.println();
        }

        File in_test5 = new File("./inputs/OBSTTestCasesTeam1.txt");
        int[] result5 = parse_1D(in_test5, 1, "Expected");
        System.out.println("PARSE TEST EXPECTED OBST TEAM 1: ");
        for(int i : result5){
            System.out.print(i + " ");
        }
        System.out.println();

    }
}
