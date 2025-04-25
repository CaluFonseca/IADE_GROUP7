package com.badlogic.UniverseConqueror.characters;

import com.badlogic.UniverseConqueror.Attacks.Bullet;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;

public class Character {
    private String name;
    private Vector2 position;
    private int health;
    private int attackPower;
    private int itemsCollected;

    public float stateTime;
    private State currentState;

    private ArrayList<Texture> loadedTextures;
    private boolean animationPaused = false;

    private Sound jumpSound, walkSound, crawlMoveSound, itemPickupSound, hurtSound, deathSound, emptyGunSound, shootSound;

    private boolean animating = true;
    private boolean isWalking = false;
    private boolean isShiftPressed = false;
    private boolean isDead = false;
    private boolean isMoving = false;
    private boolean isJumping = false;

    private boolean facingRight = true;

    private float velocityY;
    private float gravity = -500f;
    private float jumpStrength = 300f;
    private float groundY;

    private float damageCooldown = 1.0f;
    private float timeSinceLastDamage = 0f;
    private long crawlSoundId = -1;

    private Texture crosshairTexture;
    private Vector2 crosshairPosition;

    public enum State {
        IDLE, WALK, CLIMB, CRAWL_MOVE, JUMP, FALL, DEATH, HURT, ATTACK, SUPER_ATTACK
    }

    private Animation<TextureRegion> idleAnimation, walkAnimation, climbAnimation, crawlMoveAnimation, superAttackAnimation;
    private Animation<TextureRegion> jumpAnimation, fallAnimation, deathAnimation, hurtAnimation;
    private Animation<TextureRegion> currentAnimation;
    private Animation<TextureRegion> attackAnimation;
    // escala atual
    private float crosshairScale = 0.03f;
    private float attackTimer = 0;
    private float attackDuration = 0.3f; // duração do ataque em segundos

