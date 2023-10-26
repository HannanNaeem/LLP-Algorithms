# LLP-Algorithms
Implementation of a few well known algorithms using lattice-linear predicates

## Directory Structure
The root directory has the main algorithm classes (which can be invoked.. described later).
- `inputs` sub-directory contain separate files for default inputs to each Algorithm class along with the package/class used to generate them
- `outputs` sub-directory contains the stored output and times for a run (when `-o` option is used)
- `tests` sub-directory contains separate files with inputs and expected outputs for each Algorithm

## Bellman Ford and Optimal BST -- @HannanNaeem
### Running the algorithms (Skip to [tests](#tests) for TLDR)
#### Inputs
The algorithms can be complied and run on the default inputs (`inputs/BellManFordInputs.txt` and `inputs/OBSTInputs.txt` respectively). Using BellManFord as an example:
```
> javac BellManFord.java
> java BellManFord
```
Each input line/case within the file is expected to start with `"Input"`. We can change the input file by providing an additional command line argument:
```
> javac BellManFord.java
> java BellManFord ./inputs/<AnotherInput>.txt
```
Similarly we have the option to run a single input only, rather than all the cases in a file. Note that cases are numbered from `0`
```
> java BellManFord ./inputs/BellManFordInputs.txt -s 3
```
All of the above -- `<path_to_file>`, `-s` and `<case_number>` -- must be given explicitly and in the same order to run a single case.\
Only lines with `Input` prefix are considered and everything else is ignored. Hence we can run the above commands with `./tests/BellManFordTests.txt` too.

#### Outputs
The output to the console will look like this:
```
File set to ./tests/OBSTTestCasesTeam1.txt    // if the file is changed from default
run all  false                                // for single-case run option only
input number  2                               // for single-case run option only
Running: 2
RESULT:
20 82 104 257 367 382 450 450 679 751 998 1265 1301 1578 1918 2179 
Time: 47.246066
```
For a single-case run we will see additional information as noted above.\
Additionally, `-o` flag can be appended to save the output to `./outputs/` directory. The outputs for the run will be stored against `Output(<case_number>):` followed by the time taken to run the case inthe next line. For example:
```
> java BellManFord -o
```
yields `BellManFordOutput.txt`. The name of the output file cannot be changed and is the name of the algorithm that was run. A snippet from output:
```
Output(0): {0,7,44,35,65,96,32,97,37,101}
Time: 3.72642 ms
Output(1): {0,72,119,103,109,50,87,118,65,89}
Time: 2.372141 ms
...
```
#### Tests
For convenience of comparison, the generated inputs and their expected outputs are stored together in the `./tests` directory. Each expected output must start with `Expected` header. It is also required that the first `Input` will correspond with the `Expected` header. Alternation is not required.
To run these tests: 
- a `-t` is expected as the FINAL command line argument.
- Or `-t <test_file_path>` is expected.
Running the following tests this implementation against Team1's test cases:
```
java OptimalBST -t ./tests/OBSTTestCasesTeam1.txt

```
or simply:
```
> java OptimalBST -t
```
The output (console only) will now look like this:
```
...

Running: 47
TEST: OK                // <------ Indicates we are testing against expected outputs
RESULT:
64 80 192 332 508 762 942 1131 1319 1379 1576 
Time: 12.919116

...

FAILURES: 0
```
Here are some other combination of commands that might be useful:
```
> java BellManFord -o -t                                          // testing default suite and storing output
> java BellManFord ./tests/BellManFordTests.txt -s 0 -t           // testing a single case from default testing suite
> java BellManFord ./inputs/BellManFordInputs.txt -s 3 -t ./tests/<SomeTestFile>.txt   // The first arg is ignored, testing a single case from provided test file
```


### BellManFord
Expects a adjacency matrix with no negative cycles, refer to inputs for format. Output is a single array with shortest distances of the first vertex to all others
### Optimal BST
Expects a single frequency array. Output is the first row of the cost table against each node

