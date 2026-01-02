package com.cgvsu.render_engine;


import com.cgvsu.math.Vector3f;


public class PointLight extends Light {
    public PointLight(Vector3f position, float intensity) {
        super(position, intensity);
    }

    @Override
    public void getDirectionTo(Vector3f vertexPos, Vector3f dest) {

        float dx = position.x - vertexPos.x;
        float dy = position.y - vertexPos.y;
        float dz = position.z - vertexPos.z;

        float lenSq = dx * dx + dy * dy + dz * dz;
        if (lenSq< 0.00000f){
            dest.set(0,0,0);
            return;
        }
        float invLen = 1.0f/(float) Math.sqrt(lenSq);
        dest.x = dx*invLen;
        dest.y = dy*invLen;
        dest.z = dz*invLen;
    }
}
