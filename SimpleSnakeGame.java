import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A simple, classic Snake game implemented in a single Java file using Java Swing.
 * The game features a main menu start screen, real-time grid-based movement,
 * fruit spawning, collision detection, and a game-over screen with restart functionality.
 * Supports both Arrow keys and WASD keys for alternative player control layouts.
 * * @author AI Assistant
 * @version 1.4
 */
public class SimpleSnakeGame extends JPanel implements ActionListener {

    // Game configuration constants
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 600;
    private static final int GRID_SIZE = 25; // Size of each grid unit
    private static final int DELAY = 150; // Game speed (lower is faster)

    // Game state variables
    private final ArrayList<Point> snake = new ArrayList<>();
    private Point apple;
    private char direction = 'R'; // U = Up, D = Down, L = Left, R = Right
    private boolean isStarted = false; // Tracks if the user has left the start screen
    private boolean running = false;   // Tracks if the active game is running or over
    private Timer timer;
    private final Random random = new Random();

    /**
     * Constructs a new SimpleSnakeGame panel.
     * Sets up the window dimensions, background color, focusable state for inputs,
     * and attaches the keyboard listener.
     */
    public SimpleSnakeGame() {
        this.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
    }

    /**
     * Initializes a new game session.
     * This method resets the snake to its default length and starting position,
     * resets the direction, spawns the first apple, updates state flags, 
     * and manages the game loop {@link javax.swing.Timer}.
     */
    public void startGame() {
        snake.clear();
        // Start with a snake of length 3 in the middle of the screen
        snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        snake.add(new Point(BOARD_WIDTH / 2 - GRID_SIZE, BOARD_HEIGHT / 2));
        snake.add(new Point(BOARD_WIDTH / 2 - (2 * GRID_SIZE), BOARD_HEIGHT / 2));

        spawnApple();
        direction = 'R';
        running = true;
        isStarted = true;
        
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Randomly generates coordinates for an apple within the grid boundaries.
     * The coordinates are snapped perfectly to the {@link #GRID_SIZE}.
     */
    public void spawnApple() {
        int x = random.nextInt((int) (BOARD_WIDTH / GRID_SIZE)) * GRID_SIZE;
        int y = random.nextInt((int) (BOARD_HEIGHT / GRID_SIZE)) * GRID_SIZE;
        apple = new Point(x, y);
    }

    /**
     * Overrides the Swing JComponent paint method to draw game elements.
     * * @param g the {@link Graphics} context used for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Determines the current state of the game and delegates the rendering
     * to the appropriate specialized screen or gameplay drawing routines.
     * * @param g the {@link Graphics} context used for drawing.
     */
    public void draw(Graphics g) {
        if (!isStarted) {
            drawStartScreen(g);
        } else if (running) {
            // Draw Apple
            g.setColor(Color.RED);
            g.fillOval(apple.x, apple.y, GRID_SIZE, GRID_SIZE);

            // Draw Snake
            for (int i = 0; i < snake.size(); i++) {
                if (i == 0) {
                    g.setColor(Color.GREEN); // Head
                } else {
                    g.setColor(new Color(45, 180, 0)); // Body
                }
                g.fillRect(snake.get(i).x, snake.get(i).y, GRID_SIZE, GRID_SIZE);
            }

            // Draw Score
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + (snake.size() - 3), 10, 30);
            
        } else {
            gameOver(g);
        }
    }

    /**
     * Renders the introductory start screen text, including instructions
     * on how to control the snake and how to begin the game.
     * * @param g the {@link Graphics} context used for drawing.
     */
    public void drawStartScreen(Graphics g) {
        // Title Text
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        FontMetrics titleMetrics = getFontMetrics(g.getFont());
        g.drawString("SNAKE GAME", (BOARD_WIDTH - titleMetrics.stringWidth("SNAKE GAME")) / 2, BOARD_HEIGHT / 3);

        // Subtitle / Instructions
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics instructMetrics = getFontMetrics(g.getFont());
        g.drawString("Use Arrow Keys or WASD to Navigate", (BOARD_WIDTH - instructMetrics.stringWidth("Use Arrow Keys or WASD to Navigate")) / 2, BOARD_HEIGHT / 2);

        // Action Text
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics actionMetrics = getFontMetrics(g.getFont());
        g.drawString("Press ENTER to Start", (BOARD_WIDTH - actionMetrics.stringWidth("Press ENTER to Start")) / 2, (BOARD_HEIGHT / 2) + 80);
    }

    /**
     * Advances the snake forward by one grid block based on the current direction.
     * A new head position is pushed onto the front of the list. If an apple is consumed, 
     * a new apple is spawned; otherwise, the tail is removed to simulate movement.
     */
    public void move() {
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case 'U' -> newHead.y -= GRID_SIZE;
            case 'D' -> newHead.y += GRID_SIZE;
            case 'L' -> newHead.x -= GRID_SIZE;
            case 'R' -> newHead.x += GRID_SIZE;
        }

        snake.add(0, newHead);

        if (newHead.equals(apple)) {
            spawnApple();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    /**
     * Checks if the snake has collided with its own body or the frame borders.
     * If a collision is flagged, the game loop is terminated.
     */
    public void checkCollisions() {
        Point head = snake.get(0);

        // Check body collision
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }

        // Check wall collision
        if (head.x < 0 || head.x >= BOARD_WIDTH || head.y < 0 || head.y >= BOARD_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    /**
     * Renders the Game Over screen, showcasing the player's final performance 
     * statistics and a dynamic prompt to trigger a restart.
     * * @param g the {@link Graphics} context used for drawing.
     */
    public void gameOver(Graphics g) {
        // Game Over text
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (BOARD_WIDTH - metrics.stringWidth("Game Over")) / 2, BOARD_HEIGHT / 2);

        // Final Score text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 25));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Final Score: " + (snake.size() - 3), (BOARD_WIDTH - metrics2.stringWidth("Final Score: " + (snake.size() - 3))) / 2, BOARD_HEIGHT / 2 + 50);
        
        // Restart prompt
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("Press SPACE to Restart", (BOARD_WIDTH - metrics3.stringWidth("Press SPACE to Restart")) / 2, BOARD_HEIGHT / 2 + 100);
    }

    /**
     * Receives event triggers from the game loop Timer on fixed delays.
     * If the game is ongoing, updates calculations and repaints the canvas.
     * * @param e the triggering {@link ActionEvent}.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && isStarted) {
            move();
            checkCollisions();
        }
        repaint();
    }

    /**
     * An inner key adapter tracking user keystrokes to execute game navigation,
     * start commands, or game state restarts. Maps both Arrow and WASD schemes.
     */
    private class MyKeyAdapter extends KeyAdapter {
        
        /**
         * Invoked whenever a physical keyboard key is pressed down.
         * Controls menu logic, prevention of directional self-intersection, and restarts.
         * * @param e the triggering {@link KeyEvent}.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            // Handle start screen transition
            if (!isStarted && keyCode == KeyEvent.VK_ENTER) {
                startGame();
                return;
            }

            // Handle gameplay controls
            if (running) {
                switch (keyCode) {
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> { 
                        if (direction != 'R') direction = 'L'; 
                    }
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> { 
                        if (direction != 'L') direction = 'R'; 
                    }
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> { 
                        if (direction != 'D') direction = 'U'; 
                    }
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> { 
                        if (direction != 'U') direction = 'D'; 
                    }
                }
            } else {
                // Handle restart if game is over
                if (keyCode == KeyEvent.VK_SPACE) {
                    startGame();
                }
            }
        }
    }

    /**
     * Application entryway. Sets up the window framework configuration wrapper 
     * (JFrame) and renders the program viewable on desktop monitors.
     * * @param args command-line arguments (unused).
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple Snake Game");
        SimpleSnakeGame gamePanel = new SimpleSnakeGame();
        
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Trigger an initial paint to render the start screen immediately
        gamePanel.repaint();
    }
}