package com.cgvsu.affine;

import com.cgvsu.math.Matrix4x4;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.cgvsu.AffineTransformation;
import com.cgvsu.model.Model;

public class AffineTransformationTest {

    @Test
    void testScale() {
        Matrix4x4 scale = AffineTransformation.scale(2.0f, 3.0f, 4.0f);
        assertEquals(2.0f, scale.get(0, 0));
        assertEquals(3.0f, scale.get(1, 1));
        assertEquals(4.0f, scale.get(2, 2));
        assertEquals(1.0f, scale.get(3, 3));
    }

    @Test
    void testRotationX() {
        float angle = (float) (Math.PI / 2);
        Matrix4x4 rotation = AffineTransformation.rotationX(angle);

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        assertEquals(1.0f, rotation.get(0, 0), 0.0001f);
        assertEquals(0.0f, rotation.get(0, 1), 0.0001f);
        assertEquals(0.0f, rotation.get(1, 0), 0.0001f);
        assertEquals(cos, rotation.get(1, 1), 0.0001f);
        assertEquals(sin, rotation.get(1, 2), 0.0001f);
        assertEquals(-sin, rotation.get(2, 1), 0.0001f);
        assertEquals(cos, rotation.get(2, 2), 0.0001f);
    }

    @Test
    void testRotationY() {
        float angle = (float) (Math.PI / 2);
        Matrix4x4 rotation = AffineTransformation.rotationY(angle);

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        assertEquals(cos, rotation.get(0, 0), 0.0001f);
        assertEquals(0.0f, rotation.get(0, 1), 0.0001f);
        assertEquals(sin, rotation.get(0, 2), 0.0001f);
        assertEquals(0.0f, rotation.get(1, 0), 0.0001f);
        assertEquals(1.0f, rotation.get(1, 1), 0.0001f);
        assertEquals(0.0f, rotation.get(1, 2), 0.0001f);
        assertEquals(-sin, rotation.get(2, 0), 0.0001f);
        assertEquals(0.0f, rotation.get(2, 1), 0.0001f);
        assertEquals(cos, rotation.get(2, 2), 0.0001f);
    }

    @Test
    void testRotationZ() {
        float angle = (float) (Math.PI / 2);
        Matrix4x4 rotation = AffineTransformation.rotationZ(angle);

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        assertEquals(cos, rotation.get(0, 0), 0.0001f);
        assertEquals(sin, rotation.get(0, 1), 0.0001f);
        assertEquals(0.0f, rotation.get(0, 2), 0.0001f);
        assertEquals(-sin, rotation.get(1, 0), 0.0001f);
        assertEquals(cos, rotation.get(1, 1), 0.0001f);
        assertEquals(0.0f, rotation.get(1, 2), 0.0001f);
        assertEquals(0.0f, rotation.get(2, 0), 0.0001f);
        assertEquals(0.0f, rotation.get(2, 1), 0.0001f);
        assertEquals(1.0f, rotation.get(2, 2), 0.0001f);
    }

    @Test
    void testTranslation() {
        Matrix4x4 translation = AffineTransformation.translation(5.0f, 6.0f, 7.0f);
        assertEquals(1.0f, translation.get(0, 0));
        assertEquals(1.0f, translation.get(1, 1));
        assertEquals(1.0f, translation.get(2, 2));
        assertEquals(5.0f, translation.get(0, 3));
        assertEquals(6.0f, translation.get(1, 3));
        assertEquals(7.0f, translation.get(2, 3));
        assertEquals(1.0f, translation.get(3, 3));
    }

    @Test
    void testCombine() {
        Matrix4x4 scale = AffineTransformation.scale(2.0f, 2.0f, 2.0f);
        Matrix4x4 translate = AffineTransformation.translation(3.0f, 4.0f, 5.0f);

        Matrix4x4 combined = AffineTransformation.combine(translate, scale);

        assertEquals(2.0f, combined.get(0, 0));
        assertEquals(2.0f, combined.get(1, 1));
        assertEquals(2.0f, combined.get(2, 2));
        assertEquals(3.0f, combined.get(0, 3));
        assertEquals(4.0f, combined.get(1, 3));
        assertEquals(5.0f, combined.get(2, 3));
    }

    @Test
    void testCombineSingleMatrix() {
        Matrix4x4 identity = Matrix4x4.identity();
        Matrix4x4 result = AffineTransformation.combine(identity);
        assertTrue(identity.equals(result));
    }

    @Test
    void testCombineEmpty() {
        Matrix4x4 result = AffineTransformation.combine();
        assertTrue(Matrix4x4.identity().equals(result));
    }

    @Test
    void testCreateModelMatrix() {
        Matrix4x4 modelMatrix = AffineTransformation.createModelMatrix(
                1.0f, 2.0f, 3.0f,
                0.0f, 0.0f, 0.0f,
                2.0f, 3.0f, 4.0f
        );

        assertNotNull(modelMatrix);
        assertEquals(2.0f, modelMatrix.get(0, 0));
        assertEquals(3.0f, modelMatrix.get(1, 1));
        assertEquals(4.0f, modelMatrix.get(2, 2));
    }

    @Test
    void testTransformation() {
        Model model = new Model();
        model.setModelMatrix(Matrix4x4.identity());

        AffineTransformation.transformation(
                model,
                1.0f, 2.0f, 3.0f,
                90.0f, 0.0f, 0.0f,
                2.0f, 2.0f, 2.0f
        );

        Matrix4x4 result = model.getModelMatrix();
        assertNotNull(result);
        assertFalse(Matrix4x4.identity().equals(result));
    }

    @Test
    void testTransformationNullModel() {
        AffineTransformation.transformation(null, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    void testRandomTransformation() {
        Model model = new Model();
        Matrix4x4 initialMatrix = Matrix4x4.identity();
        model.setModelMatrix(initialMatrix);

        AffineTransformation.randomTransformation(model);

        Matrix4x4 result = model.getModelMatrix();
        assertNotNull(result);
        assertFalse(initialMatrix.equals(result));
    }
}