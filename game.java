import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class game extends Application {

    private final double WIDTH = 500;
    private final double HEIGHT = 500;
    private ImageView player;
    private List<Rectangle> bullets = new ArrayList<>();
    private List<ImageView> enemies = new ArrayList<>();
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private long lastBulletTime = 0;
    private Random random = new Random();
    private int score = 0;
    private Text scoreText;
    private Text gameOverText;
    private boolean gameOver = false;
    private double enemySpeed = 2.0;
    private Button startButton, retryButton, exitButton;
    private Pane root;
    private AnimationTimer gameLoop;

    @Override
    public void start(Stage stage) {
        root = new Pane();
        root.setPrefSize(WIDTH, HEIGHT);

        Image backgroundImage = new Image("file:resources/pexels-krisof-1252890.jpg");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(WIDTH);
        backgroundView.setFitHeight(HEIGHT);
        root.getChildren().add(backgroundView); 

        setupPlayer();
        setupTextElements();

        startButton = new Button("Start Game");
        startButton.setTranslateX(WIDTH / 2 - 40);
        startButton.setTranslateY(HEIGHT / 2 - 20);
        startButton.setOnAction(_ -> startGame());
        root.getChildren().add(startButton);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Space Shooter with Background");
        stage.show();
        setupKeyListeners(scene);
    }

    private void setupPlayer() {
        Image playerImage = new Image("file:resources/33986.png");
        player = new ImageView(playerImage);
        player.setFitWidth(50);
        player.setFitHeight(50);
        player.setTranslateX(WIDTH / 2 - player.getFitWidth() / 2);
        player.setTranslateY(HEIGHT - 60);
    }

    private void setupTextElements() {
        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font(20));
        scoreText.setFill(Color.BLACK);
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(30);

        gameOverText = new Text("Game Over");
        gameOverText.setFont(Font.font(40));
        gameOverText.setFill(Color.RED);
        gameOverText.setTranslateX(WIDTH / 2 - 100);
        gameOverText.setTranslateY(HEIGHT / 2);
        gameOverText.setVisible(false);
    }

    private void setupKeyListeners(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (!gameOver) {
                if (e.getCode() == KeyCode.LEFT) {
                    moveLeft = true;
                } else if (e.getCode() == KeyCode.RIGHT) {
                    moveRight = true;
                } else if (e.getCode() == KeyCode.SPACE) {
                    shootBullet(root);
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT) {
                moveLeft = false;
            } else if (e.getCode() == KeyCode.RIGHT) {
                moveRight = false;
            }
        });
    }

    private void startGame() {
        root.getChildren().remove(startButton);
        root.getChildren().addAll(player, scoreText);

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver) {
                    movePlayer();
                    moveBullets(root);
                    spawnEnemies(root);
                    moveEnemies();
                    checkCollisions(root);
                }
            }
        };

        gameLoop.start();
    }

    private void movePlayer() {
        if (moveLeft && player.getTranslateX() >= 0) {
            player.setTranslateX(player.getTranslateX() - 5);
        }
        if (moveRight && player.getTranslateX() + player.getFitWidth() <= WIDTH) {
            player.setTranslateX(player.getTranslateX() + 5);
        }
    }

    private void shootBullet(Pane root) {
        if (System.currentTimeMillis() - lastBulletTime > 200) {
            Rectangle bullet = new Rectangle(5, 20, Color.RED);
            bullet.setTranslateX(player.getTranslateX() + player.getFitWidth() / 2 - bullet.getWidth() / 2);
            bullet.setTranslateY(player.getTranslateY() - 20);
            bullets.add(bullet);
            root.getChildren().add(bullet);
            lastBulletTime = System.currentTimeMillis();
        }
    }

    private void moveBullets(Pane root) {
        Iterator<Rectangle> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Rectangle bullet = bulletIterator.next();
            bullet.setTranslateY(bullet.getTranslateY() - 10);
            if (bullet.getTranslateY() < 0) {
                bulletIterator.remove();
                root.getChildren().remove(bullet);
            }
        }
    }

    private void spawnEnemies(Pane root) {
        if (random.nextInt(100) < 2) {
            Image enemyImage = new Image("file:resources/pngwing.com (1).png");
            ImageView enemy = new ImageView(enemyImage);
            enemy.setFitWidth(40);
            enemy.setFitHeight(40);
            enemy.setTranslateX(random.nextInt((int) WIDTH - 40));
            enemy.setTranslateY(0);
            enemies.add(enemy);
            root.getChildren().add(enemy);
        }
    }

    private void moveEnemies() {
        Iterator<ImageView> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            ImageView enemy = enemyIterator.next();
            enemy.setTranslateY(enemy.getTranslateY() + enemySpeed);
            if (enemy.getTranslateY() > HEIGHT) {
                gameOver();
                enemyIterator.remove();
            }
        }
    }

    private void checkCollisions(Pane root) {
        Iterator<Rectangle> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Rectangle bullet = bulletIterator.next();
            Iterator<ImageView> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                ImageView enemy = enemyIterator.next();
                if (bullet.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                    score += 10;
                    scoreText.setText("Score: " + score);
                    if (score % 100 == 0) {
                        enemySpeed += 0.5;
                    }
                    bulletIterator.remove();
                    enemyIterator.remove();
                    root.getChildren().removeAll(bullet, enemy);
                    break;
                }
            }
        }
    }

    private void gameOver() {
        gameOver = true;
        gameLoop.stop();
        gameOverText.setVisible(true);
        root.getChildren().add(gameOverText);
        showEndButtons();
    }

    private void showEndButtons() {
        retryButton = new Button("Retry");
        retryButton.setTranslateX(WIDTH / 2 - 40);
        retryButton.setTranslateY(HEIGHT / 2 + 40);
        retryButton.setOnAction(_ -> resetGame());

        exitButton = new Button("Exit");
        exitButton.setTranslateX(WIDTH / 2 + 40);
        exitButton.setTranslateY(HEIGHT / 2 + 40);
        exitButton.setOnAction(_ -> System.exit(0));

        root.getChildren().addAll(retryButton, exitButton);
    }

    private void resetGame() {
        root.getChildren().clear();

        Image backgroundImage = new Image("file:resources/pexels-krisof-1252890.jpg");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(WIDTH);
        backgroundView.setFitHeight(HEIGHT);
        root.getChildren().add(backgroundView);

        setupPlayer();
        root.getChildren().add(player);
        root.getChildren().add(scoreText);

        bullets.clear();
        enemies.clear();
        score = 0;
        scoreText.setText("Score: 0");
        gameOver = false;
        enemySpeed = 2.0;

        gameLoop.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
