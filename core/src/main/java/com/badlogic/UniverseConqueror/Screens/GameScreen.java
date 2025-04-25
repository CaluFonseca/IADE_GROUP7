package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.UniverseConqueror.Attacks.Bullet;
import com.badlogic.UniverseConqueror.GameLauncher;
import com.badlogic.UniverseConqueror.Joystick;
import com.badlogic.UniverseConqueror.Item;
import com.badlogic.UniverseConqueror.Minimap;
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

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {
    private final GameLauncher game; // Referência ao Game (GameLauncher)
    private GameScreen gameScreenInstance;

    private TiledMap map;
    private IsometricTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Character player;
    private ArrayList<Item> items;

    private Stage stage;
    private Skin skin;
    private Table footerTable;
    private Label healthLabel, attackPowerLabel, itemsLabel;
    private Image cameraIconImage;
    ;
    private BitmapFont font;
    private Minimap minimap;

    private Texture uiskinTexture;
    private TextureRegionDrawable healthBackground, attackPowerBackground, itemsBackground;

    private boolean cameraFollow = true;
    private float dragStartX, dragStartY;
    private boolean cameraControlEnabled = false;

    private boolean isPaused = false;

    private Vector2 savedPlayerPosition;  // Para armazenar a posição como Vec
    private ArrayList<Item> savedItems;   // Para armazenar os itens coletados

    private Joystick joystick;
    private int mapWidth;
    private int mapHeight;
    private int tileWidth;
    private int tileHeight;
    private ArrayList<Enemy> enemies;  // Lista de inimigos
    private ArrayList<Bullet> bullets;
    private ShapeRenderer shapeRenderer;

    public GameScreen(GameLauncher game) {
        this.game = game;
        this.savedPlayerPosition = null;
        this.savedItems = new ArrayList<>();
        this.gameScreenInstance = this;
    }

    public void saveState() {
        savedPlayerPosition = player.getPosition().cpy();
        savedItems.clear(); // Limpa para garantir que não vamos acumular
        for (Item item : items) {
            if (!item.isCollected()) {
                savedItems.add(item);
            }
        }
    }

    public void restoreState() {
        // Restaure a posição do jogador e o estado dos itens
        if (savedPlayerPosition != null) {
            player.setPosition(savedPlayerPosition.x, savedPlayerPosition.y);  // Agora funciona com Vector2
        }
        items.clear();
        for (Item item : savedItems) {
            // Cria novos itens com a posição e estado restaurado
            Item restoredItem = item.copy();
            items.add(restoredItem);
        }
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (paused) {
            saveState();
        } else {
            restoreState();
        }
    }

    public void resumeGame() {
        setPaused(false);
        configureInput();
    }

    public void initializeEnemies(float x, float y) {
        enemies = new ArrayList<>();
        enemies.add(new Enemy("Goblin0", x - 700, y, 100, 3, 50f));
        enemies.add(new Enemy("Goblin1", x - 500, y - 500, 150, 3, 30f));
        enemies.add(new Enemy("Goblin2", x - 500, y + 500, 150, 3, 30f));
        enemies.add(new Enemy("Goblin3", x + 700, y + 1000, 100, 3, 50f));
        enemies.add(new Enemy("Goblin4", x + 500, y + 500, 150, 3, 30f));
        enemies.add(new Enemy("Goblin5", x + 500, y + 500, 150, 3, 30f));
    }

    @Override
    public void show() {
        // Evita reiniciar o estado do jogo
        if (this.player != null) return;
        shapeRenderer = new ShapeRenderer();
        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1); // azul claro tipo céu
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        map = new TmxMapLoader().load("mapa.tmx");
        mapRenderer = new IsometricTiledMapRenderer(map, 1f);
        batch = new SpriteBatch();
        bullets = new ArrayList<>();
        mapWidth = map.getProperties().get("width", Integer.class);
        mapHeight = map.getProperties().get("height", Integer.class);
        tileWidth = map.getProperties().get("tilewidth", Integer.class);
        tileHeight = map.getProperties().get("tileheight", Integer.class);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float centerX = mapWidth * tileWidth / 2f;
        float centerY = 0;
        camera.position.set(centerX, centerY, 0);
        camera.zoom = 0.8f;

        camera.update();
        // Exemplo de como criar inimigos
        initializeEnemies(centerX, centerY);
        player = new Character("Soldado", centerX, centerY, 100, 20);
        minimap = new Minimap("minimap.png", "marker.png", 10, 10, 100, 100, player);
        minimap.setWorldSize(mapWidth, mapHeight); // definir tamanho do mundo real

        items = new ArrayList<>();
        items.add(new Item("Vida", centerX + 100, centerY, "item.png"));
        items.add(new Item("Ataque", centerX + 150, centerY + 50, "bullet_item.png"));
        stage = new Stage();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        font = new BitmapFont();
        stage.addActor(minimap);
        uiskinTexture = new Texture("ui/uiskin.png");
        healthBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        attackPowerBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));
        itemsBackground = new TextureRegionDrawable(new TextureRegion(uiskinTexture, 0, 80, 190, 75));

        footerTable = new Table();
        footerTable.bottom().right();
        footerTable.setFillParent(true);

        healthLabel = new Label("Vida: " + player.getHealth(), skin);
        attackPowerLabel = new Label("Ataque: " + player.getAttackPower(), skin);
        itemsLabel = new Label("Itens: " + player.getItemsCollected(), skin);

        // Adicionar o ícone da câmera
        cameraIconImage = new Image(new Texture("camera_off.png"));
        cameraIconImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Alterna entre câmera ligada/desligada
                cameraControlEnabled = !cameraControlEnabled;
                if (cameraControlEnabled) {
                    cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_on.png"))));
                } else {
                    cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
                }
            }
        });

        Table healthBox = new Table();
        healthBox.setBackground(healthBackground);
        healthBox.add(healthLabel).pad(5);

        Table attackBox = new Table();
        attackBox.setBackground(attackPowerBackground);
        attackBox.add(attackPowerLabel).pad(5);

        Table itemsBox = new Table();
        itemsBox.setBackground(itemsBackground);
        itemsBox.add(itemsLabel).pad(5);

        footerTable.add(cameraIconImage).size(100, 100).pad(10).left();
        footerTable.add(healthBox).pad(10).left();
        footerTable.add(attackBox).pad(10).left();
        footerTable.add(itemsBox).pad(10).left();
        footerTable.add(minimap).size(100, 100).pad(10).left();
        stage.addActor(footerTable);

        Texture base = new Texture("joystick_base.png");
        Texture knob = new Texture("joystick_knob.png");

        joystick = new Joystick(base, knob, 100, 100, 70);
        stage.addActor(joystick);

        configureInput();
    }

    private void configureInput() {
        InputAdapter inputAdapter = new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (!isPaused) {
                    float targetZoom = MathUtils.clamp(camera.zoom + amountY * 0.1f, 0.5f, 3f);
                    camera.zoom = MathUtils.lerp(camera.zoom, targetZoom, 0.2f);
                }
                return true;
            }

            public boolean isPaused() {
                return isPaused;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE && !isPaused) {
                    PauseScreen pauseScreen = new PauseScreen(game, gameScreenInstance);
                    game.setScreen(pauseScreen);
                    setPaused(true);
                }

                if (keycode == Input.Keys.C) {
                    cameraControlEnabled = !cameraControlEnabled;
                    if (cameraControlEnabled) {
                        cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_on.png"))));
                    } else {
                        cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
                    }
                }
                return false;
            }

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
                        bullets.add(player.shootBullet());
                        player.setAttackPower();
                        player.setState(Character.State.ATTACK);
                        player.setAnimating(true);
                        player.resetAttackTimer();
                    }
                }
                return true;
            }

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

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    cameraFollow = false;
                }
                return true;
            }
        };

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);         // UI interativa
        multiplexer.addProcessor(inputAdapter);  // Controles personalizados
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void drawHealthBar(SpriteBatch batch, Enemy enemy) {
        float barWidth = 40f;
        float barHeight = 5f;

        float healthPercent = (float) enemy.getHealth() / enemy.getMaxHealth();
        float barX = enemy.getPosition().x - barWidth / 2;
        float barY = enemy.getPosition().y + 40f; // Ajusta conforme a altura do sprite

        shapeRenderer.setProjectionMatrix(camera.combined);
        // Barra vermelha (fundo)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Barra verde (vida restante)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();
    }

    @Override
    public void render(float delta) {
        if (isPaused) {
            // Renderização pausada
            return;
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        player.update(delta, camera);

        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update(delta);

            for (Enemy enemy : enemies) {

                if (bullet.getBounds().overlaps(enemy.getBounds())) {
                    enemy.takeDamage(player.getAttackPower()); // Cria esse método se necessário
                    bulletIterator.remove();
                    break;
                }
            }

            if (bullet.isOutOfBounds(camera)) {
                bulletIterator.remove();
            }
        }

        // Atualiza os inimigos
        for (Enemy enemy : enemies) {
            enemy.update(delta, player.getPosition(), camera);
            // Verifica colisão com o personagem
            if (enemy.checkCollision(player.getBounds())) {
                // Inimigo colidiu com o personagem
                enemy.attack(player);

                if (!player.isDead()) {
                    player.setState(Character.State.HURT);
                    player.setAnimating(true);
                }

                if (player.getHealth() <= 0 && !player.isDead()) {
                    player.die();
                }
            }
        }
        // Verifica se o jogador morreu
        if (player.isDead()) {
            // Espera até que a animação de morte seja concluída antes de transitar para a tela de Game Over
            if (player.isDeathAnimationFinished()) {
                for (Enemy enemy : enemies) {
                    enemy.stopSound();
                }
                game.setScreen(new GameOverScreen(game));
                return;  // Impede a continuação da renderização após a transição de tela
            }
        }
        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            item.update(player.getBounds());
            if (item.isCollected()) {
                player.collectItem(item.getName());
                itemIterator.remove();
            }
        }

        healthLabel.setText("Vida: " + player.getHealth());
        attackPowerLabel.setText("Ataque: " + player.getAttackPower());
        itemsLabel.setText("Itens: " + player.getItemsCollected());

        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        boolean moved = false;
        float speed = 100 * delta;
        float dx = 0, dy = 0;

        if (player.isDead()) {
            player.setState(Character.State.DEATH);
            player.setAnimating(true);
            camera.position.set(player.getPosition(), 0);
            camera.update();

            mapRenderer.setView(camera);
            mapRenderer.render();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            player.render(batch);

            for (Bullet bullet : bullets) {
                bullet.update(delta);
                bullet.render(batch);
            }

            batch.end();

            stage.act(Math.min(delta, 1 / 30f));
            stage.draw();
            return;
        }

        // Movimento via teclado (WASD)
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += speed;
            moved = true;
            cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= speed;
            moved = true;
            cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= speed;
            moved = true;
            cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += speed;
            moved = true;
            cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
        }

        // Movimento via joystick
        if (joystick.isMoving()) {
            Vector2 direction = joystick.getDirection();  // Obtém a direção do joystick
            dx = direction.x * speed;  // Calcula o deslocamento com base na direção do joystick
            dy = direction.y * speed;
            moved = true;
            cameraIconImage.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture("camera_off.png"))));
        }
        if (player.getState() != Character.State.ATTACK) {
            if (shift) {
                player.setState(Character.State.CRAWL_MOVE);
                if (moved) {
                    player.updatePosition(dx, dy, shift);
                    player.setAnimating(true);
                    cameraFollow = true;
                } else {
                    player.setAnimating(false);
                }
            } else if (moved) {
                player.setState(Character.State.WALK);
                player.updatePosition(dx, dy, shift);
                player.setAnimating(true);
                cameraFollow = true;
            } else {
                player.setState(Character.State.IDLE);
                player.setAnimating(true);
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                player.jump();

            }


        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isPaused) {
            setPaused(true);
            game.setScreen(new PauseScreen(game, this));
        }

        if (cameraControlEnabled) {
            float camSpeed = 5f * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) camera.position.x -= camSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camera.position.x += camSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) camera.position.y += camSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) camera.position.y -= camSpeed;
        }
        if (cameraFollow) camera.position.set(player.getPosition(), 0);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
        for (Item item : items) {
            item.render(batch);
        }

        for (Bullet bullet : bullets) {
            bullet.render(batch);
        }
        player.render(batch);
        batch.end();
// barras de vida fora do SpriteBatch
        for (Enemy enemy : enemies) {
            if (enemy.wasHurt() && !enemy.isDead()) {
                drawHealthBar(batch, enemy);
            }
        }
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        batch.dispose();
        map.dispose();
        shapeRenderer.dispose();
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

}
