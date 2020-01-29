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

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
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

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors

    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
    void updateWorld() {
        // % of surrounding neighbours that are like me
        double threshold = 0.7;
        setSatisfied(threshold);

        /////////////////////////////////////
        //1. räkna antal dissatisfied blå, röd och hur många tomma platser
        //2. gå igenom lista från början, vid tom plats, slumpa om en röd eller blå ska läggas
        //3. kolla om satisfied
        //4. gör igen
        ///////////////////////////////////


        // TODO update world
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime
    // That's why we must have "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test();    // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};

        // Number of locations (places) in world (must be a square)
        int nLocations = 900;   // Should also try 90 000
        int sideLen    = (int) sqrt(nLocations);
        Random rand = new Random();

        world = new Actor[sideLen][sideLen];

        for (int i = 0; i < sideLen; i++) {

            for (int j = 0; j < sideLen; j++) {

                double prob = rand.nextDouble();
                if (prob < dist[0]) {
                    world[i][j] = new Actor(Color.RED);
                }
                else if(prob < dist[2]) {
                    world[i][j] = new Actor(Color.BLUE);
                }

            }

        }
        fixScreenSize(nLocations);
    }

    // ---------------  Methods ------------------------------

    void setSatisfied(double threshold) {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {

                if (world[i][j] == null) continue;

                double stat = scanNine(i,j, world[i][j].color, world);
                if (stat < threshold) {
                    world[i][j].isSatisfied = false;
                }
                else {
                    world[i][j].isSatisfied = true;
                }

                out.println(world[i][j].isSatisfied + " at " + i + " " + j + ", stat=" + stat);
                
            }
        }
    }

    //REMOVW ACTORCOLOR SENARE
    double scanNine(int y, int x, Color actorColor, Actor[][] tW){

        int numAct = 0;
        int friends= 0;

        int yStart = y-1;
        int yEnd = (y + 1);

        int xStart = x-1;
        int xEnd = (x + 1);

        if (y == 0) {
            yStart = 0;
        }
        else if (y == tW.length-1) {
            yEnd = y;
        }

        if (x == 0){
            xStart = 0;
        }
        else if (x == tW.length-1) {
            xEnd = x;
        }

        for (int i = yStart; i <= yEnd; i++) {

            for (int j = xStart; j <= xEnd; j++) {

                if((y == i && x == j) || tW[i][j] == null) continue;

                if (tW[i][j].color == actorColor) {
                   friends++;
                }
                if (tW[i][j].color == Color.BLUE || tW[i][j].color == Color.RED) {
                    numAct++;
                }

            }

        }

        if (numAct == 0) return 1;

        return (double)friends / (double)numAct;
        
    }

    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size && 0 <= col && col < size;
    }

    // ----------- Utility methods -----------------

    // TODO (general method possible reusable elsewhere)

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

        out.println(scanNine(2,0, Color.RED, testWorld));

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
