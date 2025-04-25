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
    private enum State {IDLE, WALK, ATTACK, HURT, DEAD}

    private String name;
    private Vector2 position;
    private int health;
    private int attackPower;
    private float speed;

    private Texture spriteSheet;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> hurtAnimation;
    private Animation<TextureRegion> deadAnimation;

    private Animation<TextureRegion> currentAnimation;
    private State currentState;

    private float stateTime;
    private Vector2 targetPosition;

    private Sound enemySound;
    private long soundId;
    private boolean isDead;
    private boolean facingRight = true;

    private boolean wasHurt = false;

    private static final int FRAME_WIDTH = 98;
    private static final int FRAME_HEIGHT = 100;
    private static final float FRAME_DURATION = 0.3f;
    public ShapeRenderer shapeRenderer;
    private boolean attacking = false;

    public Enemy(String name, float x, float y, int health, int attackPower, float speed) {
        this.name = name;
        this.position = new Vector2(x, y);
        this.health = health;
        this.attackPower = attackPower;
        this.speed = speed;
        this.stateTime = 0f;
        this.soundId = -1;
        this.isDead = false;

        spriteSheet = new Texture(Gdx.files.internal("assets/enemy/enemy_alien.png"));
        spriteSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        loadAnimations();
        setState(State.IDLE);
        enemySound = Gdx.audio.newSound(Gdx.files.internal("audio/Classic_alien_arrival.mp3"));
    }

    public void drawHealthBar(SpriteBatch batch) {
        float barWidth = 40f;
        float barHeight = 5f;

        float healthPercent = (float) getHealth() / getMaxHealth();
        float barX = getPosition().x - barWidth / 2;
        float barY = getPosition().y + 40f; // Ajusta conforme a altura do sprite

        // Barra vermelha (fundo)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Barra verde (vida restante)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();
    }

    public Vector2 getPosition() {
        return position;
    }

    private void loadAnimations() {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, FRAME_WIDTH, FRAME_HEIGHT);

        for (int row = 0; row < tmp.length; row++) {
            for (int col = 0; col < tmp[row].length; col++) {
                tmp[row][col] = new TextureRegion(tmp[row][col]);
            }
        }

        walkAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[0]), Animation.PlayMode.LOOP);
        idleAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[1]), Animation.PlayMode.LOOP);
        attackAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[2]), Animation.PlayMode.NORMAL);
        hurtAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[3]), Animation.PlayMode.NORMAL);
        deadAnimation = new Animation<>(FRAME_DURATION, new Array<>(tmp[4]), Animation.PlayMode.NORMAL);
    }

    public boolean checkCollision(Rectangle playerBounds) {
        return playerBounds.overlaps(getBounds());
    }

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

    public void update(float delta, Vector2 playerPosition, OrthographicCamera camera) {
        if (isDead) {
            stateTime += delta;
            return;
        }

        if (attacking) {
            stateTime += delta;
            if (currentState == State.ATTACK && currentAnimation.isAnimationFinished(stateTime)) {
                setState(State.IDLE);
                attacking = false;
            }
            return;
        }

        targetPosition = playerPosition;

        float deltaX = targetPosition.x - position.x;
        float deltaY = targetPosition.y - position.y;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        facingRight = deltaX >= 0;

        if (distance > 1f) {
            float angle = (float) Math.atan2(deltaY, deltaX);
            position.x += Math.cos(angle) * speed * delta;
            position.y += Math.sin(angle) * speed * delta;
            setState(State.WALK);
        } else {
            setState(State.IDLE);
        }

        if (isOnScreen(camera) && soundId == -1) {
            soundId = enemySound.loop();
        } else if (!isOnScreen(camera) && soundId != -1) {
            enemySound.stop();
            soundId = -1;
        }

        stateTime += delta;
    }

    public void stopSound() {
        if (soundId != -1) {
            enemySound.stop();
            soundId = -1;
        }
    }

    public void takeDamage(int damage) {
        if (isDead) return;

        this.health -= damage;
        wasHurt = true;

        if (this.health <= 0) {
            setState(State.DEAD);
            isDead = true;
            enemySound.stop();
            soundId = -1;
        } else {
            setState(State.HURT);
        }
    }

    public void attack(Character player) {
        if (isDead || attacking) return;

        setState(State.ATTACK);
        player.takeDamage(attackPower);
        attacking = true;
    }

    public void render(SpriteBatch batch) {

        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, !isDead);
        float scale = 0.7f;
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        boolean shouldFlip = !facingRight;

        if (shouldFlip && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (!shouldFlip && frame.isFlipX()) {
            frame.flip(true, false);
        }

        if (!(isDead && currentAnimation.isAnimationFinished(stateTime))) {
            batch.draw(frame, position.x - width / 2, position.y - height / 2, width, height);
        }
    }

    public Rectangle getBounds() {
        float scale = 0.5f;
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime, true);
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;
        return new Rectangle(position.x - width / 2, position.y - height / 2, width, height);
    }

    public boolean isOnScreen(OrthographicCamera camera) {
        Rectangle enemyRect = getBounds();
        Rectangle cameraRect = new Rectangle(
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight
        );
        return cameraRect.overlaps(enemyRect);
    }

    public boolean wasHurt() {
        return wasHurt;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return 100;
    }
}
