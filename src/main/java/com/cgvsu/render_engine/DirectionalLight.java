package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;

public class DirectionalLight extends Light {
    private final Vector3f normalizedDirection;

    public DirectionalLight(Vector3f direction, float intensity) {
        super(direction.normalized(), intensity);
        this.normalizedDirection = direction.multiply(-1).normalized();
    }

    @Override
    public void getDirectionTo(Vector3f vertexPos, Vector3f dest) {
        dest.x = normalizedDirection.x;
        dest.y = normalizedDirection.y;
        dest.z = normalizedDirection.z;
    }
}

