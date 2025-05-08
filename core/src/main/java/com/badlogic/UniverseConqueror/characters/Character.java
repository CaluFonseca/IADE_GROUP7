package com.badlogic.UniverseConqueror.characters;

// Import necessary LibGDX and project classes
import com.badlogic.UniverseConqueror.Attacks.Bullet;
import com.badlogic.UniverseConqueror.Attacks.Fireball;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;

// Character class represents the player
public class Character {
    // Basic attributes
    private String name;
    private Vector2 position;
    private Vector2 velocity;
    private int health;
    private int attackPower;
    private int itemsCollected;

    // Animation control variables
    public float stateTime;
    private State currentState;
    private ArrayList<Texture> loadedTextures;
    private boolean animationPaused = false;

    //Sounds
    private Sound jumpSound, walkSound, fastMoveSound, itemPickupSound, hurtSound, deathSound, emptyGunSound, shootSound, fireBallSound;

    // Movement and state flags
    private boolean animating = true;
    private boolean isDead = false;
    private boolean isMoving = false;
    private boolean isJumping = false;
    private boolean facingRight = true;

    // Physics variables
    private float velocityY;
    private float gravity = -500f;
    private float jumpStrength = 300f;
    private float groundY;

    // Damage control
    private float damageCooldown = 1.0f;
    private float timeSinceLastDamage = 0f;
    private long fastSoundId = -1;
    private long walkSoundId = -1;

    // Crosshair variables
    private Texture crosshairTexture;
    private Vector2 crosshairPosition;
    private float crosshairScale = 0.03f;
    private float crosshairWidth, crosshairHeight;

    // Speed control
    private float baseSpeed = 100f;
    private float fastSpeedMultiplier = 3f;

    // State enumeration
    public enum State {
        IDLE, WALK, CLIMB, FAST_MOVE, JUMP, FALL, DEATH, HURT, ATTACK, SUPER_ATTACK, DEFENSE, WALK_INJURED, IDLE_INJURED, DEFENSE_INJURED
    }

    // Animations for different states
    private Animation<TextureRegion> idleAnimation, walkAnimation, climbAnimation, fastMoveAnimation, superAttackAnimation, defenseInjuredAnimation;
    private Animation<TextureRegion> jumpAnimation, fallAnimation, deathAnimation, hurtAnimation, defenseAnimation, walkInjuredAnimation, idleInjuredAnimation;
    private Animation<TextureRegion> currentAnimation;
    private Animation<TextureRegion> attackAnimation;

    private float attackTimer = 0;
    private float attackDuration = 0.3f;

    // Character class represents the player or an enemy character
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
        this.velocity = new Vector2(0, 0);

        idleAnimation = loadAnimation("armysoldier/Idle", 2, 0.2f);
        walkAnimation = loadAnimation("armysoldier/Walk", 7, 0.1f);
        climbAnimation = loadAnimation("armysoldier/Climb", 4, 0.1f);
        fastMoveAnimation = loadAnimation("armysoldier/fastMove", 2, 0.1f);
        jumpAnimation = loadAnimation("armysoldier/Jump", 4, 0.15f);
        fallAnimation = loadAnimation("armysoldier/Fall", 1, 0.15f);
        deathAnimation = loadAnimation("armysoldier/Death", 3, 0.15f);
        hurtAnimation = loadAnimation("armysoldier/Hurt", 2, 0.12f);
        attackAnimation = loadAnimation("armysoldier/Attack", 6, 0.1f);
        superAttackAnimation = loadAnimation("armysoldier/SuperAttack", 3, 0.1f);
        defenseAnimation = loadAnimation("armysoldier/Defense", 4, 0.1f);
        walkInjuredAnimation = loadAnimation("armysoldier/WalkInjured", 5, 0.1f);
        idleInjuredAnimation = loadAnimation("armysoldier/IdleInjured", 2, 0.2f);
        defenseInjuredAnimation = loadAnimation("armysoldier/DefenseInjured", 4, 0.1f);
        currentAnimation = idleAnimation;

