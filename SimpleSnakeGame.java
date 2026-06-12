import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class SimpleSnakeGame extends JPanel implements ActionListener {

    // Game configuration constants
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 600;
    private static final int GRID_SIZE = 25; // Size of each grid unit
    private static final int GAME_UNITS = (BOARD_WIDTH * BOARD_HEIGHT) / (GRID_SIZE * GRID_SIZE);
    private static final int DELAY = 150; // Game speed (lower is faster)

    // Game state variables
    private final ArrayList<Point> snake = new ArrayList<>();
    private Point apple;
    private char direction = 'R'; // U = Up, D = Down, L = Left, R = Right
    private boolean running = false;
    private Timer timer;
    private final Random random = new Random();

    public SimpleSnakeGame() {
        this.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    /**
     * This method spawns an apple for the snake to eat.
     */
    public void spawnApple() {
        int x = random.nextInt((int) (BOARD_WIDTH / GRID_SIZE)) * GRID_SIZE;
        int y = random.nextInt((int) (BOARD_HEIGHT / GRID_SIZE)) * GRID_SIZE;
        apple = new Point(x, y);
    }
    
    /**
     * This method starts the game.
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

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * This method creates the graphics seen on screen.
     * @param g
     */
    public void draw(Graphics g) {
        if (running) {
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * This method creates the movement of the snake.
     */
    public void move() {
        // Create a new head position based on current direction
        Point head = snake.get(0);
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case 'U' -> newHead.y -= GRID_SIZE;
            case 'D' -> newHead.y += GRID_SIZE;
            case 'L' -> newHead.x -= GRID_SIZE;
            case 'R' -> newHead.x += GRID_SIZE;
        }

        // Add the new head to the front of the list
        snake.add(0, newHead);

        // Check if snake ate the apple
        if (newHead.equals(apple)) {
            spawnApple();
        } else {
            // Remove the tail if it didn't eat an apple to mimic moving forward
            snake.remove(snake.size() - 1);
        }
    }

    /**
     * This method checks to see if the snake collided with the edge of the screen, or itself.
     * If collision is detected, the game ends.
     */
    public void checkCollisions() {
        Point head = snake.get(0);

        // Check if head collides with body
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }

        // Check if head touches screen boundaries
        if (head.x < 0 || head.x >= BOARD_WIDTH || head.y < 0 || head.y >= BOARD_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    /**
     * This method creates the "Game Over" screen.
     * @param g
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
     * This method checks for movement and collisions.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollisions();
        }
        repaint();
    }

    // Inner class to handle keyboard inputs
    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
                case KeyEvent.VK_SPACE:
                    if (!running) {
                        startGame();
                    }
                    break;
            }
        }
    }

    // Main method to set up the Application Frame
    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple Snake Game");
        SimpleSnakeGame gamePanel = new SimpleSnakeGame();

        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack(); // Pack sizes the frame so that all its contents are at or above their preferred sizes
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
}