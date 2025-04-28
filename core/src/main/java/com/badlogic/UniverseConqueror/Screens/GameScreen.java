package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.UniverseConqueror.Utils.Timer;
import com.badlogic.UniverseConqueror.Attacks.Bullet;
import com.badlogic.UniverseConqueror.Attacks.Fireball;
import com.badlogic.UniverseConqueror.GameLauncher;
import com.badlogic.UniverseConqueror.Utils.Joystick;
import com.badlogic.UniverseConqueror.Utils.Item;
import com.badlogic.UniverseConqueror.Utils.Minimap;
import com.badlogic.UniverseConqueror.characters.Character;
import com.badlogic.UniverseConqueror.characters.Enemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {
    // Game and UI related attributes
    private final GameLauncher game; // Reference to the Game (GameLauncher)
    private GameScreen gameScreenInstance;
    private Stage stage;
    private Skin skin;
    private Table footerTable;
    private Label healthLabel, attackPowerLabel, itemsLabel;
    private Image cameraIconImage;
    private BitmapFont font;
    private Minimap minimap;
    private Texture uiskinTexture;
    private TextureRegionDrawable healthBackground, attackPowerBackground, itemsBackground;
    private Joystick joystick;
    private Label timerLabel;
    private Label enemiesKilledLabel;
    private int enemiesKilled = 0;

    // Camera, Map, and Rendering related attributes
    private TiledMap map;
    private IsometricTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Game objects related attributes
    private Character player;
    private ArrayList<Item> items;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Fireball> fireballs;

    // Game state related attributes
    private boolean isPaused = false;
    private boolean cameraFollow = true;
    private boolean cameraControlEnabled = false;
    private Vector2 savedPlayerPosition; // Store the player's position
    private ArrayList<Item> savedItems; // Store uncollected items
    private Timer playingTimer;


    // Variables for dragging the camera position
    private float dragStartX, dragStartY;  // Starting positions for drag events

    // Constructor initializes the GameScreen instance and game-related attributes
    public GameScreen(GameLauncher game) {
        this.game = game;
        this.savedPlayerPosition = null;
        this.savedItems = new ArrayList<>();
        this.gameScreenInstance = this;
        playingTimer = new Timer(Float.MAX_VALUE);
    }

    // Called when the screen is shown (initialization of resources)
    @Override
    public void show() {
        if (this.player != null) return; // Prevent re-initializing if already initialized

        // Initialize rendering components
        initializeRendering();

        // Initialize game objects (player, enemies, items, etc.)
        initializeGameObjects();

        // Initialize UI elements (stage, labels, camera icon, etc.)
        initializeUI();

        // Configure input handling (keyboard, mouse, joystick)
        configureInput();
    }

    // Initialize rendering components (camera, map, batch, etc.)
    private void initializeRendering() {
        shapeRenderer = new ShapeRenderer();
        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1); // Set sky blue background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Load map and initialize map renderer
        map = new TmxMapLoader().load("mapa.tmx");
        mapRenderer = new IsometricTiledMapRenderer(map, 1f);

        // Initialize sprite batch for rendering
        batch = new SpriteBatch();
    }

    // Initializes the enemies on the screen at the specified position (x, y)
    public void initializeEnemies(float x, float y) {
        enemies = new ArrayList<>();

        // Add enemies with different positions, health, attack, and speed values
        enemies.add(new Enemy("Alien0", x - 700, y, 100, 3, 50f));
        enemies.add(new Enemy("Alien1", x - 500, y - 500, 150, 3, 30f));
        enemies.add(new Enemy("Alien2", x - 500, y + 500, 150, 3, 30f));
        enemies.add(new Enemy("Alien3", x + 700, y + 1000, 100, 3, 50f));
        enemies.add(new Enemy("Alien4", x + 500, y + 500, 150, 3, 30f));
        enemies.add(new Enemy("Alien5", x + 500, y + 500, 150, 3, 30f));
    }

    // Initialize game objects (player, enemies, items, etc.)
    private void initializeGameObjects() {
        bullets = new ArrayList<>();
        fireballs = new ArrayList<>();

        // Map dimensions
        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);

        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float centerX = mapWidth * tileWidth / 2f;
        float centerY = 0;
        camera.position.set(centerX, centerY, 0);
        camera.zoom = 0.8f;
        camera.update();

        // Initialize enemies and player
        initializeEnemies(centerX, centerY);
        player = new Character("Soldier", centerX, centerY, 100, 100);

        // Initialize minimap
        minimap = new Minimap("minimap.png", "marker.png", 10, 10, 100, 100, player);
        minimap.setWorldSize(mapWidth, mapHeight); // Set world size for minimap

        // Initialize items
        items = new ArrayList<>();
        items.add(new Item("Vida", centerX + 100, centerY, "item.png"));
        items.add(new Item("Ataque", centerX + 150, centerY + 50, "bullet_item.png"));
        items.add(new Item("SuperAtaque", centerX + 250, centerY + 70, "fireball_logo.png"));
    }

    // Initialize UI components (labels, footer, etc.)
    private void initializeUI() {
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        font = new BitmapFont();
        stage.addActor(minimap);

        // Timer setup
        timerLabel = new Label("00:00:00", skin);
        timerLabel.setFontScale(2f);

        Table timerTable = new Table();
        timerTable.top();
        timerTable.setFillParent(true);
        timerTable.add(timerLabel).expandX().center();

        playingTimer.start();
        stage.addActor(timerTable);

        // Killed Enemy count
        Image killedCounterImage = new Image(new Texture("Killed_alien_counter.png"));
        enemiesKilledLabel = new Label("0", skin);
        enemiesKilledLabel.setFontScale(0.7f);
        enemiesKilledLabel.setAlignment(Align.center);

        Stack killsStack = new Stack();
        killsStack.add(killedCounterImage);
        killsStack.add(enemiesKilledLabel);

        Table killsTable = new Table();
        killsTable.top().right();
        killsTable.setFillParent(true);
        killsTable.add(killsStack).size(50, 60).pad(10);

        stage.addActor(killsTable);

        // UI skin setup
        uiskinTexture = new Texture("ui/uiskin.png");
        healthBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        attackPowerBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        itemsBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));

        // Footer UI setup
        footerTable = new Table();
        footerTable.bottom().right();
        footerTable.setFillParent(true);

        // Setup labels for health, attack power, and items
        healthLabel = new Label("Health: " + player.getHealth(), skin);
        attackPowerLabel = new Label("Attack: " + player.getAttackPower(), skin);
        itemsLabel = new Label("Items: " + player.getItemsCollected(), skin);

        // Camera control icon
        cameraIconImage = new Image(new Texture("camera_off.png"));
        cameraIconImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cameraControlEnabled = !cameraControlEnabled;
                String texture = cameraControlEnabled ? "camera_on.png" : "camera_off.png";
                cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(texture))));
            }
        });

        // Set up label backgrounds properly
        Table healthBox = new Table();
        healthBox.setBackground(healthBackground); // Set the health background to the label
        healthBox.add(healthLabel).pad(5);

        Table attackBox = new Table();
        attackBox.setBackground(attackPowerBackground); // Set the attack power background to the label
        attackBox.add(attackPowerLabel).pad(5);

        Table itemsBox = new Table();
        itemsBox.setBackground(itemsBackground); // Set the items background to the label
        itemsBox.add(itemsLabel).pad(5);

        // Add components to the footer table
        footerTable.add(cameraIconImage).size(100, 100).pad(10).left();
        footerTable.add(healthBox).pad(10).left();
        footerTable.add(attackBox).pad(10).left();
        footerTable.add(itemsBox).pad(10).left();
        footerTable.add(minimap).size(100, 100).pad(10).left();
        stage.addActor(footerTable);

        // Joystick setup
        Texture base = new Texture("joystick_base.png");
        Texture knob = new Texture("joystick_knob.png");
        joystick = new Joystick(base, knob, 100, 100, 70);
        stage.addActor(joystick);
    }

    // Configure input handling (keyboard, mouse, joystick)
    private void configureInput() {
        InputAdapter inputAdapter = new InputAdapter() {
            // Handle zoom with mouse scroll
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (!isPaused) {
                    float targetZoom = MathUtils.clamp(camera.zoom + amountY * 0.1f, 0.5f, 3f);
                    camera.zoom = MathUtils.lerp(camera.zoom, targetZoom, 0.2f);
                }
                return true;
            }

            // Handle key press events
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE && !isPaused) {
                    PauseScreen pauseScreen = new PauseScreen(game, gameScreenInstance);
                    game.setScreen(pauseScreen);
                    setPaused(true); // Pauses the game
                }

                if (keycode == Input.Keys.C) {
                    // Toggle camera control on/off
                    cameraControlEnabled = !cameraControlEnabled;
                    if (cameraControlEnabled) {
                        cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_on.png"))));
                    } else {
                        cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
                    }
                }

                // Perform super attack when E is pressed
                if (keycode == Input.Keys.E) {
                    player.performSuperAttack();
                }

                // Perform defense when TAB is pressed
                if (keycode == Input.Keys.TAB) {
                    player.defending();
                }

                return false;
            }

            // Handle touch input events
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.RIGHT && cameraControlEnabled) {
                    Vector3 world = new Vector3(screenX, screenY, 0);
                    camera.unproject(world);
                    dragStartX = world.x;
                    dragStartY = world.y;
                    cameraFollow = false;
                }
                if (button == Input.Buttons.LEFT && !player.isDead()) {
                    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        // Fireball or bullet depending on the player's state
                        if (player.getState() == Character.State.SUPER_ATTACK) {
                            fireballs.add(player.castFireball());
                            player.reduceAttackPower(5);
                        } else {
                            bullets.add(player.shootBullet());
                            player.reduceAttackPower(1);
                            player.setState(Character.State.ATTACK);
                            player.setAnimating(true);
                            player.resetAttackTimer();
                        }
                    }
                }
                return true;
            }

            // Handle touch dragging events
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (!cameraFollow && cameraControlEnabled) {
                    Vector3 world = new Vector3(screenX, screenY, 0);
                    camera.unproject(world);
                    float dx = dragStartX - world.x;
                    float dy = dragStartY - world.y;
                    camera.position.add(dx, dy, 0);
                    dragStartX = world.x;
                    dragStartY = world.y;
                }
                return true;
            }

            // Handle touch up events
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    cameraFollow = false;
                }
                return true;
            }
        };

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);         // UI interactions
        multiplexer.addProcessor(inputAdapter);  // Custom controls
        Gdx.input.setInputProcessor(multiplexer);
    }

    // Update the game screen (player, enemies, items, etc.)
    @Override
    public void render(float delta) {
        if (isPaused) {
            return;  // Skip rendering when paused
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1); // Set background color to black
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen for the new frame

        player.update(delta, camera); // Update player position and state

        // Update fireballs and check for collisions with enemies
        Iterator<Fireball> fireballIterator = fireballs.iterator();
        while (fireballIterator.hasNext()) {
            Fireball fireball = fireballIterator.next();
            fireball.update(delta);

            for (Enemy enemy : enemies) {
                if (fireball.getBounds().overlaps(enemy.getBounds())) {
                    enemy.takeDamage(90); // Deal damage to the enemy
                    fireballIterator.remove(); // Remove fireball after collision
                    break;
                }
            }

            if (fireball.isOutOfBounds(camera)) {
                fireballIterator.remove();
            }
        }

        // Update bullets and check for collisions with enemies
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(delta);

            for (Enemy enemy : enemies) {
                if (bullet.getBounds().overlaps(enemy.getBounds())) {
                    enemy.takeDamage(player.getAttackPower()); // Deal damage to the enemy
                    bulletIterator.remove(); // Remove bullet after collision
                    break;
                }
            }

            if (bullet.isOutOfBounds(camera)) {
                bulletIterator.remove();
            }
        }

        // Update enemies and check for collisions with the player
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                enemy.update(delta, player.getPosition(), camera);

                if (enemy.checkCollision(player.getBounds())) {
                    enemy.attack(player);
                    if (!player.isDead() && (player.getState() != Character.State.DEFENSE) && (player.getState() != Character.State.DEFENSE_INJURED)) {
                        player.setState(Character.State.HURT);
                        player.setAnimating(true);
                    }
                    if (player.getHealth() <= 0 && !player.isDead()) {
                        player.die();
                    }
                }
            } else {
                enemiesKilled++;
                enemies.remove(enemy);
                break;
            }
        }

        enemiesKilledLabel.setText(enemiesKilled);

        // If the player is dead, wait for the death animation to finish before transitioning to Game Over screen
        if (player.isDead()) {
            if (player.isDeathAnimationFinished()) {
                for (Enemy enemy : enemies) {
                    enemy.stopSound(); // Stop enemy sounds
                }
                game.setScreen(new GameOverScreen(game)); // Transition to Game Over screen
                return;
            }
        }

        // Update items and check if they are collected by the player
        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            item.update(player.getBounds()); // Update item position based on player bounds
            if (item.isCollected()) {
                player.collectItem(item.getName()); // Collect the item
                itemIterator.remove(); // Remove item from the list
            }
        }

        // Update HUD labels with current player data
        healthLabel.setText("Health: " + player.getHealth());
        attackPowerLabel.setText("Attack: " + player.getAttackPower());
        itemsLabel.setText("Items: " + player.getItemsCollected());

        // Handle movement inputs (WASD or joystick)
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        boolean moved = false;
        float speed = 100 * delta;
        float dx = 0, dy = 0;

        if (player.isDead()) {
            player.setState(Character.State.DEATH); // Set to death state
            player.setAnimating(true);  // Start death animation
            camera.position.set(player.getPosition(), 0); // Follow player during death
            camera.update();

            mapRenderer.setView(camera);
            mapRenderer.render();
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            player.render(batch);  // Render the player

            // Render all bullets and fireballs
            for (Bullet bullet : bullets) {
                bullet.update(delta);
                bullet.render(batch);
            }
            for (Fireball fireball : fireballs) {
                fireball.update(delta);
                fireball.render(batch);
            }
            batch.end();

            stage.act(Math.min(delta, 1 / 30f)); // Update stage (UI)
            stage.draw(); // Draw UI
            return;  // Stop rendering after player death
        }

        // Handle movement inputs (WASD keys)
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += speed;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= speed;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= speed;
            moved = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += speed;
            moved = true;
        }

        // Handle joystick movement input
        if (joystick.isMoving()) {
            Vector2 direction = joystick.getDirection();  // Get joystick direction
            dx = direction.x * speed;  // Calculate movement in x direction
            dy = direction.y * speed;  // Calculate movement in y direction
            moved = true;
        }

        // Camera fallow character if moved
        if (moved ) {
            cameraFollow = true;
        }

        // Handle character state and movement
        if (player.getState() != Character.State.ATTACK) {
            if (shift) {
                if (player.getHealth() > 25) {
                    player.setState(Character.State.FAST_MOVE);  // Set to fast movement if shift is held
                }
                if (moved) {
                    player.updatePosition(dx, dy, shift);
                    player.setAnimating(true);
                } else {
                    player.updatePosition(0, 0, shift); // Stop movement if no key is pressed
                    player.setAnimating(false);
                }
            } else if (moved) {
                if (player.getHealth() < 25) {
                    player.setState(Character.State.WALK_INJURED);  // Set to injured walk if health is low
                } else {
                    player.setState(Character.State.WALK);
                }
                player.updatePosition(dx, dy, shift);
                player.setAnimating(true);
            } else if (player.getState() != Character.State.SUPER_ATTACK && player.getState() != Character.State.DEFENSE && player.getState() != Character.State.DEFENSE_INJURED) {
                if (player.getHealth() < 25) {
                    player.setState(Character.State.IDLE_INJURED);  // Set to injured idle if health is low
                } else {
                    player.setState(Character.State.IDLE);
                }
                player.updatePosition(0, 0, shift); // Stop movement if no key is pressed
                player.setAnimating(true);
            }

            // Handle jump input (space key)
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (player.getHealth() > 25) {
                    player.jump();  // Make the player jump if health is sufficient
                }
            }

            // Handle super attack input (E key)
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                player.performSuperAttack();
            }
        }

        // Handle camera control input (arrow keys or WASD)
        if (cameraControlEnabled) {
            float camSpeed = 5f * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) camera.position.x -= camSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camera.position.x += camSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) camera.position.y += camSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) camera.position.y -= camSpeed;
        }

        // Follow the player with the camera if enabled
        if (cameraFollow) {
            Vector3 targetPosition = new Vector3(player.getPosition().x, player.getPosition().y, 0);
            camera.position.lerp(targetPosition, 5f * delta);
        }
        camera.update();  // Update camera after movement
        mapRenderer.setView(camera);
        mapRenderer.render();  // Render the map

        // Render all objects (enemies, items, bullets, fireballs, player)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Enemy enemy : enemies) {
            enemy.render(batch);  // Render each enemy
        }
        for (Item item : items) {
            item.render(batch);  // Render each item
        }
        for (Fireball fireball : fireballs) {
            fireball.render(batch);  // Render each fireball
        }
        for (Bullet bullet : bullets) {
            bullet.render(batch);  // Render each bullet
        }
        player.render(batch);  // Render the player
        batch.end();

        // Draw health bar for enemies
        for (Enemy enemy : enemies) {
            if (enemy.wasHurt() && !enemy.isDead()) {
                drawHealthBar(batch, enemy);  // Draw health bar if enemy was hurt
            }
        }

        // Update the timer
        playingTimer.update(delta);

        // Update the timer label
        updateTimer();

        // Update stage and UI
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();  // Draw the UI
    }

    // Draw the health bar for an enemy on the screen
    private void drawHealthBar(SpriteBatch batch, Enemy enemy) {
        float barWidth = 40f;
        float barHeight = 5f;

        // Calculate the health percentage
        float healthPercent = (float) enemy.getHealth() / enemy.getMaxHealth();
        float barX = enemy.getPosition().x - barWidth / 2;
        float barY = enemy.getPosition().y + 40f; // Adjust based on enemy sprite position

        shapeRenderer.setProjectionMatrix(camera.combined);
        // Draw the red background bar (empty health bar)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Draw the green health bar (filled portion)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();
    }

    // Update the timer display to show elapsed time in hours:minutes:seconds format
    private void updateTimer() {
        float elapsed = playingTimer.getTime();

        // Calculate hours, minutes, and seconds from the elapsed time
        int hours = (int) (elapsed / 3600);
        int minutes = (int) ((elapsed % 3600) / 60);
        int seconds = (int) (elapsed % 60);

        // Format and display the time
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerLabel.setText(timeFormatted);  // Update the timer label
    }

    // Save the current state of the game, including the player's position and uncollected items
    public void saveState() {
        savedPlayerPosition = player.getPosition().cpy();  // Save the player's position
        savedItems.clear();  // Clear previously saved items

        // Save only uncollected items
        for (Item item : items) {
            if (!item.isCollected()) {
                savedItems.add(item);
            }
        }
    }

    // Restore the saved state of the game, including the player's position and items
    public void restoreState() {
        if (savedPlayerPosition != null) {
            player.setPosition(savedPlayerPosition.x, savedPlayerPosition.y);  // Restore the player's position
        }

        items.clear();  // Clear current items

        // Restore saved items
        for (Item item : savedItems) {
            Item restoredItem = item.copy();  // Create a copy of each item
            items.add(restoredItem);
        }
    }

    // Set the game state to paused or resumed, saving or restoring the game state accordingly
    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (paused) {
            saveState();  // Save the game state when paused
        } else {
            restoreState();  // Restore the game state when resumed
        }
    }

    // Resume the game from a paused state and reconfigure input handling
    public void resumeGame() {
        setPaused(false);  // Set the game as not paused
        configureInput();  // Reconfigure input handling for the resumed game
    }

    // Resize the camera view when the window size changes
    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();  // Update the camera with the new size
    }

    // Hide the screen (currently not used in this context)
    @Override
    public void hide() {
        // Placeholder for hiding the screen if needed (currently empty)
    }

    // Pause the game (currently not implemented but can be customized)
    @Override
    public void pause() {
        // Placeholder for pausing the game (currently empty)
    }

    // Resume the game (currently not implemented but can be customized)
    @Override
    public void resume() {
        // Placeholder for resuming the game (currently empty)
    }

    // Dispose of resources when the screen is no longer needed
    @Override
    public void dispose() {
        stage.dispose();  // Dispose of stage resources
        skin.dispose();  // Dispose of UI skin resources
        batch.dispose();  // Dispose of sprite batch
        map.dispose();  // Dispose of the map
        shapeRenderer.dispose();  // Dispose of shape renderer

        // Dispose of fireballs in the game
        for (Fireball fireball : fireballs) {
            fireball.dispose();
        }
    }
}