        jumpSound = Gdx.audio.newSound(Gdx.files.internal("audio/jump.mp3"));
        walkSound = Gdx.audio.newSound(Gdx.files.internal("audio/walk_on_grass.mp3"));
        fastMoveSound = Gdx.audio.newSound(Gdx.files.internal("audio/flight.mp3"));
        itemPickupSound = Gdx.audio.newSound(Gdx.files.internal("audio/item_pickup.mp3"));
        hurtSound = Gdx.audio.newSound(Gdx.files.internal("audio/hurt.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("audio/death.mp3"));
        emptyGunSound = Gdx.audio.newSound(Gdx.files.internal("audio/empty_gun.mp3"));
        shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/laser_shoot.mp3"));
        fireBallSound = Gdx.audio.newSound(Gdx.files.internal("audio/fireball.mp3"));

        crosshairTexture = new Texture(Gdx.files.internal("crosshair.png"));
        crosshairPosition = new Vector2();
        crosshairWidth = crosshairTexture.getWidth() * crosshairScale;
        crosshairHeight = crosshairTexture.getHeight() * crosshairScale;
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
            }
        }
        if (frames.isEmpty()) {
            return null;
        }

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        if (basePath.contains("Death") || basePath.contains("Hurt") || basePath.contains("SuperAttack") || basePath.contains("fastMove") || basePath.contains("Defense") || basePath.contains("DefenseInjured")) {
            anim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }
        return anim;
    }

    //StopSound
    private void stopSound(Sound sound, long id) {
        if (id != -1) {
            sound.stop(id);
        }
    }

    //setIdleBasedOnHealth
    private void setIdleBasedOnHealth() {
        setState(health < 25 ? State.IDLE_INJURED : State.IDLE);
    }

    //setWalkBasedOnHealth
    private void setWalkBasedOnHealth() {
        setState(health < 25 ? State.WALK_INJURED : State.WALK);
    }

    // Updates the character state, animation, and physics

    public void update(float delta, OrthographicCamera camera) {
    if (!animationPaused) {
        stateTime += delta;
        timeSinceLastDamage += delta;
    }

    if (currentState == State.ATTACK) {
        attackTimer -= delta;
        if (attackTimer <= 0) {
            setIdleBasedOnHealth();
            setAnimating(false);
        }
    }

    if (currentState == State.SUPER_ATTACK) {
        if (currentAnimation.isAnimationFinished(stateTime)) {
            // Stay in SUPER_ATTACK state until player acts
        }
    }

    Vector3 screen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(screen);
    crosshairPosition.set(screen.x, screen.y);

    if (currentState == State.HURT || currentState == State.ATTACK) {
        if (currentAnimation.isAnimationFinished(stateTime) && health > 0) {
            if (isMoving) {
                setWalkBasedOnHealth();
            } else {
                setIdleBasedOnHealth();
            }
        }
    }

    if (isJumping) {
        velocity.y += gravity * delta;
        position.y += velocity.y * delta;

        if (health > 25) {
            if (velocity.y > 0) setState(State.JUMP);
            else if (velocity.y < 0) setState(State.FALL);
        }

        if (position.y <= groundY) {
            position.y = groundY;
            isJumping = false;
            velocity.y = 0;
            setIdleBasedOnHealth();
        }


    }

    switch (currentState) {
        case IDLE: currentAnimation = idleAnimation; break;
        case WALK: currentAnimation = walkAnimation; break;
        case CLIMB: currentAnimation = climbAnimation; break;
        case FAST_MOVE: currentAnimation = fastMoveAnimation; break;
        case JUMP: currentAnimation = jumpAnimation; break;
        case FALL: currentAnimation = fallAnimation; break;
        case DEATH: currentAnimation = deathAnimation; break;
        case HURT: currentAnimation = hurtAnimation; break;
        case ATTACK: currentAnimation = attackAnimation; break;
        case SUPER_ATTACK: currentAnimation = superAttackAnimation; break;
        case DEFENSE: currentAnimation = defenseAnimation; break;
        case WALK_INJURED: currentAnimation = walkInjuredAnimation; break;
        case IDLE_INJURED: currentAnimation = idleInjuredAnimation; break;
        case DEFENSE_INJURED: currentAnimation = defenseInjuredAnimation; break;
    }
}

    // Render the character and crosshair
    public void render(SpriteBatch batch) {
        if (currentAnimation == null) return;

        TextureRegion frame = animating
            ? currentAnimation.getKeyFrame(stateTime, currentAnimation.getPlayMode() == Animation.PlayMode.LOOP)
            : currentAnimation.getKeyFrame(0f);

        float drawWidth = frame.getRegionWidth() * 0.4f;
        float drawHeight = frame.getRegionHeight() * 0.4f;

        batch.draw(frame,
            facingRight ? position.x : position.x + drawWidth,
            position.y,
            facingRight ? drawWidth : -drawWidth,
            drawHeight);

        batch.draw(crosshairTexture,
            crosshairPosition.x - crosshairWidth * 0.5f,
            crosshairPosition.y - crosshairHeight * 0.5f,
            crosshairWidth, crosshairHeight);
    }

    // Update character position based on input
    public void updatePosition(float deltaX, float deltaY, boolean shiftPressed) {
        if ((currentState == State.ATTACK || currentState == State.SUPER_ATTACK) && !attackAnimation.isAnimationFinished(stateTime)) return;

        isMoving = deltaX != 0 || deltaY != 0;

        if (!isDead && !isJumping) {
            float adjustedSpeed = baseSpeed;
            if (movementLocked && !isJumping) {
                deltaX = 0;
                deltaY = 0;
            }

            if (isMoving) {
                if (!shiftPressed) {
                    setWalkBasedOnHealth();
                    if (walkSoundId == -1) walkSoundId = walkSound.loop(0.5f);
                    if (fastSoundId != -1) {
                        stopSound(fastMoveSound, fastSoundId);
                        fastSoundId = -1;
                    }
                } else {
                    if (health > 25) {
                        setState(State.FAST_MOVE);
                        adjustedSpeed *= fastSpeedMultiplier;
                        if (fastSoundId == -1) fastSoundId = fastMoveSound.loop(1.0f);
                        if (walkSoundId != -1) {
                            stopSound(walkSound, walkSoundId);
                            walkSoundId = -1;
                        }
                    }
                }
            } else {
                if (walkSoundId != -1) {
                    stopSound(walkSound, walkSoundId);
                    walkSoundId = -1;
                }
                if (fastSoundId != -1) {
                    stopSound(fastMoveSound, fastSoundId);
                    fastSoundId = -1;
                }
                setIdleBasedOnHealth();
            }

            if (deltaX > 0) facingRight = true;
            else if (deltaX < 0) facingRight = false;

            // Define apenas a velocity, sem mover o player diretamente
            Vector2 move = new Vector2(deltaX, deltaY);
            if (move.len() > 0) {
                move.nor().scl(adjustedSpeed);
                velocity.set(move); // velocidade em unidades/segundo
            } else {
                velocity.setZero();
            }
        }
    }


    // Make the character jump
    public void jump() {
        if (!isJumping && !isDead && health > 25) {
            velocity.y = jumpStrength;
            isJumping = true;
            unlockMovement();

            if (facingRight) velocity.x = baseSpeed * 0.6f;
            else velocity.x = -baseSpeed * 0.6f;

            System.out.println("Saltou. Movimento desbloqueado.");
            setState(State.JUMP);
            jumpSound.play();

            // Só atualiza groundY se estiver no chão (para não bugar quando no ar)
            if (Math.abs(position.y - groundY) < 10) {
                groundY = position.y;
            }
        }
    }

    // Deal damage to the character
    public void takeDamage(int damage) {
        if (timeSinceLastDamage < damageCooldown || isDead || currentState == State.DEFENSE || currentState == State.DEFENSE_INJURED) return;

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

    // Handle character death
    public void die() {
        isDead = true;
        deathSound.play();
        setState(State.DEATH);
        setAnimating(true);
    }

    // Shoot a bullet towards the crosshair
    public Bullet shootBullet() {
        if (attackPower > 1) {
            shootSound.play(0.4f);
            facingRight = crosshairPosition.x > position.x;

            float scale = 0.4f;
            TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime);
            float frameWidth = currentFrame.getRegionWidth() * scale;
            float frameHeight = currentFrame.getRegionHeight() * scale;

            float originX = position.x + frameWidth / 2f;
            float originY = position.y + frameHeight / 2f;
            float offsetX = facingRight ? 50 : -50;
            float offsetY = 0;

            return new Bullet(originX + offsetX, originY + offsetY, new Vector2(crosshairPosition));
        } else {
            emptyGunSound.play();
            return new Bullet(-1, -1, new Vector2(0, 0));
        }
    }

    // Cast a fireball towards the crosshair position
    public Fireball castFireball() {
        if (attackPower > 5) {
            fireBallSound.play(1.0f);
            Vector2 fireballDirection = new Vector2(crosshairPosition.x - position.x, crosshairPosition.y - position.y).nor();
            facingRight = fireballDirection.x >= 0;

            float scale = 0.4f;
            TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime);
            float frameWidth = currentFrame.getRegionWidth() * scale;
            float frameHeight = currentFrame.getRegionHeight() * scale;

            float originX = position.x + frameWidth / 2f;
            float originY = position.y + frameHeight / 2f;
            float offsetX = facingRight ? 40 : -40;
            float offsetY = 0;

            return new Fireball(originX + offsetX, originY + offsetY, new Vector2(crosshairPosition));
        } else {
            emptyGunSound.play();
            return new Fireball(-1, -1, new Vector2(0, 0));
        }
    }

    // Collect items (increase health, attack, or super attack power)
    public void collectItem(String itemName) {
        itemsCollected++;
        itemPickupSound.play();
        if (itemName.equalsIgnoreCase("Vida")) {
            health = Math.min(health + 5, 100);
        } else if (itemName.equalsIgnoreCase("Ataque")) {
            setAddAttackPower(1);
        } else if (itemName.equalsIgnoreCase("SuperAtaque")) {
            setAddAttackPower(5);
        }
    }

    // Dispose of all textures and sounds
    public void dispose() {
        for (Texture t : loadedTextures) t.dispose();
        crosshairTexture.dispose();
        jumpSound.dispose();
        walkSound.dispose();
        fastMoveSound.dispose();
        itemPickupSound.dispose();
        hurtSound.dispose();
        deathSound.dispose();
        emptyGunSound.dispose();
        shootSound.dispose();
        fireBallSound.dispose();
    }

    // Set character state
    public void setState(State newState) {
        if (currentState == State.DEATH || (currentState == State.HURT && !hurtAnimation.isAnimationFinished(stateTime))) return;
        if (currentState != newState) {
            currentState = newState;
            stateTime = 0f;
            animationPaused = false;
        }
    }

    // Set character position
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    // Enable or disable animation
    public void setAnimating(boolean animating) {
        this.animating = animating;
    }

    // Getters for character properties
    public State getState() { return currentState; }
    public Vector2 getPosition() { return position; }
    public int getHealth() { return health; }
    public int getAttackPower() { return attackPower; }
    public void reduceAttackPower(int amount) { attackPower = Math.max(0, attackPower - amount); }
    public void setAddAttackPower(int addValue) { attackPower += addValue; }
    public int getItemsCollected() { return itemsCollected; }
    public boolean isDead() { return isDead; }
    public boolean isAnimationFinished() { return currentAnimation != null && currentAnimation.isAnimationFinished(stateTime); }
    public boolean isDeathAnimationFinished() { return currentState == State.DEATH && stateTime >= deathAnimation.getAnimationDuration(); }

    // Get character's bounding box (for collision detection)
    public Rectangle getBounds() {
        TextureRegion frame = currentAnimation.getKeyFrame(stateTime);
        return new Rectangle(position.x, position.y, frame.getRegionWidth() * 0.4f, frame.getRegionHeight() * 0.4f);
    }

    // Reset attack timer after an attack
    public void resetAttackTimer() {
        attackTimer = attackDuration;
    }

    // Perform a super attack if not currently dead, hurt, or already super attacking
    public void performSuperAttack() {
        if (currentState == State.DEATH || currentState == State.HURT || currentState == State.SUPER_ATTACK) return;

        setState(State.SUPER_ATTACK);
        setAnimating(true);
    }

    // Toggle defending state based on current state and health
    public void defending() {
        if (currentState == State.DEATH) return;

        boolean isDefending = currentState == State.DEFENSE || currentState == State.DEFENSE_INJURED;

        setState(isDefending
            ? (health < 25 ? State.IDLE_INJURED : State.IDLE)
            : (health < 25 ? State.DEFENSE_INJURED : State.DEFENSE));

        stateTime = 0f; // Reset animation timer
        setAnimating(true);
    }
    public Vector2 getVelocity() {
        return this.velocity;  // Assuming 'velocity' is a Vector2 field in the class
    }

    public void setVelocity(float x, float y) {
        this.velocity.set(x, y);
    }

    // Método para verificar se o personagem está no chão
    public boolean isOnGround() {
        // Verifique se a posição do personagem está próxima ao chão
        return velocity.y == 0;  // ou qualquer outra lógica que defina que ele está tocando o chão
    }

    // Método para verificar se o personagem está pulando
    public boolean isJumping() {
        return currentState == Character.State.JUMP;  // Ajuste isso conforme o estado de pulo no seu código
    }
    public void stopMovement() {
        this.velocity.x = 0;  // Zera a velocidade horizontal
    }

    private boolean movementLocked = false;

    public void lockMovementUntilJump() {
        movementLocked = true;
    }

    public void unlockMovement() {
        movementLocked = false;
    }

    public boolean isMovementLocked() {
        return movementLocked;
    }
    public boolean isAbove(Rectangle rect) {
     //   return position.y > rect.y + rect.height;
        return (position.y > rect.y + rect.height - 5); // 5 de margem
    }
    public void landOnPlatform(Rectangle platform) {
        position.y = platform.y + platform.height;
        groundY = position.y;
        isJumping = false;
        velocity.y = 0;
        unlockMovement();
        setIdleBasedOnHealth();
    }
    public void checkIfLanded(Array<Rectangle> platforms) {
        for (Rectangle platform : platforms) {
            if (getBounds().overlaps(platform) && velocity.y <= 0) {
                isJumping = false;
              //  grounded = true;

                // Se estiver acima da plataforma, pode andar
                if (isAbove(platform)) {
                    unlockMovement();
                }

                return;
            }
        }
    }
}
