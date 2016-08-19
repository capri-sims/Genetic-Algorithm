/******************************************************
***  Class Name: GUI
***  Class Author: Capri Sims
******************************************************
*** Purpose: To create the graphical user interface
*       for Mice. Allows input of maze and shows 
*       solutions.
******************************************************
*** Date: July 10, 2015
******************************************************
*** 
*******************************************************/
package group4_geneticalgorithmproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI extends JFrame{
    private Path file;
    private char[][] maze = new char[20][20];
    private JPanel[][] viewMaze = new JPanel[20][20];
    private JPanel mazeContainer = new JPanel(new GridLayout(20,20));
    private JPanel top = new JPanel(), bottom = new JPanel(); 
    private Mice myMice;
    private List<String> best = new ArrayList<>();
    private List<Integer> fitness = new ArrayList<>();
    private int row, col;
    private Timer timer;
    private JLabel generationLabel, fitnessLabel, stringLabel;
    private boolean stop;
    
    public GUI(){
        
        super("Mouse Maze");
        setLayout(new BorderLayout());
        setSize(700, 850);
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        
        generationLabel = new JLabel("Please enter a maze.", JLabel.LEFT);
        generationLabel.setBorder(new EmptyBorder(0,0,0,100));
        fitnessLabel = new JLabel("", JLabel.RIGHT); 
        fitnessLabel.setBorder(new EmptyBorder(0,75,0,50));
        top.setBorder(new EmptyBorder(20,20,20,20));
        top.add(generationLabel);
        top.add(fitnessLabel);
        add(top, BorderLayout.NORTH);
        
        stringLabel = new JLabel();
        //stringLabel.setBorder(new EmptyBorder(0,0,0,0)); 
        bottom.setPreferredSize(new Dimension(700, 100)); //if window size is changed, change this too
        bottom.add(stringLabel);
        bottom.setBorder(new EmptyBorder(20,20,20,20));
        add(bottom, BorderLayout.SOUTH);
        
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem addMaze = new JMenuItem("New Maze...");
        fileMenu.add(addMaze);
        bar.add(fileMenu);
        setJMenuBar(bar);
        
        
        setVisible(true);
        
        addMaze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                stop = true;
                generationLabel.setText("Calculating....");
                fitnessLabel.setText("");
                mazeContainer.removeAll();
                
                fileReader();
                showMaze();

                myMice = new Mice(maze, row, col);
                best = myMice.getBest();
                fitness = myMice.getFitness();
                moveMouse();
            }

        });   
    }
    
    /******************************************************
    ***  Method Name: moveMouse
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To show the mouse moving on screen.
    *** Method Inputs: None
    *** Return value: None
    ******************************************************
    *** Date: July 11, 2015
    ******************************************************
    *** This took a whole day to code. Timers and GUIs 
    * are very tricky.
    *******************************************************
    *** Changes: 
    *   7/15/2015 (Capri Sims)
    *   Added checks in the if statements to prevent 
    *     going out of bounds
    *   Changed if statements to allow more movement
    *   Reduced time
    *******************************************************/
    private void moveMouse(){
        //initial mouse position (row,col) found in showMaze
        int time = 30; //.03 seconds
        final Counter counter = new Counter();
        counter.times = 0;
        counter.index = 0;
        stop = false;
        
        timer = new Timer(time, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(counter.times == myMice.getIterations() || stop){
                    timer.stop();
                    return;
                }
                
                char[] mouse = best.get(counter.times).toCharArray();
                generationLabel.setText("Generation: " + (counter.times));
                fitnessLabel.setText("Fitness: " + fitness.get(counter.times));
                
                if(counter.index != mouse.length){
                    char ch = mouse[counter.index];
                    if(ch == 'w' && row != 0 && maze[row-1][col] != 'x'){ //prevents out of bounds
                        viewMaze[row][col].setBackground(Color.CYAN);
                        viewMaze[--row][col].setBackground(Color.BLUE);
                    }
                    else if(ch == 'a' && col != 0 && maze[row][col-1] != 'x'){
                        viewMaze[row][col].setBackground(Color.CYAN);
                        viewMaze[row][--col].setBackground(Color.BLUE);
                    }
                    else if(ch == 's' && row != maze.length - 1 && maze[row+1][col] != 'x'){
                        viewMaze[row][col].setBackground(Color.CYAN);
                        viewMaze[++row][col].setBackground(Color.BLUE);
                    }
                    else if(ch == 'd' && col != maze[0].length - 1 && maze[row][col+1] != 'x'){
                        viewMaze[row][col].setBackground(Color.CYAN);
                        viewMaze[row][++col].setBackground(Color.BLUE);
                    }

                    counter.index++;
                    add(mazeContainer, BorderLayout.CENTER);
                    mazeContainer.revalidate();
                    mazeContainer.repaint();
                }
                else{
                    resetMaze();
                    counter.times += 5; //skip every 5 generations
                    counter.index = 0;
                }
            }
        });
        timer.start();
    }
    
    /******************************************************
    ***  Method Name: resetMaze
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To reset the maze after each generation.
    *** Method Inputs: None
    *** Return value: None
    ******************************************************
    *** Date: July 11, 2015
    ******************************************************
    *** 
    *******************************************************/
    private void resetMaze(){

        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 20; j++){
                
                if(maze[i][j] == 'x')
                    viewMaze[i][j].setBackground(Color.BLACK); 
                else if(maze[i][j] == 'm'){
                    viewMaze[i][j].setBackground(Color.BLUE); 
                    row = i;
                    col = j; //mouse position
                }
                else{
                    viewMaze[i][j].setBackground(Color.WHITE);
                }

            }
        }
        add(mazeContainer, BorderLayout.CENTER);
        mazeContainer.revalidate();
        mazeContainer.repaint();      
    }
    
    /******************************************************
    ***  Method Name: showMaze
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To show the maze.
    *** Method Inputs: None
    *** Return value: None
    ******************************************************
    *** Date: July 11, 2015
    ******************************************************
    *** 
    *******************************************************/
    private void showMaze(){
        
        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 20; j++){
                viewMaze[i][j] = new JPanel();
                viewMaze[i][j].setOpaque(true);
                
                if(maze[i][j] == 'x')
                    viewMaze[i][j].setBackground(Color.BLACK); 
                else if(maze[i][j] == 'm'){
                    viewMaze[i][j].setBackground(Color.BLUE); //mouse image??
                    row = i;
                    col = j; //mouse position
                }

                mazeContainer.add(viewMaze[i][j]);
            }
        }
        add(mazeContainer, BorderLayout.CENTER);
        mazeContainer.revalidate();
        mazeContainer.repaint();        
    }
    
    /******************************************************
    ***  Method Name: fileReader
    ***  Method Author: Capri Sims
    ******************************************************
    *** Purpose: To read the file and create the maze array.
    *** Method Inputs: None
    *** Return value: None
    ******************************************************
    *** Date: July 11, 2015
    ******************************************************
    *** 
    *******************************************************/
    private void fileReader(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text", "txt"));
        int result = fileChooser.showOpenDialog(fileChooser);
        
        if(result != JFileChooser.CANCEL_OPTION)
            file = fileChooser.getSelectedFile().toPath();
        
        List<String> text = new ArrayList<>();
        try{ 
            text = Files.readAllLines(file, Charset.defaultCharset()); }
        catch(Exception ex){ System.err.println(ex); }
        
        int i = 0, j = 0;
        for(String line : text){ 
            
            int l = line.length();
            for(char ch : line.toCharArray()){
                if(Character.isLetter(ch)){ //that dot screws with the indexes so it must be skipped
                    maze[i][j] = ch;
                    j++;
                }
                else{
                    l--; //to account for any skipping
                }
            }
            i++;
            j-=l;
        }
        
    }
    

    
}

