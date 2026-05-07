package io.github.found1;

// ============================================================
// 🎮 GAME SCREEN — Your platformer lives here
// ============================================================
//
// Day 1 (Apr 27): Get the player moving, jumping, and landing
// Day 2 (Apr 29): Add sprite sheet animations
// Day 3 (May 1):  Add enemies and collision
// Day 4 (May 5):  Add coins and platforms
// Day 5 (May 7):  Add MenuScreen and GameOverScreen
// Day 6 (May 11): Add HUD, sound, polish
// Day 7 (May 13): Final polish and submit
//
// ============================================================

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {

    private final Main game;

    // ── Constants ──
    private static final float GRAVITY = -500f;
    private static final float JUMP_VELOCITY = 300f;
    private static final float MOVE_SPEED = 150f;
    private static final float GROUND_Y = 50f;
    private static final int START_LIVES = 3;
    private static final float SPAWN_X = 50f;
    private static final float SPAWN_Y = 80f;

    // ── Rendering ──
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture playerSheet, enemySheet, coinSheet, pixel;
    private Animation<TextureRegion> slimeAnim, coinAnim;
    private Animation<TextureRegion> idleAnim, runAnim, jumpAnim;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private ArrayList<float[]> enemies;
    private ArrayList<Rectangle> coins;
    private Rectangle playerBounds;
    private int score = 0;
    private ArrayList<Rectangle> platforms;
    private int lives = START_LIVES;
    private boolean switchToGameOver = false;
    private boolean playerWon = false;
    private BitmapFont hudFont;

    // ── Player ──
    private float playerX = 100f;
    private float playerY = GROUND_Y;
    private float velocityY = 0f;
    private boolean onGround = true;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);

        // For Day 1, just load the full sprite sheet as a single texture.
        // On Day 2 you'll split it into animations.
        playerSheet = new Texture("player.png");
        pixel = new Texture("white.png");

        TextureRegion[][] grid = TextureRegion.split(playerSheet, 64, 64);

        hudFont = new BitmapFont();
        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.5f);

        idleAnim = new Animation<>(0.2f, grid[0]);
        runAnim = new Animation<>(0.1f, grid[1]);
        jumpAnim = new Animation<>(0.15f, grid[2]);

        idleAnim.setPlayMode(Animation.PlayMode.LOOP);
        runAnim.setPlayMode(Animation.PlayMode.LOOP);
        jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);

        enemySheet = new Texture("enemy-slime.png");
        TextureRegion[][] eGrid = TextureRegion.split(enemySheet, 64, 64);
        slimeAnim = new Animation<>(0.15f, eGrid[0]);
        slimeAnim.setPlayMode(Animation.PlayMode.LOOP);

        coinSheet = new Texture("coin.png");
        TextureRegion[][] cGrid = TextureRegion.split(coinSheet, 32, 32);
        coinAnim = new Animation<>(0.08f, cGrid[0]);
        coinAnim.setPlayMode(Animation.PlayMode.LOOP);

        playerBounds = new Rectangle(playerX, playerY, 64, 64);

        enemies = new ArrayList<>();
        enemies.add(new float[]{250, GROUND_Y, 80, 200, 350});
        enemies.add(new float[]{250, GROUND_Y, 60, 400, 550});

        coins = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            coins.add(new Rectangle(150 + i * 70, 200, 32, 32));
        }

        // DAY 3 TODO 5: Create the player bounding box:
        //   playerBounds = new Rectangle(playerX, playerY, 64, 64);
        //
        //   DAY 6 TODO 3: Later, change this to tighter bounds:
        //     playerBounds = new Rectangle(0, 0, 28, 48);


        // DAY 4 TODO 4: Create platforms list and add platforms:
        //   platforms = new ArrayList<>();
        //   platforms.add(new Rectangle(0, 30, W, 20));           // ground
        //   platforms.add(new Rectangle(100, 130, 120, 16));      // lower
        //   platforms.add(new Rectangle(300, 130, 120, 16));
        //   platforms.add(new Rectangle(500, 130, 120, 16));
        //   platforms.add(new Rectangle(50, 230, 140, 16));       // mid
        //   platforms.add(new Rectangle(250, 260, 160, 16));
        //   platforms.add(new Rectangle(470, 230, 130, 16));
        //   platforms.add(new Rectangle(150, 360, 130, 16));      // high
        //   platforms.add(new Rectangle(380, 390, 140, 16));


        // DAY 3 TODO 6: Create enemies ArrayList and add 2 enemies:
        //   enemies = new ArrayList<>();
        //   enemies.add(new float[]{250, GROUND_Y, 80, 200, 350});
        //   enemies.add(new float[]{450, GROUND_Y, 60, 400, 550});
        //
        //   DAY 4 TODO 5: Later, update enemies to patrol on platforms:
        //     enemies = new ArrayList<>();
        //     enemies.add(new float[]{200, 50, 70, 100, 350});      // ground
        //     enemies.add(new float[]{310, 146, 50, 300, 400});     // lower platform
        //     enemies.add(new float[]{260, 276, -45, 250, 390});    // mid platform


        // DAY 3 TODO 7: Create coins ArrayList and add 5 coins:
        //   coins = new ArrayList<>();
        //   for (int i = 0; i < 5; i++) {
        //       coins.add(new Rectangle(150 + i * 70, 200, 32, 32));
        //   }
        //
        //   DAY 4 TODO 6: Later, update coins to sit on platforms:
        //     coins = new ArrayList<>();
        //     coins.add(new Rectangle(60, 70, 32, 32));         // ground
        //     coins.add(new Rectangle(440, 70, 32, 32));
        //     coins.add(new Rectangle(140, 160, 32, 32));       // lower
        //     coins.add(new Rectangle(540, 160, 32, 32));
        //     coins.add(new Rectangle(80, 260, 32, 32));        // mid
        //     coins.add(new Rectangle(310, 290, 32, 32));
        //     coins.add(new Rectangle(510, 260, 32, 32));
        //     coins.add(new Rectangle(190, 390, 32, 32));       // high
        //     coins.add(new Rectangle(430, 420, 32, 32));


        // DAY 5 TODO 3: Set spawn position:
        //   playerX = SPAWN_X;
        //   playerY = SPAWN_Y;

    }

    private void updateEnemies(float delta) {
        for(var enemy : enemies) {
            enemy[0] += enemy[2] * delta;
            if (enemy[0] <= enemy[3]) {
                enemy[0] = enemy[3];
                enemy[2] = -enemy[2];
            }
            if (enemy[0] >= enemy[4]){
                enemy[0] = enemy[4];
                enemy[2] = -enemy[2];
            }
        }
    }

    private void checkCollisions() {
        playerBounds.setPosition(playerX, playerY);

        for (float[] enemy : enemies) {
            Rectangle enemyRect = new Rectangle(enemy[0], enemy[1], 64, 64);
            if (playerBounds.overlaps(enemyRect)) {
                playerX = 100;
                playerY = GROUND_Y;
                velocityY = 0;
                System.out.println("Hit: Resetting player.");
            }
        }

        Iterator<Rectangle> it = coins.iterator();
               while (it.hasNext()) {
                   Rectangle coin = it.next();
                   if (playerBounds.overlaps(coin)) {
                       it.remove();
                       score++;
                       System.out.println("Coin! Score: " + score);
                   }
               }
    }

    @Override
    public void render(float delta) {

        // ── INPUT ──
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= MOVE_SPEED * delta;
            facingRight = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += MOVE_SPEED * delta;
            facingRight = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.W) && onGround) {
            velocityY = JUMP_VELOCITY;
            onGround = false;
        }

        // ── PHYSICS ──
        velocityY += GRAVITY * delta;
        playerY += velocityY * delta;
        if (playerY <= GROUND_Y) {
            playerY = GROUND_Y;
            velocityY = 0.0f;
            onGround = true;
        }

        // ── UPDATES ──

        updateEnemies(delta);
        checkCollisions();

        // ── ANIMATION ──

        stateTime += delta;

        Animation<TextureRegion> currentAnim;
        if (!onGround) {
            currentAnim = jumpAnim;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            currentAnim = runAnim;
        } else {
            currentAnim = idleAnim;
        }

        boolean looping = onGround;
        TextureRegion frame = currentAnim.getKeyFrame(stateTime, looping);

        if (!facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        }


        // ── DRAW ──
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(frame, playerX, playerY, 64, 64);

        TextureRegion slimeFrame = slimeAnim.getKeyFrame(stateTime, true);
        for (float[] enemy : enemies) {
            batch.draw(slimeFrame, enemy[0], enemy[1]);
        }

        TextureRegion coinFrame = coinAnim.getKeyFrame(stateTime, true);
        for (Rectangle coin : coins) {
            batch.draw(coinFrame, coin.x, coin.y);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerSheet.dispose();
        enemySheet.dispose();
        coinSheet.dispose();
    }
}
