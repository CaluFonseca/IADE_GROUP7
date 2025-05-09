package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.UniverseConqueror.ECS.components.*;
import com.badlogic.UniverseConqueror.ECS.systems.*;
import com.badlogic.UniverseConqueror.ECS.entity.ItemFactory;
import com.badlogic.UniverseConqueror.GameLauncher;
import com.badlogic.UniverseConqueror.Utils.*;
import com.badlogic.UniverseConqueror.ECS.entity.PlayerFactory;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.*;
import java.util.ArrayList;

import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class GameScreen implements Screen {
    private final GameLauncher game;
    private PooledEngine engine;
    private Entity player;
    private OrthographicCamera camera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private IsometricTiledMapRenderer mapRenderer;
    private TiledMap map;
    private ShapeRenderer shapeRenderer;

    private BitmapFont font;
    private Stage stage;
    private Table footerTable;
    private Texture uiskinTexture;
    private Label healthLabel, attackPowerLabel, itemsLabel, timerLabel;
    private TextureRegionDrawable healthBackground, attackPowerBackground, itemsBackground;
    private Skin skin;

    private TextureRegion cameraOnTexture, cameraOffTexture;
    private Image cameraIconImage;

    private Timer playingTimer;
    private ComponentMapper<HealthComponent> healthMapper;
    private ComponentMapper<AttackComponent> attackMapper;
    private CameraInputSystem cameraInputSystem;
    private Joystick joystick;
    private MapCollisionHandler collisionHandler;

    private Rectangle playerBounds;
    // Constructor
    public GameScreen(GameLauncher game) {
        this.game = game;
        this.playingTimer = new Timer(Float.MAX_VALUE);
        this.shapeRenderer = new ShapeRenderer();
        this.engine = new PooledEngine();
    }

    @Override
    public void show() {
        //  engine = new PooledEngine();
        initializeAssets();
        initializeWorld();
        initializeCamera();
        initializePlayer();
        initializeUI();
        initializeSystems();
        initializeItems();
        initializeInputProcessor();

    }

    private void initializeAssets() {
        cameraOnTexture = new TextureRegion(new Texture("camera_on.png"));
        cameraOffTexture = new TextureRegion(new Texture("camera_off.png"));
    }

    private void initializeWorld() {
        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();
        map = new TmxMapLoader().load("mapa.tmx");
        mapRenderer = new IsometricTiledMapRenderer(map);
      //  collisionHandler = new MapCollisionHandler(map, Constants.PPM, world);
        this.collisionHandler = new MapCollisionHandler(map, "Collisions", "Jumpable");
        collisionHandler.createBox2DBodies(world);
        shapeRenderer = new ShapeRenderer();
        // Create the contact listener
        MyContactListener myContactListener = new MyContactListener(engine);

// Set the contact listener to the world
        world.setContactListener(myContactListener);

     //   System.out.println("Total de corpos no mundo após carregar colisões: " + world.getBodyCount());
     //   System.out.println("Mapa carregado: " + map);
      //  System.out.println("Corpos no mundo: " + world.getBodyCount());
    }

    private void initializePlayer() {

        AnimationComponent tempAnim = new AnimationComponent();
        tempAnim.init();
        ObjectMap<StateComponent.State, Animation<TextureRegion>> anims = tempAnim.animations;

        int mapWidthInTiles = map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int tilePixelWidth = map.getProperties().get("tilewidth", Integer.class);
        int tilePixelHeight = map.getProperties().get("tileheight", Integer.class);

        float mapPixelWidth = mapWidthInTiles * tilePixelWidth;
        float mapPixelHeight = mapHeightInTiles * tilePixelHeight;
        float centerX = (mapWidthInTiles + mapHeightInTiles) * tilePixelWidth / 4f;
        float centerY = (mapHeightInTiles - mapWidthInTiles) * tilePixelHeight / 4f;
        ObjectMap<String, Sound> sounds = new ObjectMap<>();

        player = PlayerFactory.createPlayer(engine, new Vector2(centerX, centerY), anims, sounds, world);
        player.add(new PositionComponent());
        player.add(new VelocityComponent());
      //  System.out.println("Jogador criado: " + player);
    }

    private void initializeSystems() {
        //engine = new PooledEngine();
        cameraInputSystem = new CameraInputSystem(camera);
        engine.addSystem(new PlayerInputSystem(world, joystick));
        engine.addSystem(new MovementSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new JumpSystem(world));
        engine.addSystem(new PhysicsSystem(world));
        engine.addSystem(cameraInputSystem);
        engine.addSystem(new CameraSystem(camera, map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class),
            map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class)));
        engine.addSystem(new RenderSystem(new SpriteBatch(), camera));
        engine.addSystem(new ItemCollectionSystem(playerBounds, itemsLabel));
        engine.addSystem(new HealthSystem(
            null, // deathSound
            null, // hurtSound
            currentHealth -> healthLabel.setText("Health: " + currentHealth)
        ));
    }

    private void initializeUI() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        font = new BitmapFont();

        // Footer UI setup
        footerTable = new Table();
        footerTable.bottom().right();
        footerTable.setFillParent(true);

        stage = new Stage(new ScreenViewport());
        stage.addActor(footerTable);
        initializeLabels();

        // Timer setup
        timerLabel = new Label("00:00:00", skin);
        timerLabel.setFontScale(2f);
        Table timerTable = new Table();
        timerTable.top().setFillParent(true);
        timerTable.add(timerLabel).expandX().center();
        playingTimer.start();
        stage.addActor(timerTable);

        // Joystick setup
        joystick = new Joystick(new Texture("joystick_base.png"), new Texture("joystick_knob.png"), 100f, 100f, 60f);
        joystick.setPosition(50, 50);
        stage.addActor(joystick);

        // Camera icon setup
        cameraIconImage = new Image(cameraOnTexture);
        Table uiTable = new Table();
        uiTable.top().left();
        uiTable.setFillParent(true);
        uiTable.add(cameraIconImage).pad(10).size(48); // tamanho opcional

        // Adicionando o listener ao ícone da câmera
        cameraIconImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean following = cameraInputSystem.isFollowingPlayer();
                cameraInputSystem.setCameraFollow(!following);
                String newIcon = !following ? "camera_on.png" : "camera_off.png";
                cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(newIcon))));
            }
        });
        stage.addActor(uiTable);
    }

    private void initializeLabels() {
        healthMapper = ComponentMapper.getFor(HealthComponent.class);
        attackMapper = ComponentMapper.getFor(AttackComponent.class);

        HealthComponent healthComponent = healthMapper.get(player);
        AttackComponent attackComponent = attackMapper.get(player);

        healthLabel = new Label("Health: " + healthComponent.currentHealth, skin);
        attackPowerLabel = new Label("Attack: " + attackComponent.remainingAttackPower, skin);
        itemsLabel = new Label("Items: 0", skin);
        // UI skin setup
        uiskinTexture = new Texture("ui/uiskin.png");
        healthBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        attackPowerBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        itemsBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        Table healthBox = new Table();
        healthBox.setBackground(healthBackground); // Set the health background to the label
        healthBox.add(healthLabel).pad(5);

        Table attackBox = new Table();
        attackBox.setBackground(attackPowerBackground); // Set the attack power background to the label
        attackBox.add(attackPowerLabel).pad(5);

        Table itemsBox = new Table();
        itemsBox.setBackground(itemsBackground); // Set the items background to the label
        itemsBox.add(itemsLabel).pad(5);
        // Footer setup
        footerTable.center();
        footerTable.bottom();
        footerTable.add(healthBox).pad(10).left();
        footerTable.add(attackBox).pad(10).left();
        footerTable.add(itemsBox).pad(10).left();
    }

    private void initializeCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1.0f;
        float mapPixelWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float centerX = mapPixelWidth / 2f;

