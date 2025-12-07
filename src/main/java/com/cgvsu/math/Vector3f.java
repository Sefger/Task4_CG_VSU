package com.cgvsu.math;

// Это заготовка для собственной библиотеки для работы с линейной алгеброй
public class Vector3f {
    private static final float eps = 1e-7f;
    public float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Vector3f other) {
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps && Math.abs(z - other.z) < eps;
    }

    public float getY() {
        return this.y;
    }

    public float getX() {
        return this.x;
    }
    public float getZ(){
        return this.z;
    }
    public void setX(float x){
        this.x = x;
    }
    public void setY(float y){
        this.y = y;
    }
    public void setZ(float z){
        this.z = z;
    }

}
