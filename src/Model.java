import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Model of the game of breakout
 * @author Mike Smith University of Brighton
 */

public class Model extends Observable
{
    // Boarder
    private static final int B              = 6;  // Border offset
    private static final int M              = 40; // Menu offset

    // Size of things
    private static final float BALL_SIZE    = 30; // Ball side
    private static final float BRICK_WIDTH  = 50; // Brick size
    private static final float BRICK_HEIGHT = 30;

    private static final int BAT_MOVE       = 10; // Distance to move bat

    // Scores
    private static final int HIT_BRICK      = 50;  // Score
    private static final int HIT_BOTTOM     = -200;// Score

    private GameObj ball;          // The ball
    private List<GameObj> bricks;  // The bricks
    private List<GameObj> twoBricks;  // The bricks
    private GameObj bat;           // The bat

    private boolean runGame = true; // Game running
    private boolean fast = false;   // Sleep in run loop

    private int score = 0;

    private final float W;         // Width of area
    private final float H;         // Height of area

    File brickBreak = new File("break.wav");  
    File bounce = new File("bounce.wav");

    static void PlaySound(File Sound)
    {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(Sound));
            clip.start();

            //Thread.sleep(clip.getMicrosecondLength()/1000);
        } catch (Exception e)
        {

        }
    }

    public Model( int width, int height )
    {
        this.W = width; this.H = height;
    }

    /**
     * Create in the model the objects that form the game
     */

    public void createGameObjects()
    {
        synchronized( Model.class )
        {
            ball   = new GameObj(W/2, H/2, BALL_SIZE, BALL_SIZE, Colour.RED );
            bat    = new GameObj(W/2, H - BRICK_HEIGHT*1.5f, BRICK_WIDTH*3, BRICK_HEIGHT/4, Colour.GRAY);
            bricks = new ArrayList<>();
            twoBricks = new ArrayList<>();
            int addWidth = 28;
            int addHeight = 100;

            // *[1]******************************************************[1]*
            // * Fill in code to place the bricks on the board              *
            // **************************************************************
            for (int k=0; k < 1; k++) 
            {
                for (int p=0; p < 8; p++)
                {
                    twoBricks.add(new GameObj(addWidth, addHeight, BRICK_WIDTH, BRICK_HEIGHT, Colour.darkGray));
                    addWidth += 70;
                }

                addWidth = 28;
                addHeight += 40;
            }
            for (int i=0; i < 3; i++) 
            {
                for (int j=0; j < 8; j++)
                {
                    bricks.add(new GameObj(addWidth, addHeight, BRICK_WIDTH, BRICK_HEIGHT, Colour.BLUE));
                    addWidth += 70;
                }

                addWidth = 28;
                addHeight += 40;
            }
        }
    }

    private ActivePart active  = null;

    /**
     * Start the continuous updates to the game
     */
    public void startGame()
    {
        synchronized ( Model.class )
        {
            stopGame();
            active = new ActivePart();
            Thread t = new Thread( active::runAsSeparateThread );
            t.setDaemon(true);   // So may die when program exits
            t.start();
        }
    }

    /**
     * Stop the continuous updates to the game
     * Will freeze the game, and let the thread die.
     */
    public void stopGame()
    {  
        synchronized ( Model.class )
        {
            if ( active != null ) { active.stop(); active = null; }
        }
    }

    public GameObj getBat()             { return bat; }

    public GameObj getBall()            { return ball; }

    public List<GameObj> getBricks()    { return bricks; }

    public List<GameObj> getTwoBricks()    { return twoBricks; }

    /**
     * Add to score n units
     * @param n units to add to score
     */
    protected void addToScore(int n)    { score += n; }

    public int getScore()               { return score; }

    /**
     * Set speed of ball to be fast (true/ false)
     * @param fast Set to true if require fast moving ball
     */
    public void setFast(boolean fast)   
    { 
        this.fast = fast; 
    }

    /**
     * Move the bat. (-1) is left or (+1) is right
     * @param direction - The direction to move
     */
    public void moveBat( int direction )
    {
        // *[2]******************************************************[2]*
        // * Fill in code to prevent the bat being moved off the screen *
        // **************************************************************
        if ((bat.getX() > 0) && (bat.getX() < 450)) 
        {
            float dist = direction * BAT_MOVE;    // Actual distance to move
            Debug.trace( "Model: Move bat = %6.2f", dist );
            bat.moveX(dist);
        }
        if (bat.getX() == 0) 
        {
            bat.moveX(BAT_MOVE);
        }
        if (bat.getX() == 450)
        {
            bat.moveX(-BAT_MOVE);
        }
    }

    /**
     * This method is run in a separate thread
     * Consequence: Potential concurrent access to shared variables in the class
     */
    class ActivePart
    {
        private boolean runGame = true;

        public void stop()
        {
            runGame = false;
        }

        public void runAsSeparateThread()
        {
            final float S = 3; // Units to move (Speed)
            try
            {
                synchronized ( Model.class ) // Make thread safe
                {
                    GameObj       ball   = getBall();     // Ball in game
                    GameObj       bat    = getBat();      // Bat
                    List<GameObj> bricks = getBricks();   // Bricks
                    List<GameObj> twoBricks = getTwoBricks();  // The bricks
                }

                while (runGame)
                {
                    synchronized ( Model.class ) // Make thread safe
                    {
                        float x = ball.getX();  // Current x,y position
                        float y = ball.getY();
                        // Deal with possible edge of board hit
                        if (x >= W - B - BALL_SIZE)  ball.changeDirectionX();
                        if (x <= 0 + B            )  ball.changeDirectionX();
                        if (y >= H - B - BALL_SIZE)  // Bottom
                        { 
                            ball.changeDirectionY(); addToScore( HIT_BOTTOM ); 
                        }
                        if (y <= 0 + M            )  ball.changeDirectionY();

                        // As only a hit on the bat/ball is detected it is 
                        //  assumed to be on the top or bottom of the object.
                        // A hit on the left or right of the object
                        //  has an interesting affect

                        boolean hit = false;                        
                        // *[3]******************************************************[3]*
                        // * Fill in code to check if a visible brick has been hit      *
                        // *      The ball has no effect on an invisible brick          *
                        // **************************************************************
                        if (hit)
                        {
                            ball.changeDirectionY();
                        }

                        if ( ball.hitBy(bat) )
                        {
                            ball.changeDirectionY();
                            PlaySound(bounce);
                        }

                        for (GameObj thisBrick: bricks)
                        {
                            if (thisBrick.hitBy(ball))
                            {
                                if (thisBrick.isVisible()) {
                                    thisBrick.setVisibility(false);
                                    ball.changeDirectionY(); 
                                    PlaySound(brickBreak);
                                    addToScore(200);
                                }
                            }                             
                        }
                        for (GameObj thisBrick: twoBricks)
                        {
                            if (thisBrick.hitBy(ball))
                            {
                                if (thisBrick.isVisible()) {
                                    bricks.add(new GameObj(thisBrick.getX(), thisBrick.getY(),  BRICK_WIDTH, BRICK_HEIGHT, Colour.BLUE));
                                    thisBrick.setVisibility(false);
                                    PlaySound(brickBreak);
                                    addToScore(200);                                    
                                    ball.changeDirectionY(); 
                                }
                            }                             
                        }
                    }
                    modelChanged();      // Model changed refresh screen
                    Thread.sleep( fast ? 2 : 20 );
                    ball.moveX(S);  ball.moveY(S);
                }
            } catch (Exception e) 
            { 
                Debug.error("Model.runAsSeparateThread - Error\n%s", 
                    e.getMessage() );
            }
        }
    }

    /**
     * Model has changed so notify observers so that they
     *  can redraw the current state of the game
     */
    public void modelChanged()
    {
        setChanged(); notifyObservers();
    }
}
