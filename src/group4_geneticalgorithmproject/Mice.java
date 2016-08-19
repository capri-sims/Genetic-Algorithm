/******************************************************
***  Class Name: Mice
***  Class Author: Group 4
******************************************************
*** Purpose: To solve a given maze using a genetic 
*       algorithm.
******************************************************
*** Date: July 10, 2015
******************************************************
*** TO DO: 
*       Lower Mutation Rate -check
*       Change Fitness Scoring
*       (discourage from moving backwards)
*   
*       Getting stuck on a solution way too early
* 
*   PREMATURE CONVERGANCE
*   TO COMBAT: 
*       increased population size to 400
*       varied breeding crossover points and added uniform crossover
*       increased mutation rate
*       changed fitness scoring
* 
*       create virus?
*       change selection
* 
*       wrapped algorithm in while loop (until end is found)
*   
*******************************************************/
package group4_geneticalgorithmproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Mice {
    
    private int size = 400, iterations = 200; //http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.105.2431&rep=rep1&type=pdf
    private List<String> population = new ArrayList<>();
    private List<String> best = new ArrayList<>();
    private List<String> parents = new ArrayList<>();
    private List<Integer> fitness = new ArrayList<>();
    private List<Integer> bestFitness = new ArrayList<>();
    private Random rng = new Random();
    private char[][] maze = new char[20][20];
    private int rowStart, colStart; //starting position
    private double mutateCount, totalCount;
    private int TOTAL_CHANCE = 100, LIKELY_CHANCE = 40;
    private boolean endReached = false;
    
    public Mice(char[][]  maze, int row, int col){ 
        this.maze = maze;
        rowStart = row;
        colStart = col;
        
        while(!endReached){
            population.clear();
            best.clear();
            bestFitness.clear();
            
            generate();

            for(int i = 0; i < iterations; i++){
                fitness();
                virus();
                selection();
                breed();
                mutate();
            }
        }

        //System.out.println("Mutation Rate: " + (mutateCount / totalCount));
    }
    
    //    ******************************************************
    //***  FITNESS
    //***  Jonathan Barker
    //******************************************************
    //*** To calculate the fitness of the overall program
    //******************changes*****************************
    //started by calulating based off only of the string
    // had to restart due to loss of data
    //chaged it to go based off of the array and the string
    // 7/15/2015 - changed the switch statements to if statements due to 
    // 7/15/2015 - getting lost in own switches
    //******************************************************
    //*** 7/15/2015 (Capri Sims)
    //***  Added fitness.clear()
    //***  Added  checks in the if statements to prevent 
    //***    going out of bounds
    //***  Added break statments in case end is reached
    //***  Added collection of best mice and fitness at end
    //*** 7/17/2015 (Capri Sims)
    //***   added fit -= mouse.length(); //removed it
    //***   lowered initial value of bestFit
    //***   placed position decrements inside of if statements
    //***   added visited array
    //***   changed fitness scoring to use visited
    //*** 7/18/2015 (Capri Sims)
    //***   removed fitness evaluation from within if statements
    //***    (to reduce redundancy and better understanding)
    //***   added additional scoring
    //***   changed scoring - again and again
    //******************************************************
    private void fitness()
    {
        int fit;        
        int row;
        int col;
        int bestFit = -999999;
        String bestMouse = null;
        int[][] visited;
        int v;
        int lastR, lastC;
        
        fitness.clear();
        
        for(String mouse: population)
        {
            fit = 0;
            row = rowStart; 
            col = colStart;
            visited  = new int[20][20];
            v = 0;

            for(char charRun: mouse.toCharArray())
            {
                visited[row][col] = ++v;
                int spaceCounter = 0;
                lastR = row;
                lastC = col;
                
                //if the player moves up
                if(charRun == 'w' && row != 0 && maze[row-1][col] != 'x'){
                    row--;
                }
                //if the player moves left
                else if(charRun == 'a' && col != 0 && maze[row][col-1] != 'x'){
                    col--; 
                }
                //if the player moves down
                else if(charRun == 's' && row != maze.length - 1 && maze[row+1][col] != 'x'){
                    row++;
                }
                //if the player moves right
                else if(charRun == 'd' && col != maze[0].length - 1 && maze[row][col+1] != 'x'){
                    col++;
                }
                
                if(col != maze[0].length - 1 && (maze[row][col+1] == 'o' || maze[row][col+1] == 'w')){
                    spaceCounter++;
                }
                if(row != maze.length - 1 && (maze[row+1][col] == 'o' || maze[row+1][col] == 'w')){
                    spaceCounter++;
                }
                if(col != 0 && (maze[row][col-1] == 'o' || maze[row][col-1] == 'w')){
                    spaceCounter++;
                }
                if(row != 0 && (maze[row-1][col] == 'o' || maze[row-1][col] == 'w')){
                    spaceCounter++;
                }

                if(spaceCounter == 1) //reached dead end
                    fit -= 30;
                else if(spaceCounter > 2){ 
                    if(visited[row][col] == 0) //reached new junction
                        fit += 100;
                    else                        //reached old junction
                        fit -= 30;
                }
                
                if(maze[row][col] == 'e'){ //reached the end
                    fit += 1000;
                    fit += mouse.length() - mouse.indexOf(charRun); 
                    endReached = true;
                }

                if(visited[row][col] > 0)
                    fit -= 1; 
                else if(visited[row][col] == 0) //needs heavy encouragment to explore
                    fit += 100;    
                
                if(lastR == row && lastC == col){
                    fit -= 10; 
                }
            }
            
            fitness.add(fit);
            if(fit > bestFit){ //finds best of each generation
                bestFit = fit;
                bestMouse = mouse;
            }
        }
        bestFitness.add(bestFit);
        best.add(bestMouse);
        //System.out.println(bestFit + " : " + bestMouse);
    }
    
    /*
    ******************************************************
    *** breed
    *** Zach Clayton
    ******************************************************
    *** Purpose of the Method
    *** Perform the breeding duties to create new 
    *** populations for future iterations
    *** Method Inputs:
    *** n/a
    *** Return value:
    *** n/a
    ******************************************************
    *** List of changes with dates. 
    *** 07/06/2015 – Creation date
    *** 07/06/2015 – Made & tested crossover @ one-point
    *** 07/08/2015 – Made & tested crossover @ two-point
    *** 07/14/2015 – Integrated into mice.java
    *** 07/14/2015 – Rewrote both methods of crossover to
    ***              utilize lists
    *** 07/14/2015 – Fixed looping to accomedate new loops
    *** 07/15/2015 - Cleared population at beginning (Capri Sims)
    *** 07/15/2015 - Changed random number generator to rng (Capri Sims)
    *** 07/15/2015 - Removed selection loop (Capri Sims)
    *** 07/15/2015 - Switched places of for loop and howToBreed if statement (CS)
    *** 07/15/2015 - Changed breedLoop max size to size*2 to account for size changes (CS)
    *** 07/18/2015 - Changed position of child and untilCO reset (CS)
    *** 07/18/2015 - Changed CO points to allow for random numbers (CS)
    *** 07/18/2015 - Added another breeding option (uniform crossover)
    *** 07/18/2015 - Changed range of howToBreed and utilizing if statements
    *** 07/18/2015 - Increased randomization of everything
    ******************************************************
    */
    private void breed ()
    {
        int howToBreed, breedLoop, point0CO = 0, point1CO = 0, point2CO = 0, point3CO = 0, point4CO = 0, runSelection, untilCO = 0;
        String child;
        population.clear();
       
        for (breedLoop = 0; breedLoop < size*2; breedLoop += 2)
        {
            // Randomly chooses to breed by either one or two points (or third option)
            howToBreed = rng.nextInt (5);
            child = ""; // Resetiing child & untilCO for future iterations
            untilCO = 0;

            if (howToBreed == 1)
            {
                // One-point crossover, 1/3 chance to be chosen
                
                // Defines cutoff points
                point0CO = rng.nextInt(parents.get(breedLoop).length() - 1) + 1;
                point1CO = point0CO;
                point2CO = parents.get (breedLoop + 1).length (); 
                
                // Crossover of first half
                while (untilCO < point0CO)
                {
                    child += parents.get (breedLoop).charAt (untilCO);
                    untilCO += 1;
                }
                // Crossover of second half
                untilCO = point1CO;
                while (untilCO < point2CO)
                {
                    child += parents.get (breedLoop + 1).charAt (untilCO);
                    untilCO += 1;
                }
                
                // Add crossover results stored in child to population list
                population.add (child);

            }
            else if(howToBreed == 2)
            {
                // Two-point crossover, 2/3 chance to be chosen
                int coin1 = rng.nextInt(2);
                int coin2 = 1;
                if(coin1 == 1){
                    coin2 = 0;
                }
                
                // Defines cutoff points
                point0CO = rng.nextInt(parents.get(breedLoop + coin1).length() / 2) + 1;
                point1CO = point0CO;
                point2CO = rng.nextInt((int)(parents.get(breedLoop + coin2).length() / 2.5) - 1) + point0CO + 1;
                point3CO = point2CO;
                point4CO = parents.get (breedLoop + coin1).length ();

                // Crossover of first part
                while (untilCO < point0CO)
                {
                    child += parents.get (breedLoop + coin1).charAt (untilCO);
                    untilCO += 1;
                }
                
                // Crossover of second part
                untilCO = point1CO;
                while (untilCO < point2CO)
                {
                    child += parents.get (breedLoop + coin2).charAt (untilCO);
                    untilCO += 1;
                }
                // Crossover of third part
                untilCO = point3CO;
                while (untilCO < point4CO)
                {
                    child += parents.get (breedLoop + coin1).charAt (untilCO);
                    untilCO += 1;
                }

                // Add crossover results stored in child to population list
                population.add (child);

            }
            else{ //Uniform Crossover (zipper)
                
                int coin1 = rng.nextInt(2);
                int coin2 = 1;
                if(coin1 == 1){
                    coin2 = 0;
                }

                String parent1 = parents.get(breedLoop + coin1);
                String parent2 = parents.get(breedLoop + coin2);

                boolean todo = true;
                int i = 0, j = 0;
                while(todo){
                    j = rng.nextInt(2); //significantly improves diversity

                    if(i < parent1.length() && j == 0){
                        child += parent1.charAt(i++);
                    }
                    if(i < parent2.length() && j == 1){
                        child += parent2.charAt(i++);
                    }
                    if(i >= parent1.length() && i >= parent2.length()){
                        todo = false;
                    }
                }
                population.add(child);
            }
        }
    }
    
    
    /**
    ****************************************************************************
    ***  Method Name: mutate
    ***  Method Author: Adrian Velarde
    ****************************************************************************
    *** This particular Method is intended to take a population as a whole and
    *** mutate each individual path. The likely-hood of a move within the path
    *** mutating is 3%.
    *** Method Inputs: List<String> myPopulation.
    *** Return value: List<String>.
    ****************************************************************************
    *** July 8, 2015
    ****************************************************************************
    * July 15, 2015 (Capri Sims)
    *   moved to Mice
    *   changed to private void and removed input and output
    *   replaced population with mutatedPopulation at end in place of return
    *   changed myPopulatioin to population
    *   replaced random number generator with rng
    *   added totalCount and mutateCount
    *   MUTATION RATE WAY TOO HIGH 163%
    * July 17, 2015 (Capri Sims)
    *   moved if statement outside of for loop
    *   moved subtract statement into loop 
    *   removed inner for loop
    *   set i to random number
    *   removed currentMove & replaced it with move
    *   placed the contents of path into move as chars
    *   changed how case 0 and 1 work to work with move
    *   removed mutatedPath, mutatedPopulation, and related 
    *   removed test comments
    *   changed total and likely _chance
    * July 18, 2015 (Capri Sims)
    *   removed total and likely _chance to make global for virus use
    ****************************************************************************
    **/
    private void mutate()
    {
        char[] movePool = {'w', 'a', 's', 'd'};

        for(String path : population)
        {
            totalCount++;
            //recreate 'move' everytime to avoid clearing it
            List<Character> move = new ArrayList<>();
            for(char ch : path.toCharArray()){
                move.add(ch);
            }
            
            if(rng.nextInt(TOTAL_CHANCE) <= LIKELY_CHANCE)
                {
                mutateCount++;
            
                int i = rng.nextInt(path.length());
                
                //each mutation has a (LIKELY_CHANCE/3)% chance of occuring
                switch(rng.nextInt(3))
                {
                    case 0: //swap moves
                    {
                        move.set(i, movePool[rng.nextInt(4)]);
                        break;
                    }
                    case 1: //subtract a move
                    {
                        move.remove(i);
                        break;
                    }
                    case 2: //add a move
                    {
                        move.add(i,movePool[rng.nextInt(4)]);
                        break;
                    }
                }
                
                String temp = "";
                for(char ch : move){
                    temp += ch;
                }    
                
                path = temp; 
            }
        }
    }

    
    
    /*********************************************************
    ***  generate
    ***  Jennifer Demieville
    ******************************************************
    *** Produces random maze solutions, randomizes turns 
    *** as well as length of solutions
    ******************************************************
    *** 7/5/15
    ******************************************************
    *** 7/13/15 @ 15:11- changed array size to random with 
    *** maximum value of 100
    ******************************************************
    *** 7/15/2015 (Capri Sims)
    *    Changed name from solution to generate
    *    placed in Mice
    *    added turns to population in string form
    *    replaced random number generators with rng
    *    changed length of turns
    *    wrapped in for loop
    *   7/17/2015 (Capri Sims)
    *    changed size of turns
    ********************************************************/
    private void generate(){
        
        for(int j = 0; j < size; j++){

            char[] turns = new char[rng.nextInt(50) + 300]; 

            for (int i = 0; i < turns.length; i++)      //equates random values generated with turns in mazes
            {
                    int number = rng.nextInt(4);
                    char direction = ' ';

                    if (number == 1)
                        direction = 'w';
                    else if (number == 2)
                        direction = 's';
                    else if (number == 3)
                        direction = 'd';
                    else 
                        direction = 'a';

                    turns[i] = direction;
             }
             population.add(String.valueOf(turns));
        }
    }

    
    /******************************************************
    ***  Method Name: selection
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To select parents for breeding. Uses 
    *       the tournament method of selection. 
    *** Method Inputs: None
    *** Return value: None
    ******************************************************
    *** Date: July 9, 2015
    ******************************************************
    *** Changes: (Capri Sims)
    *   07/15/2015 - wrapped in for loop
    *   07/15/2015 - changed location of tempBest initialization
    *   07/18/2015 - added dice roll to randomize tournament size
    *   07/18/2015 - added incest prevention
    *******************************************************/
    private void selection(){
        int index, tempBest, dice;
        parents.clear();
        
        for(int j = 0; j < (size * 2); j++){
            tempBest = -1;
            dice = rng.nextInt(4) + 2;
            for(int i =0 ; i < dice; i++){ 
                index = rng.nextInt(size);
                if(tempBest == -1 || fitness.get(index) > fitness.get(tempBest)){  //Dependent on how fitness is calculated 
                    tempBest = index;
                }
            }
            parents.add(population.get(tempBest));
            
            //Incest prevention
            if(j > 0 && parents.get(j).equals(parents.get(j - 1))){
                tempBest = -1;
                for(int i =0 ; i < dice; i++){ 
                    index = rng.nextInt(size);
                    if(tempBest == -1 || fitness.get(index) > fitness.get(tempBest)){  
                        tempBest = index;
                    }
                }
                parents.set(j, population.get(tempBest));
            }
        }
        
    }
    
    /******************************************************
    ***  Method Name: getBest
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To return best.
    *** Method Inputs: None
    *** Return value: List<String> best
    ******************************************************
    *** Date: July 9, 2015
    ******************************************************
    *** 
    *******************************************************/
    public List<String> getBest(){
        return best;
    }
    
    /******************************************************
    ***  Method Name: getFitness
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To return the fitnesses of best.
    *** Method Inputs: None
    *** Return value: list<Integer> bestFitness
    ******************************************************
    *** Date: July 10, 2015
    ******************************************************
    *** 
    *******************************************************/
    public List<Integer> getFitness(){ 
        return bestFitness;
    }
    
    /******************************************************
    ***  Method Name: getIterations
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To return iterations.
    *** Method Inputs: None
    *** Return value: int iterations
    ******************************************************
    *** Date: July 10, 2015
    ******************************************************
    *** 
    *******************************************************/
    public int getIterations(){
        return iterations;
    }
    
    /******************************************************
    ***  Method Name: virus
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To increase genetic diversity.
    *** Method Inputs: None
    *** Return value: None
    ******************************************************
    *** Date: July 18, 2015
    ******************************************************
    *** 
    *******************************************************/
    private void virus(){
        double similarity = 0;
        
        for(int i = 0; i < size - 1; i++){
            if(Objects.equals(fitness.get(i), fitness.get(i + 1))){
                similarity++;
            }
        }
        similarity = similarity / size;
        
        //System.out.println(similarity);
        
        if(similarity >= .1){
            LIKELY_CHANCE = 10000;
            if(similarity >= .3){
                for(int i = 0; i < rng.nextInt(50) + 50; i++){
                    int k = rng.nextInt(3);
                    if( k == rng.nextInt(3)){
                    
                        int insert = rng.nextInt(size);
                        String temp = "";

                        for (int j = 0; j < 350; j++){
                            int number = rng.nextInt(4);

                            if (number == 1)
                                temp += 'w';
                            else if (number == 2)
                                temp += 's';
                            else if (number == 3)
                                temp += 'd';
                            else 
                                temp += 'a';
                         }

                        population.set(insert, temp);
                    }
                }
            }
        }
    }

    
}
