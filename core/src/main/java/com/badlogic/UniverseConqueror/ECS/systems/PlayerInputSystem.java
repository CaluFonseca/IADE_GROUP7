package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.*;
import com.badlogic.UniverseConqueror.Utils.Constants;
import com.badlogic.UniverseConqueror.Utils.Joystick;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class PlayerInputSystem extends IteratingSystem {
   /// private final ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private final ComponentMapper<StateComponent> sm = ComponentMapper.getFor(StateComponent.class);
    private final ComponentMapper<PhysicsComponent> phm = ComponentMapper.getFor(PhysicsComponent.class);
    private final ComponentMapper<JumpComponent> jumpMapper = ComponentMapper.getFor(JumpComponent.class);
    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<AnimationComponent> am = ComponentMapper.getFor(AnimationComponent.class);
    private final ComponentMapper<AttackComponent> acm = ComponentMapper.getFor(AttackComponent.class);

    private final World world;
    private final Joystick joystick;



    public PlayerInputSystem(World world, Joystick joystick) {
        super(Family.all(PlayerComponent.class,  AttackComponent.class, VelocityComponent.class, PhysicsComponent.class, PositionComponent.class, StateComponent.class, AnimationComponent.class).get());
        this.world = world;
        this.joystick = joystick;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
     //   PositionComponent pos = pm.get(entity);
        StateComponent state = sm.get(entity);
        PhysicsComponent physics = phm.get(entity);
        VelocityComponent velocity = vm.get(entity);
        JumpComponent jump = jumpMapper.get(entity);
        AnimationComponent animation = am.get(entity);
        AttackComponent attack = acm.get(entity);

        float dx = 0f, dy = 0f;
        boolean isMoving = false;

        // Verifica se o Shift está pressionado
        boolean isShiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        float currentSpeed = isShiftPressed ? Constants.SPRINT_SPEED : Constants.SPEED;

        // Verifica se há input de teclado
        boolean keyPressed = false;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= currentSpeed;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += currentSpeed;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            dy += currentSpeed;
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            dy -= currentSpeed;
            keyPressed = true;
        }

        // Se nenhuma tecla pressionada, usa joystick
        if (!keyPressed && joystick != null && joystick.isMoving()) {
            Vector2 dir = joystick.getDirection(); // vetor normalizado
            dx = dir.x * currentSpeed;
            dy = dir.y * currentSpeed;
        }

        isMoving = dx != 0 || dy != 0;

        // Atualiza a velocidade
        velocity.velocity.set(dx * Constants.PHYSICS_MULTIPLIER, dy * Constants.PHYSICS_MULTIPLIER);
        System.out.println("STATE ATUAL: " + state.get());
        if (state.get() == StateComponent.State.HURT || state.get() == StateComponent.State.DEATH) {
           // velocity.velocity.setZero(); // opcional: para não mover durante HURT
            return; // ignora input se estiver machucado ou morto
        }

        // Super ataque
        if (state.superAttackTimeLeft > 0) {
            state.superAttackTimeLeft -= deltaTime;
            state.set(StateComponent.State.SUPER_ATTACK);
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            state.set(StateComponent.State.SUPER_ATTACK);
            state.superAttackTimeLeft = 2.0f;
            return;
        }

        // Defesa
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            state.set(StateComponent.State.DEFENSE);
            velocity.velocity.setZero();
            if (physics.body != null) {
                physics.body.setLinearVelocity(0, 0);
            }
            return;
        }

        // Pulo
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (jump.canJump) {
                jump.isJumping = true;
                state.set(StateComponent.State.JUMP);
                physics.body.applyForceToCenter(new Vector2(0, Constants.JUMP_FORCE), true);
                return;
            }
        }

        // Define estado com base no movimento
        if (isMoving && isShiftPressed) {
            state.set(StateComponent.State.FAST_MOVE);
        } else if (isMoving) {
            state.set(StateComponent.State.WALK);}
//        } else {
//            state.set(StateComponent.State.IDLE);
//        }

        // Ataque com botão direito do rato
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            if (attack.canAttack()) {
                attack.startAttack();
                state.set(StateComponent.State.ATTACK);
                return; // ← evita movimentar enquanto ataca
            }
        }

        // Atualiza direção do sprite
        if (dx > 0) {
            animation.facingRight = true;
        } else if (dx < 0) {
            animation.facingRight = false;
        }
    }
}