//        camera.position.set(centerX, 0, 0);
//        camera.update();
    }

    private void initializeInputProcessor() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(cameraInputSystem.getInputAdapter());
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void initializeItems() {
        ArrayList<ItemFactory> items = new ArrayList<>();
        float centerX = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class) / 2f;
        float centerY = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class) / 4f;

        items.add(new ItemFactory("Vida", centerX + 100, centerY, "item.png"));
        items.add(new ItemFactory("Ataque", centerX + 150, centerY + 50, "bullet_item.png"));
        items.add(new ItemFactory("SuperAtaque", centerX + 250, centerY + 70, "fireball_logo.png"));

        // Adiciona cada item à engine
        for (ItemFactory item : items)  {
            engine.addEntity(item.createEntity(engine, world));
        }
        SpriteBatch batchItem = new SpriteBatch();
        engine.addSystem(new RenderItemSystem(batchItem, camera));

    }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(1 / 60f, 6, 2);

        updateUI(delta);
        // Verifica se jogador morreu após o update dos sistemas
        HealthComponent health = healthMapper.get(player);
        triggerGameOver(health);
        renderWorld();
        stage.draw();

    }

    private void updateCameraPosition() {
        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (cameraInputSystem.isFollowingPlayer() && pos != null) {
            camera.position.set(pos.position.x, pos.position.y, 0);
        }
        camera.update();
    }

    private void updateUI(float delta) {
        stage.act();
        stage.draw();
        updateTimer(delta);
        updateCameraIcon();
        updateCameraPosition();
    }

    private void updateCameraIcon() {
        // Alterna o ícone da câmera dependendo do estado
        if (cameraInputSystem.isFollowingPlayer()) {
            cameraIconImage.setDrawable(new TextureRegionDrawable(cameraOnTexture));
        } else {
            cameraIconImage.setDrawable(new TextureRegionDrawable(cameraOffTexture));
        }
    }

    private void updateTimer(float delta) {
        playingTimer.update(delta);
        float elapsed = playingTimer.getTime();
        int hours = (int) (elapsed / 3600);
        int minutes = (int) ((elapsed % 3600) / 60);
        int seconds = (int) (elapsed % 60);
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerLabel.setText(timeFormatted);
    }

    private void renderWorld() {
        mapRenderer.setView(camera);
        mapRenderer.render();
        engine.update(Gdx.graphics.getDeltaTime());
        //renderCollisionDebug();
        debugRenderer.render(world, camera.combined);
    }

    private void triggerGameOver(HealthComponent health) {
        if (health != null && health.isDead()) {
            game.setScreen(new GameOverScreen(game));
        }
    }
