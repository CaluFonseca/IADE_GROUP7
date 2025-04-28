package com.badlogic.UniverseConqueror.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Enemy {
    // Enum representing the different states of the enemy
    private enum State {IDLE, WALK, ATTACK, HURT, DEAD}

    private String name;  // Name of the enemy
    private Vector2 position;  // Position of the enemy on the screen
    private int health;  // Health of the enemy
    private int attackPower;  // Attack power of the enemy
    private float speed;  // Movement speed of the enemy

    private Texture spriteSheet;  // Sprite sheet for the enemy's animations
    private Animation<TextureRegion> idleAnimation;  // Idle animation
    private Animation<TextureRegion> walkAnimation;  // Walking animation
    private Animation<TextureRegion> attackAnimation;  // Attack animation
    private Animation<TextureRegion> hurtAnimation;  // Hurt animation
    private Animation<TextureRegion> deadAnimation;  // Dead animation

    private Animation<TextureRegion> currentAnimation;  // Current animation to play
    private State currentState;  // Current state of the enemy

    private float stateTime;  // Time elapsed since the current animation started
    private Vector2 targetPosition;  // Target position (usually the player)

    private Sound enemySound;  // Sound to play when the enemy is active
    private long soundId;  // ID for the sound loop
    private boolean isDead;  // Indicates if the enemy is dead
    private boolean facingRight = true;  // Whether the enemy is facing right or not

    private boolean wasHurt = false;  // Indicates if the enemy was hurt recently

    private static final int FRAME_WIDTH = 98;  // Width of each frame in the sprite sheet
    private static final int FRAME_HEIGHT = 100;  // Height of each frame in the sprite sheet
    private static final float FRAME_DURATION = 0.3f;  // Duration of each frame in the animation
    public ShapeRenderer shapeRenderer;  // For drawing the health bar
    private boolean attacking = false;  // Whether the enemy is currently attacking

    // Constructor to initialize the enemy with its properties
    public Enemy(String name, float x, float y, int health, int attackPower, float speed) {
        this.name = name;
        this.position = new Vector2(x, y);
        this.health = health;
        this.attackPower = attackPower;
        this.speed = speed;
        this.stateTime = 0f;
        this.soundId = -1;
        this.isDead = false;

        // Load the enemy's sprite sheet
        spriteSheet = new Texture(Gdx.files.internal("assets/enemy/enemy_alien.png"));
        spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Load all the animations
        loadAnimations();

        // Set initial state to IDLE
        setState(State.IDLE);

        // Load enemy sound (e.g., alien arrival sound)
        enemySound = Gdx.audio.newSound(Gdx.files.internal("audio/Classic_alien_arrival.mp3"));
    }

    // Draws the health bar for the enemy above its position
    public void drawHealthBar(SpriteBatch batch) {
        float barWidth = 40f;
        float barHeight = 5f;

        // Calculate health percentage
        float healthPercent = (float) getHealth() / getMaxHealth();
        float barX = getPosition().x - barWidth / 2;
        float barY = getPosition().y + 40f; // Adjusted position based on sprite height

        // Draw the red bar (background of health bar)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Draw the green bar (health remaining)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();
    }

    // Gets the position of the enemy
    public Vector2 getPosition() {
        return position;
    }

    // Loads all the animations for the enemy from the sprite sheet
    private void loadAnimations() {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);

        // Convert the 2D array of texture regions into 1D arrays for each animation
        for (int row = 0; row < tmp.length; row++) {
            for (int col = 0; col < tmp[row].length; col++) {
                tmp[row][col] = new TextureRegion(tmp[row][col]);
            }
        }

        // Set up the animations
        walkAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[0]), Animation.PlayMode.LOOP);
        idleAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[1]), Animation.PlayMode.LOOP);
        attackAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[2]), Animation.PlayMode.NORMAL);
        hurtAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[3]), Animation.PlayMode.NORMAL);
        deadAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[4]), Animation.PlayMode.NORMAL);
    }

    // Checks if the enemy collides with the player's bounds
    public boolean checkCollision(Rectangle playerBounds) {
        return playerBounds.overlaps(getBounds());
    }

    // Sets the current state and animation of the enemy
    private void setState(State state) {
        if (currentState == state) return;

        this.currentState = state;
        this.stateTime = 0f;

        switch (state) {
            case IDLE:
                currentAnimation = idleAnimation;
                break;
            case WALK:
                currentAnimation = walkAnimation;
                break;
            case ATTACK:
                currentAnimation = attackAnimation;
                break;
            case HURT:
                currentAnimation = hurtAnimation;
                break;
            case DEAD:
                currentAnimation = deadAnimation;
                isDead = true;
                break;
        }
    }

    // Updates the enemy's state and position based on time and player position
    public void update(float delta, Vector2 playerPosition, OrthographicCamera camera) {
        if (isDead) {
            stateTime += delta; // Increment state time when dead
            return;
        }

        if (attacking) {
            stateTime += delta;
            if (currentState == State.ATTACK && currentAnimation.isAnimationFinished(stateTime)) {
                setState(State.IDLE); // Return to idle state after attack animation finishes
                attacking = false;
            }
            return;
        }

        // Update the enemy's position based on the player's position
        targetPosition = playerPosition;

        float deltaX = targetPosition.x - position.x;
        float deltaY = targetPosition.y - position.y;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        facingRight = deltaX >= 0; // Determine if the enemy is facing right

        if (distance > 1f) {
            float angle = (float) Math.atan2(deltaY, deltaX); // Calculate angle towards the player
            position.x += Math.cos(angle) * speed * delta; // Move along the x-axis
            position.y += Math.sin(angle) * speed * delta; // Move along the y-axis
            setState(State.WALK); // Set to walking state
        } else {
            setState(State.IDLE); // Set to idle state when close to the player
        }

        // Play sound when the enemy is on screen
        if (isOnScreen(camera) && soundId == -1) {
            soundId = enemySound.loop(); // Start playing sound if the enemy is visible
        } else if (!isOnScreen(camera) && soundId != -1) {
            enemySound.stop(); // Stop sound if the enemy is off-screen
            soundId = -1;
        }

        stateTime += delta; // Increment state time
    }

    // Stops the enemy's sound
    public void stopSound() {
        if (soundId != -1) {
            enemySound.stop();
            soundId = -1;
        }
    }

    // Applies damage to the enemy and changes its state if necessary
    public void takeDamage(int damage) {
        if (isDead) return; // Do nothing if the enemy is already dead

        this.health -= damage;
        wasHurt = true;

        if (this.health <= 0) {
            setState(State.DEAD); // Set the enemy to dead state if health is zero or below
            isDead = true;
            enemySound.stop(); // Stop sound when the enemy dies
            soundId = -1;
        } else {
            setState(State.HURT); // Set the enemy to hurt state if health is above zero
        }
    }

    // Makes the enemy attack the player
    public void attack(Character player) {
        if (isDead || attacking) return; // If the enemy is dead or already attacking, do nothing

        setState(State.ATTACK); // Set the enemy to attack state
        player.takeDamage(attackPower); // Deal damage to the player
        attacking = true; // Set attacking flag to true
    }

    // Renders the enemy on the screen
    public void render(SpriteBatch batch) {
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, !isDead); // Get the current animation frame
        float scale = 0.7f; // Scale factor for the enemy sprite
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        boolean shouldFlip = !facingRight; // Flip the sprite if the enemy is facing left

        if (shouldFlip && !frame.isFlipX()) {
            frame.flip(true, false); // Flip the sprite horizontally
        } else if (!shouldFlip && frame.isFlipX()) {
            frame.flip(true, false); // Flip the sprite back if needed
        }

        // Only render the enemy if it's not dead or if the death animation isn't finished yet
        if (!(isDead && currentAnimation.isAnimationFinished(stateTime))) {
            batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
        }
    }

    // Gets the bounds of the enemy for collision detection
    public Rectangle getBounds() {
        float scale = 0.5f;
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true); // Get the current frame
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;
        return new Rectangle(position.x - width / 2, position.y - height / 2, width, height); // Return the bounding box
    }

    // Checks if the enemy is on the screen
    public boolean isOnScreen(OrthographicCamera camera) {
        Rectangle enemyRect = getBounds(); // Get the enemy's bounding box
        Rectangle cameraRect = new Rectangle(
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight
        );
        return cameraRect.overlaps(enemyRect); // Check if the enemy's bounds overlap with the camera's viewport
    }

    // Check if the enemy was recently hurt
    public boolean wasHurt() {
        return wasHurt;
    }

    // Check if the enemy is dead
    public boolean isDead() {
        return isDead;
    }

    // Get the enemy's current health
    public int getHealth() {
        return health;
    }

    // Get the enemy's max health (for health bar calculations)
    public int getMaxHealth() {
        return 100; // This can be customized if needed
    }

    // Dispose of resources when the enemy is no longer needed
    public void dispose() {
        spriteSheet.dispose();  // Dispose of the sprite sheet texture
    }
}
