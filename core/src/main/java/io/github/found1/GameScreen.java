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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameScreen implements Screen {

    private final Main game;

    // ── Constants ──
    private static final float GRAVITY = -500f;
    private static final float JUMP_VELOCITY = 300f;
    private static final float MOVE_SPEED = 150f;
    private static final float GROUND_Y = 50f;

    // ── Rendering ──
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture playerTexture;

    // ── Animation ──
    private Animation<TextureRegion> idleAnim, runAnim, jumpAnim;
    private float stateTime = 0f;
    boolean facingRight = true;

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
        playerTexture = new Texture("player.png");

        TextureRegion[][] grid = TextureRegion.split(playerTexture, 32, 32);

        idleAnim = new Animation<>(0.2f, grid[0]);
        runAnim = new Animation<>(0.1f, grid[1]);
        jumpAnim = new Animation<>(0.15f, grid[2]);

        idleAnim.setPlayMode(Animation.PlayMode.LOOP);
        runAnim.setPlayMode(Animation.PlayMode.LOOP);
        jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);
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
        batch.draw(playerTexture, playerX, playerY, 64, 64);
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
        playerTexture.dispose();
    }
}