//    private void renderCollisionDebug() {
//        shapeRenderer.setProjectionMatrix(camera.combined);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//
//        // Cor dos retângulos de colisão do mapa
//        shapeRenderer.setColor(Color.RED);
//        for (Rectangle rect : collisionHandler.getCollisionRects()) {
//            // Converter para coordenadas de tela
//            Vector2 topLeft = new Vector2(rect.x, rect.y + rect.height);
//            Vector2 topRight = new Vector2(rect.x + rect.width, rect.y + rect.height);
//            Vector2 bottomRight = new Vector2(rect.x + rect.width, rect.y);
//            Vector2 bottomLeft = new Vector2(rect.x, rect.y);
//
//            // Desenhar o retângulo como um losango
//            Vector2 center = new Vector2(rect.x + rect.width / 2, rect.y + rect.height / 2);
//            shapeRenderer.line(center.x, rect.y, rect.x + rect.width, center.y); // Baixo para direita
//            shapeRenderer.line(rect.x + rect.width, center.y, center.x, rect.y + rect.height); // Direita para cima
//            shapeRenderer.line(center.x, rect.y + rect.height, rect.x, center.y); // Cima para esquerda
//            shapeRenderer.line(rect.x, center.y, center.x, rect.y); // Esquerda para baixo
//
////            float centerX = rect.x + rect.width / 2f;
////            float centerY = rect.y + rect.height / 2f;
////
////            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
////            shapeRenderer.setColor(Color.BLUE);
////            shapeRenderer.circle(centerX, centerY, 3); // ponto central do retângulo
////            shapeRenderer.end();
//        }
//
//        // Cor do bounding box do jogador
////        shapeRenderer.setColor(Color.GREEN);
////        Rectangle playerBounds = player.getBounds();
////        shapeRenderer.rect(playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);
//
//
//        // Cor dos retângulos de colisão do mapa
//        shapeRenderer.setColor(Color.BLUE);
//        for (Rectangle jumpable : collisionHandler.getJumpableRects()) {
//            // Converter para coordenadas de tela
//            Vector2 topLeft = new Vector2(jumpable.x, jumpable.y + jumpable.height);
//            Vector2 topRight = new Vector2(jumpable.x + jumpable.width, jumpable.y + jumpable.height);
//            Vector2 bottomRight = new Vector2(jumpable.x + jumpable.width, jumpable.y);
//            Vector2 bottomLeft = new Vector2(jumpable.x, jumpable.y);
//
//            // Desenhar o retângulo como um losango
//            Vector2 center = new Vector2(jumpable.x + jumpable.width / 2, jumpable.y + jumpable.height / 2);
//            shapeRenderer.line(center.x, jumpable.y, jumpable.x + jumpable.width, center.y); // Baixo para direita
//            shapeRenderer.line(jumpable.x + jumpable.width, center.y, center.x, jumpable.y + jumpable.height); // Direita para cima
//            shapeRenderer.line(center.x, jumpable.y + jumpable.height, jumpable.x, center.y); // Cima para esquerda
//            shapeRenderer.line(jumpable.x, center.y, center.x, jumpable.y); // Esquerda para baixo
//
//
//        }
//        shapeRenderer.end();
//    }

    @Override
    public void resize(int width, int height) {
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        debugRenderer.dispose();
        shapeRenderer.dispose();
    }
}