//    public void reset() {
//        // Limpa os recursos anteriores
//        if (mapRenderer != null) mapRenderer.dispose();
//        if (batch != null) batch.dispose();
//        if (stage != null) stage.dispose();
//        if (uiskinTexture != null) uiskinTexture.dispose();
//        if (skin != null) skin.dispose();
//
//        // Recarrega o mapa
//        map = new TmxMapLoader().load("mapa.tmx");
//        mapRenderer = new IsometricTiledMapRenderer(map, 1f);
//        batch = new SpriteBatch();
//
//        mapWidth = map.getProperties().get("width", Integer.class);
//        mapHeight = map.getProperties().get("height", Integer.class);
//        tileWidth = map.getProperties().get("tilewidth", Integer.class);
//        tileHeight = map.getProperties().get("tileheight", Integer.class);
//
//        float centerX = mapWidth * tileWidth / 2f;
//        float centerY = 0;
//
//        // Recria a câmera
//        camera = new OrthographicCamera();
//        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        camera.position.set(centerX, centerY, 0);
//        camera.zoom = 0.8f;
//        camera.update();
//
//        // Recria o jogador e elementos relacionados
//        player = new Character("Soldado", centerX, centerY, 100, 20);
//        minimap = new Minimap("minimap.png", "marker.png", 10, 10, 100, 100, player);
//        minimap.setWorldSize(mapWidth, mapHeight);
//
//        // Reinicializa itens e inimigos
//        items = new ArrayList<>();
//        items.add(new Item("Vida", centerX + 100, centerY, "item.png"));
//        initializeEnemies(centerX, centerY);
//
//        // Recria a interface
//        stage = new Stage();
//        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
//        font = new BitmapFont();
//        stage.addActor(minimap);
//
//        uiskinTexture = new Texture("ui/uiskin.png");
//        healthBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
//        attackPowerBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
//        itemsBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
//
//        footerTable = new Table();
//        footerTable.bottom().right();
//        footerTable.setFillParent(true);
//
//        healthLabel = new Label("Vida: " + player.getHealth(), skin);
//        attackPowerLabel = new Label("Ataque: " + player.getAttackPower(), skin);
//        itemsLabel = new Label("Itens: " + player.getItemsCollected(), skin);
//
//        cameraIconImage = new Image(new Texture("camera_off.png"));
//        cameraIconImage.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                cameraControlEnabled = !cameraControlEnabled;
//                String texture = cameraControlEnabled ? "camera_on.png" : "camera_off.png";
//                cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(texture))));
//            }
//        });
//
//        Table healthBox = new Table();
//        healthBox.setBackground(healthBackground);
//        healthBox.add(healthLabel).pad(5);
//
//        Table attackBox = new Table();
//        attackBox.setBackground(attackPowerBackground);
//        attackBox.add(attackPowerLabel).pad(5);
//
//        Table itemsBox = new Table();
//        itemsBox.setBackground(itemsBackground);
//        itemsBox.add(itemsLabel).pad(5);
//
//        footerTable.add(cameraIconImage).size(100, 100).pad(10).left();
//        footerTable.add(healthBox).pad(10).left();
//        footerTable.add(attackBox).pad(10).left();
//        footerTable.add(itemsBox).pad(10).left();
//        footerTable.add(minimap).size(100, 100).pad(10).left();
//        stage.addActor(footerTable);
//
//        // Recria o joystick
//        Texture base = new Texture("joystick_base.png");
//        Texture knob = new Texture("joystick_knob.png");
//        joystick = new Joystick(base, knob, 100, 100, 70);
//        stage.addActor(joystick);
//
//        // Reseta estados
//        isPaused = false;
//        savedPlayerPosition = null;
//        savedItems.clear();
//        cameraFollow = true;
//        cameraControlEnabled = false;
//
//        configureInput();
//    }


