import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class sg extends JPanel implements ActionListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int BLOCK_SIZE = 30; // Bigger snake & food
    private final int NUM_BLOCKS_X = WIDTH / BLOCK_SIZE;
    private final int NUM_BLOCKS_Y = HEIGHT / BLOCK_SIZE;

    private LinkedList<Point> snake;
    private Point food;
    private List<Point> obstacles;
    private boolean gameOver = false;
    private boolean inMenu = true;
    private int direction;
    private int score = 0;
    private javax.swing.Timer timer; // ✅ Swing Timer fixed
    private boolean sprinting = false;

    public sg() {
        snake = new LinkedList<>();
        snake.add(new Point(NUM_BLOCKS_X / 2, NUM_BLOCKS_Y / 2));
        direction = KeyEvent.VK_RIGHT;
        generateFood();
        generateObstacles();

        timer = new javax.swing.Timer(120, this); // ✅ No ambiguity
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(20, 20, 20));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (inMenu) return;

                if (key == KeyEvent.VK_SHIFT) sprinting = true;
                if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT ||
                        key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) &&
                        Math.abs(key - direction) != 2) {
                    direction = key;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) sprinting = false;
            }
        });
    }

    private void generateFood() {
        int x, y;
        do {
            x = (int) (Math.random() * NUM_BLOCKS_X);
            y = (int) (Math.random() * NUM_BLOCKS_Y);
        } while (snake.contains(new Point(x, y)));
        food = new Point(x, y);
    }

    private void generateObstacles() {
        obstacles = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) { // 8 random obstacles
            int x = rand.nextInt(NUM_BLOCKS_X);
            int y = rand.nextInt(NUM_BLOCKS_Y);
            Point obstacle = new Point(x, y);
            if (!snake.contains(obstacle) && !obstacle.equals(food)) {
                obstacles.add(obstacle);
            }
        }
    }

    private void moveSnake() {
        Point head = snake.getFirst();
        Point newHead = new Point(head);
        switch (direction) {
            case KeyEvent.VK_LEFT -> newHead.x--;
            case KeyEvent.VK_RIGHT -> newHead.x++;
            case KeyEvent.VK_UP -> newHead.y--;
            case KeyEvent.VK_DOWN -> newHead.y++;
        }

        if (newHead.x < 0 || newHead.x >= NUM_BLOCKS_X ||
                newHead.y < 0 || newHead.y >= NUM_BLOCKS_Y ||
                snake.contains(newHead) ||
                obstacles.contains(newHead)) {
            gameOver = true;
            timer.stop();
            return;
        }

        snake.addFirst(newHead);
        if (newHead.equals(food)) {
            score += 10;
            generateFood();
            if (score % 30 == 0) generateObstacles(); // new obstacles over time
        } else {
            snake.removeLast();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !inMenu) {
            moveSnake();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (inMenu) {
            drawStartMenu(g);
        } else if (gameOver) {
            drawGameOver(g);
        } else {
            drawGame(g);
        }
    }

    private void drawStartMenu(Graphics g) {
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Showcard Gothic", Font.BOLD, 70));
        g.drawString("SNAKE GAME", WIDTH / 5, HEIGHT / 3);

        Font buttonFont = new Font("Arial", Font.BOLD, 30);
        g.setFont(buttonFont);

        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(WIDTH / 3, HEIGHT / 2, 200, 60, 30, 30);
        g.fillRoundRect(WIDTH / 3, HEIGHT / 2 + 100, 200, 60, 30, 30);

        g.setColor(Color.WHITE);
        g.drawString("PLAY", WIDTH / 3 + 70, HEIGHT / 2 + 40);
        g.drawString("QUIT", WIDTH / 3 + 70, HEIGHT / 2 + 140);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mx = e.getX(), my = e.getY();

                if (mx >= WIDTH / 3 && mx <= WIDTH / 3 + 200) {
                    if (my >= HEIGHT / 2 && my <= HEIGHT / 2 + 60) {
                        inMenu = false;
                        timer.start();
                        removeMouseListener(this);
                    } else if (my >= HEIGHT / 2 + 100 && my <= HEIGHT / 2 + 160) {
                        System.exit(0);
                    }
                }
            }
        });
    }

    private void drawGame(Graphics g) {
        // Background
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Border
        g.setColor(Color.ORANGE);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // Obstacles
        for (Point o : obstacles) {
            g.setColor(new Color(120, 60, 20));
            g.fill3DRect(o.x * BLOCK_SIZE, o.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
            g.setColor(new Color(80, 40, 10));
            g.drawRect(o.x * BLOCK_SIZE, o.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        }

        // Snake Body
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) continue;

            GradientPaint gp = new GradientPaint(p.x * BLOCK_SIZE, p.y * BLOCK_SIZE,
                    new Color(0, 180, 0),
                    p.x * BLOCK_SIZE + BLOCK_SIZE, p.y * BLOCK_SIZE + BLOCK_SIZE,
                    new Color(0, 100, 0));
            ((Graphics2D) g).setPaint(gp);
            g.fillRoundRect(p.x * BLOCK_SIZE, p.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, 10, 10);
        }

        // Snake Head
        Point head = snake.getFirst();
        int hx = head.x * BLOCK_SIZE;
        int hy = head.y * BLOCK_SIZE;
        g.setColor(new Color(50, 205, 50));
        g.fillRoundRect(hx, hy, BLOCK_SIZE, BLOCK_SIZE, 10, 10);

        // Eyes
        g.setColor(Color.WHITE);
        g.fillOval(hx + 6, hy + 6, 8, 8);
        g.fillOval(hx + 16, hy + 6, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(hx + 9, hy + 8, 4, 4);
        g.fillOval(hx + 19, hy + 8, 4, 4);

        // Tongue
        g.setColor(Color.RED);
        g.fillRect(hx + 13, hy + BLOCK_SIZE - 6, 4, 8);
        g.fillPolygon(new int[]{hx + 11, hx + 13, hx + 15}, new int[]{hy + BLOCK_SIZE + 2, hy + BLOCK_SIZE + 6, hy + BLOCK_SIZE + 2}, 3);

        // Food (bigger shiny apple)
        int fx = food.x * BLOCK_SIZE;
        int fy = food.y * BLOCK_SIZE;
        g.setColor(Color.RED);
        g.fillOval(fx, fy, BLOCK_SIZE, BLOCK_SIZE);
        g.setColor(Color.GREEN);
        g.fillRect(fx + BLOCK_SIZE / 2 - 2, fy - 5, 4, 6);
        g.setColor(new Color(255, 255, 255, 100));
        g.fillOval(fx + 6, fy + 6, 6, 6);

        // Scoreboard
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRoundRect(10, 10, 150, 40, 20, 20);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("SCORE: " + score, 30, 37);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("GAME OVER", WIDTH / 4, HEIGHT / 2);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        g.drawString("SCORE: " + score, WIDTH / 2 - 50, HEIGHT / 2 + 50);

        g.setColor(Color.ORANGE);
        g.fillRoundRect(WIDTH / 2 - 100, HEIGHT / 2 + 100, 200, 60, 30, 30);
        g.setColor(Color.BLACK);
        g.drawString("RETRY", WIDTH / 2 - 45, HEIGHT / 2 + 140);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                if (mx >= WIDTH / 2 - 100 && mx <= WIDTH / 2 + 100 &&
                        my >= HEIGHT / 2 + 100 && my <= HEIGHT / 2 + 160) {
                    restartGame();
                    removeMouseListener(this);
                }
            }
        });
    }

    private void restartGame() {
        snake.clear();
        snake.add(new Point(NUM_BLOCKS_X / 2, NUM_BLOCKS_Y / 2));
        direction = KeyEvent.VK_RIGHT;
        score = 0;
        gameOver = false;
        generateFood();
        generateObstacles();
        timer.start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("🐍 Snake Game");
        sg game = new sg();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
