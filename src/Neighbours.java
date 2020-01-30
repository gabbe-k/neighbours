import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.*;
import static java.lang.System.*;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    class Actor {
        final Color color;        // Color an existing JavaFX class
        boolean isSatisfied;      // false by default

        Actor(Color color) {      // Constructor to initialize
            this.color = color;
        }

    }

    Actor[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        double threshold = 0.7;
        world = setSatisfied(threshold, world);
        int[] newPop = countDissatisfied(world);
        out.println(Arrays.toString(newPop));
        if (newPop[0] == 0 && newPop[1] == 0) {
            out.println("All satisfied");
            exit(0);
        }
        else {
            world = distribute(world, newPop);
            world = removeUnsatisfied(world);
        }

    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime
    // That's why we must have "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.5};
        // Number of locations (places) in world (must be a square)
        int nLocations = 90000;   // Should also try 90 000
        int sideLen    = (int) sqrt(nLocations);

        world = new Actor[sideLen][sideLen];

        int[] population = {(int)(nLocations*dist[0]),  //Number of red
                            (int)(nLocations*dist[1]),  //Number of blue
                            (int)(nLocations*dist[2])}; //Number of available squares

        //All squares are available in initiation
        world = distribute(world, population);

        fixScreenSize(nLocations);
    }

    // ---------------  Methods ------------------------------

    Actor[][] distribute(Actor[][] world, int[] pop) {

        Random rand = new Random();

        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[row].length; col++) {

                int numPop = pop[0] + pop[1] + pop[2];

                if (world[row][col] == null) {

                    int index = rand.nextInt(numPop);
                    if (index < pop[0]) {
                        world[row][col] = new Actor(Color.RED);
                        world[row][col].isSatisfied = true;
                        pop[0]--;
                    } else if (index < pop[0] + pop[1]) {
                        world[row][col] = new Actor(Color.BLUE);
                        world[row][col].isSatisfied = true;
                        pop[1]--;
                    } else {
                        pop[2]--;
                    }
                }

            }
        }

        return world;

    }

    Actor[][] removeUnsatisfied(Actor[][] world) {
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world.length; col++) {
                if (world[row][col] != null && world[row][col].isSatisfied == false) {
                    world[row][col] = null;
                }
            }
        }
        return world;
    }

    Actor[][] setSatisfied(double threshold, Actor[][] world) {
        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world[row].length; col++) {

                if (world[row][col] == null) continue;

                double stat = scanNeighbours(row,col, world);
                if (stat < threshold) {
                    world[row][col].isSatisfied = false;
                }
                else {
                    world[row][col].isSatisfied = true;
                }

            }
        }
        return world;
    }

    int[] countDissatisfied(Actor[][] world) {
        int red = 0;
        int blue = 0;
        int empty = 0;

        for (int row = 0; row < world.length; row++) {
            for (int col = 0; col < world.length; col++) {
                Actor act = world[row][col];

                if (act == null) {
                    empty++;
                }
                else {

                    if (act.isSatisfied == false) {

                        if (act.color == Color.BLUE){
                            blue++;
                        }
                        else if(act.color == Color.RED) {
                            red++;
                        }

                    }
                }


            }

        }
        int available = empty - red - blue;
        return new int[] {red, blue, available};
        
    }

    double scanNeighbours(int actorRow, int actorCol, Actor[][] world){

        int numAct = 0;
        int friends= 0;
        Color actorColor = world[actorRow][actorCol].color;

        for (int row = actorRow-1; row <= actorRow+1; row++) {

            for (int col = actorCol-1; col <= actorCol+1; col++) {

                if (!isValidLocation(world.length,row,col) ||
                    actorRow == row && actorCol == col     ||
                    world[row][col] == null)
                    continue;

                if (world[row][col].color == actorColor) {
                    friends++;
                }
                if (world[row][col].color == Color.BLUE || world[row][col].color == Color.RED) {
                    numAct++;
                }

            }

        }

        if (numAct == 0) return 0;

        return (double)friends / (double)numAct;
        
    }


    // ----------- Utility methods -----------------

    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size && 0 <= col && col < size;
    }

    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work. Important!!!!
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {new Actor(Color.RED), new Actor(Color.RED), null},
                {null, new Actor(Color.BLUE), null},
                {new Actor(Color.RED), null, new Actor(Color.BLUE)}
        };
        double th = 0.5;   // Simple threshold used for testing

        int size = testWorld.length;
        out.println(isValidLocation(size, 0, 0));
        out.println(!isValidLocation(size, -1, 0));
        out.println(!isValidLocation(size, 0, 3));

        out.println(scanNeighbours(2,0, testWorld));

        // TODO

        exit(0);
    }

    // ******************** NOTHING to do below this row, it's JavaFX stuff  **************

    double width = 500;   // Size for window
    double height = 500;
    final double margin = 50;
    double dotSize;

    void fixScreenSize(int nLocations) {
        // Adjust screen window
        dotSize = 9000 / nLocations;
        if (dotSize < 1) {
            dotSize = 2;
        }
        width = sqrt(nLocations) * dotSize + 2 * margin;
        height = width;
    }

    long lastUpdateTime;
    final long INTERVAL = 450_000_000;


    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long now) {
                long elapsedNanos = now - lastUpdateTime;
                if (elapsedNanos > INTERVAL) {
                    updateWorld();
                    renderWorld(gc);
                    lastUpdateTime = now;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = (int) (dotSize * col + margin);
                int y = (int) (dotSize * row + margin);
                if (world[row][col] != null) {
                    g.setFill(world[row][col].color);
                    g.fillOval(x, y, dotSize, dotSize);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
