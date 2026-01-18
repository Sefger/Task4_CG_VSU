package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Matrix4x4;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    @Test
    void testConstructor() {
        Camera camera = new Camera(
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(4.0f, 5.0f, 6.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        assertEquals(1.0f, camera.getPosition().x);
        assertEquals(2.0f, camera.getPosition().y);
        assertEquals(3.0f, camera.getPosition().z);
        assertEquals(4.0f, camera.getTarget().x);
        assertEquals(5.0f, camera.getTarget().y);
        assertEquals(6.0f, camera.getTarget().z);
    }

    @Test
    void testSetPosition() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        camera.setPosition(new Vector3f(10.0f, 20.0f, 30.0f));
        assertEquals(10.0f, camera.getPosition().x);
        assertEquals(20.0f, camera.getPosition().y);
        assertEquals(30.0f, camera.getPosition().z);
    }

    @Test
    void testSetTarget() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        camera.setTarget(new Vector3f(15.0f, 25.0f, 35.0f));
        assertEquals(15.0f, camera.getTarget().x);
        assertEquals(25.0f, camera.getTarget().y);
        assertEquals(35.0f, camera.getTarget().z);
    }

    @Test
    void testSetAspectRatio() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 0),
                new Vector3f(0, 0, 0),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        camera.setAspectRatio(1.777f);
    }

    @Test
    void testMovePosition() {
        Camera camera = new Camera(
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(0, 0, 0),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        camera.movePosition(new Vector3f(5.0f, 6.0f, 7.0f));
        assertEquals(6.0f, camera.getPosition().x);
        assertEquals(8.0f, camera.getPosition().y);
        assertEquals(10.0f, camera.getPosition().z);
    }

    @Test
    void testMoveTarget() {
        Camera camera = new Camera(
                new Vector3f(0, 0, 0),
                new Vector3f(1.0f, 2.0f, 3.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        camera.moveTarget(new Vector3f(4.0f, 5.0f, 6.0f));
        assertEquals(5.0f, camera.getTarget().x);
        assertEquals(7.0f, camera.getTarget().y);
        assertEquals(9.0f, camera.getTarget().z);
    }

    @Test
    void testRotateAroundPoint() {
        Camera camera = new Camera(
                new Vector3f(10.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        Vector3f point = new Vector3f(0.0f, 0.0f, 0.0f);
        camera.rotateAroundPoint(point, 90.0f, 0.0f);

        assertNotNull(camera.getPosition());
        assertNotNull(camera.getTarget());
    }

    @Test
    void testZoom() {
        Camera camera = new Camera(
                new Vector3f(0.0f, 0.0f, 10.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        Vector3f initialPosition = new Vector3f(
                camera.getPosition().x,
                camera.getPosition().y,
                camera.getPosition().z
        );

        camera.zoom(5.0f);

        assertNotEquals(initialPosition.z, camera.getPosition().z);
    }

    @Test
    void testMove() {
        Camera camera = new Camera(
                new Vector3f(1.0f, 2.0f, 3.0f),
                new Vector3f(4.0f, 5.0f, 6.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        camera.move(new Vector3f(10.0f, 20.0f, 30.0f));

        assertEquals(11.0f, camera.getPosition().x);
        assertEquals(22.0f, camera.getPosition().y);
        assertEquals(33.0f, camera.getPosition().z);

        assertEquals(14.0f, camera.getTarget().x);
        assertEquals(25.0f, camera.getTarget().y);
        assertEquals(36.0f, camera.getTarget().z);
    }

    @Test
    void testGetViewMatrix() {
        Camera camera = new Camera(
                new Vector3f(0.0f, 0.0f, 5.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        Matrix4x4 viewMatrix = camera.getViewMatrix();
        assertNotNull(viewMatrix);
    }

    @Test
    void testGetProjectionMatrix() {
        Camera camera = new Camera(
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 0.0f, 0.0f),
                60.0f, 1.0f, 0.1f, 1000.0f
        );

        Matrix4x4 projectionMatrix = camera.getProjectionMatrix();
        assertNotNull(projectionMatrix);
    }
}