    public Character(String name, float x, float y, int health, int attackPower) {
        this.name = name;
        this.position = new Vector2(x, y);
        this.health = health;
        this.attackPower = attackPower;
        this.stateTime = 0f;
        this.currentState = State.IDLE;
        this.loadedTextures = new ArrayList<>();
        this.itemsCollected = 0;
        this.groundY = y;

        idleAnimation = loadAnimation("armysoldier/Idle", 2, 0.2f);
        walkAnimation = loadAnimation("armysoldier/Walk", 7, 0.1f);
        climbAnimation = loadAnimation("armysoldier/Climb", 4, 0.1f);
        crawlMoveAnimation = loadAnimation("armysoldier/CrawlMove", 4, 0.1f);
        jumpAnimation = loadAnimation("armysoldier/Jump", 4, 0.15f);
        fallAnimation = loadAnimation("armysoldier/Fall", 1, 0.15f);
        deathAnimation = loadAnimation("armysoldier/Death", 3, 0.15f);
        hurtAnimation = loadAnimation("armysoldier/Hurt", 2, 0.12f);
        attackAnimation = loadAnimation("armysoldier/Attack", 6, 0.1f);
        superAttackAnimation = loadAnimation("armysoldier/SuperAttack", 3, 0.1f);
        currentAnimation = idleAnimation;

        jumpSound = Gdx.audio.newSound(Gdx.files.internal("audio/jump.mp3"));
        walkSound = Gdx.audio.newSound(Gdx.files.internal("audio/walk_on_grass.mp3"));
        crawlMoveSound = Gdx.audio.newSound(Gdx.files.internal("audio/crawl_move_sound.mp3"));
        itemPickupSound = Gdx.audio.newSound(Gdx.files.internal("audio/item_pickup.mp3"));
        hurtSound = Gdx.audio.newSound(Gdx.files.internal("audio/hurt.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("audio/death.mp3"));
        emptyGunSound = Gdx.audio.newSound(Gdx.files.internal("audio/empty_gun.mp3"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/laser_shoot.mp3"));

        crosshairTexture = new Texture(Gdx.files.internal("crosshair.png"));
        crosshairPosition = new Vector2();
    }

    private Animation<TextureRegion> loadAnimation(String basePath, int frameCount, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            String path = basePath + String.format("%04d.png", i);
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                Texture tex = new Texture(file);
                loadedTextures.add(tex);
                frames.add(new TextureRegion(tex));
            } else {
                Gdx.app.error("Character", "Frame não encontrado: " + path);
            }
        }
        if (frames.isEmpty()) {
            Gdx.app.error("Character", "Nenhuma animação carregada para: " + basePath);
            return null;
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        // Definindo o PlayMode para a animação
        if (basePath.contains("Death") || basePath.contains("Hurt")) {
            anim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }        return anim;
    }

    public void update(float delta, OrthographicCamera camera) {
        if (!animationPaused) {
            stateTime += delta;
            timeSinceLastDamage += delta;
        }

        if (currentState == State.ATTACK) {
            attackTimer -= delta;
            if (attackTimer <= 0) {
                setState(State.IDLE); // volta ao estado normal
                setAnimating(false);
            }
        }

        // converte screen → world
        Vector3 screen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(screen);
        crosshairPosition.set(screen.x, screen.y);

        if ((currentState == State.HURT || currentState == State.ATTACK)
            && currentAnimation.isAnimationFinished(stateTime) && health > 0) {
            setState(isMoving ? State.WALK : State.IDLE);
        }

        if (currentState == State.WALK && isMoving) {
            if (!isWalking) {
                walkSound.loop();
                isWalking = true;
            }
        } else {
            if (isWalking) {
                walkSound.stop();
                isWalking = false;
            }
        }

        if (currentState == State.CRAWL_MOVE && isMoving && isShiftPressed && crawlSoundId == -1) {
            crawlSoundId = crawlMoveSound.loop();
        } else if (crawlSoundId != -1 && (!isMoving || !isShiftPressed)) {
            crawlMoveSound.stop(crawlSoundId);
            crawlSoundId = -1;
        }

        if (currentState == State.SUPER_ATTACK && currentAnimation.isAnimationFinished(stateTime)) {
            setState(State.IDLE); // Or set it to another state like WALK if moving
        }

        if (isJumping) {
            velocityY += gravity * delta;
            position.y += velocityY * delta;

            if (velocityY > 0) setState(State.JUMP);
            else if (velocityY < 0) setState(State.FALL);

            if (position.y <= groundY) {
                position.y = groundY;
                isJumping = false;
                velocityY = 0;
                setState(State.IDLE);
            }
        }

        switch (currentState) {
            case IDLE:
                currentAnimation = idleAnimation;
                break;
            case WALK:
                currentAnimation = walkAnimation;
                break;
            case CLIMB:
                currentAnimation = climbAnimation;
                break;
            case CRAWL_MOVE:
                currentAnimation = crawlMoveAnimation;
                break;
            case JUMP:
                currentAnimation = jumpAnimation;
                break;
            case FALL:
                currentAnimation = fallAnimation;
                break;
            case DEATH:
                currentAnimation = deathAnimation;
                break;
            case HURT:
                currentAnimation = hurtAnimation;
                break;
            case ATTACK:
                currentAnimation = attackAnimation;
                break;
            case SUPER_ATTACK:
                currentAnimation = superAttackAnimation;
                break;
        }
    }

    public void render(SpriteBatch batch) {
        if (currentAnimation == null) return;

        TextureRegion frame = animating
            ? currentAnimation.getKeyFrame(stateTime, currentAnimation.getPlayMode() == Animation.PlayMode.LOOP)
            : currentAnimation.getKeyFrame(0f);

        if ((facingRight && frame.isFlipX()) || (!facingRight && !frame.isFlipX())) {
            frame.flip(true, false);
        }

        batch.draw(frame, position.x, position.y,
            frame.getRegionWidth() * 0.4f,
            frame.getRegionHeight() * 0.4f);

        float cw = crosshairTexture.getWidth() * crosshairScale;
        float ch = crosshairTexture.getHeight() * crosshairScale;
        batch.draw(crosshairTexture,
            crosshairPosition.x - cw * 0.5f,
            crosshairPosition.y - ch * 0.5f,
            cw, ch);
    }

    public void updatePosition(float deltaX, float deltaY, boolean shiftPressed) {
        if ((currentState == State.ATTACK || currentState == State.SUPER_ATTACK) && !attackAnimation.isAnimationFinished(stateTime)) {
            // Bloqueia a mudança de estado enquanto a animação de ataque não terminou
            return;
        }

        isShiftPressed = shiftPressed;
        isMoving = deltaX != 0 || deltaY != 0;

        if (deltaX > 0) facingRight = true;
        else if (deltaX < 0) facingRight = false;

        if (!isDead && !isJumping) {
            if (isMoving && shiftPressed) setState(State.CRAWL_MOVE);
            else if (isMoving) setState(State.WALK);
            else setState(State.IDLE);

            position.add(deltaX, deltaY);
        }
    }

    public void jump() {
        if (!isJumping && !isDead) {
            velocityY = jumpStrength;
            isJumping = true;
            setState(State.JUMP);
            jumpSound.play();
            groundY = position.y;
        }
    }

    public void takeDamage(int damage) {
        if (timeSinceLastDamage < damageCooldown || isDead) return;

        health -= damage;
        timeSinceLastDamage = 0f;

        if (damage > 0 && health > 0) {
            hurtSound.play();
            setState(State.HURT);
            stateTime = 0f;
        }

        if (health <= 0) {
            die();
        }
    }

    public void die() {
        isDead = true;
        deathSound.play();
        setState(State.DEATH);
        setAnimating(true);
    }

    public void collectItem(String itemName) {
        itemsCollected++;
        itemPickupSound.play();
        if (itemName.equalsIgnoreCase("Vida")) {
            health = Math.min(health + 5, 100);
        } else if (itemName.equalsIgnoreCase("Ataque")) {
            setAddAttackPower(); // Adiciona 1 ponto de ataque
        }
    }

    public void dispose() {
        for (Texture t : loadedTextures) t.dispose();
        jumpSound.dispose();
        walkSound.dispose();
        crawlMoveSound.dispose();
        itemPickupSound.dispose();
        hurtSound.dispose();
        deathSound.dispose();
        crosshairTexture.dispose();
        emptyGunSound.dispose();

    }

    public void setState(State newState) {
        if (currentState == State.DEATH || (currentState == State.HURT && !hurtAnimation.isAnimationFinished(stateTime))) {
            return;
        }

        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;
            animationPaused = false;
        }
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    public State getState() {
        return currentState;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getHealth() {
        return health;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower() {
        if (attackPower > 0) {
            attackPower--;
        }
    }

    public void setAddAttackPower() {
        attackPower++;
    }

    public int getItemsCollected() {
        return itemsCollected;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isAnimationFinished() {
        return currentAnimation != null && currentAnimation.isAnimationFinished(stateTime);
    }

    public boolean isDeathAnimationFinished() {
        return currentState == State.DEATH && stateTime >= deathAnimation.getAnimationDuration();
    }

    public Rectangle getBounds() {
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime);
        return new Rectangle(position.x, position.y,
            frame.getRegionWidth() * 0.4f,
            frame.getRegionHeight() * 0.4f);
    }

    public Bullet shootBullet() {
        if (attackPower > 1) {
            if (shootSound == null) {
                shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/laser_shoot.mp3"));
            }
            shootSound.play(0.4f);
            facingRight = crosshairPosition.x > position.x;

            float scale = 0.4f;
            TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime);
            float frameWidth = currentFrame.getRegionWidth() * scale;
            float frameHeight = currentFrame.getRegionHeight() * scale;

            // Calcula o ponto de origem no centro aproximado da personagem
            float originX = position.x + frameWidth / 2f;
            float originY = position.y + frameHeight / 2f;

            // Offset da bala em relação ao centro do corpo (ajustar conforme necessário)
            float offsetX = facingRight ? 50 : -50;
            float offsetY = 0;

            float startX = originX + offsetX;
            float startY = originY + offsetY;

            return new Bullet(startX, startY, new Vector2(crosshairPosition));
        } else {
            emptyGunSound.play();
            return new Bullet(-1, -1, new Vector2(0, 0));
        }
    }

    public void resetAttackTimer() {
        attackTimer = attackDuration;
    }
}
