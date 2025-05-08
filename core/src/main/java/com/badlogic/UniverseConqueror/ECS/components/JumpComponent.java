package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;

public class JumpComponent implements Component {
    public boolean isJumping = false;  // Indica se o personagem está no ar
    public float jumpForce = 500f;     // Força de pulo
    public boolean canJump = true;     // Controle para impedir múltiplos pulos no ar (ex.: double jump)
    public float jumpDuration = 0f;
    public float currentJumpTime = 0f; // Tempo de duração do pulo
    public float maxJumpTime = 0.5f;   // Tempo máximo que o pulo pode durar
    public float groundY = 0f;         // Posição do chão (onde o pulo deve terminar)
}
