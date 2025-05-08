package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class AnimationComponent implements Component {
    // Mapa que associa os estados da animação aos seus respectivos tipos de animação
    public ObjectMap<StateComponent.State, Animation<TextureRegion>> animations = new ObjectMap<>();
    public float stateTime = 0f; // Tempo de execução da animação
    public TextureRegion currentFrame; // O quadro atual da animação
    public boolean facingRight = true; //

    // Método para inicializar as animações (usado no momento da criação do componente)
    public void init() {
        // Carregar as animações diretamente, como na classe Character
        animations.put(StateComponent.State.IDLE, loadAnimation("armysoldier/Idle", 2, 0.2f));
        animations.put(StateComponent.State.WALK, loadAnimation("armysoldier/Walk", 7, 0.1f));
        animations.put(StateComponent.State.CLIMB, loadAnimation("armysoldier/Climb", 4, 0.1f));
        animations.put(StateComponent.State.FAST_MOVE, loadAnimation("armysoldier/fastMove", 2, 0.1f));
        animations.put(StateComponent.State.JUMP, loadAnimation("armysoldier/Jump", 2, 0.3f));
        animations.put(StateComponent.State.FALL, loadAnimation("armysoldier/Fall", 1, 0.15f));
        animations.put(StateComponent.State.DEATH, loadAnimation("armysoldier/Death", 3, 0.15f));
        animations.put(StateComponent.State.HURT, loadAnimation("armysoldier/Hurt", 2, 0.12f));
        animations.put(StateComponent.State.ATTACK, loadAnimation("armysoldier/Attack", 6, 0.1f));
        animations.put(StateComponent.State.SUPER_ATTACK, loadAnimation("armysoldier/SuperAttack", 3, 0.1f));
        animations.put(StateComponent.State.DEFENSE, loadAnimation("armysoldier/Defense", 4, 0.1f));
        animations.put(StateComponent.State.WALK_INJURED, loadAnimation("armysoldier/WalkInjured", 5, 0.1f));
        animations.put(StateComponent.State.IDLE_INJURED, loadAnimation("armysoldier/IdleInjured", 2, 0.2f));
        animations.put(StateComponent.State.DEFENSE_INJURED, loadAnimation("armysoldier/DefenseInjured", 4, 0.1f));
    }


    // Carregar animações a partir de arquivos de imagem
    private Animation<TextureRegion> loadAnimation(String basePath, int frameCount, float frameDuration) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            String path = basePath + String.format("%04d.png", i);
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                Texture tex = new Texture(file);
                frames.add(new TextureRegion(tex));
            }
        }
        if (frames.isEmpty()) {
            return null;
        }
        System.out.println("Falha ao carregar animação: " + basePath);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        if (basePath.contains("Death") || basePath.contains("Hurt") || basePath.contains("SuperAttack") ||
                basePath.contains("fastMove") || basePath.contains("Defense") || basePath.contains("DefenseInjured")) {
            anim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }


        return anim;


    }


    public void updateFrame(StateComponent state) {
        // Pega a animação associada ao estado atual
        Animation<TextureRegion> animation = animations.get(state.currentState);

        // Verifica se a animação existe
        if (animation != null) {
            if (facingRight) {
                // Se estiver virado para a direita, apenas pega o quadro normal
                currentFrame = animation.getKeyFrame(stateTime, true);
            } else {
                // Se estiver virado para a esquerda, espelha a animação
                TextureRegion flipped = new TextureRegion(animation.getKeyFrame(stateTime, true));
                flipped.flip(true, false); // Espelha horizontalmente
                currentFrame = flipped;
            }
        }
    }

}
