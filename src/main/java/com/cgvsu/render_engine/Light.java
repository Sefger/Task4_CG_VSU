package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;

public abstract class Light {
    protected Vector3f position;
    protected float intensity;

    public Light(Vector3f position, float intensity) {
        this.position = position;
        this.intensity = intensity;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public abstract void getDirectionTo(Vector3f vertexPos, Vector3f dest);

    public float getIntensity() {
        return this.intensity;
    }
}

// направленный свет (как солнце)
