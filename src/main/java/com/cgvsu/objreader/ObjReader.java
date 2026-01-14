package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			switch (token) {
				case OBJ_VERTEX_TOKEN -> result.getVertices().add(parseVertex(wordsInLine, lineInd));
				case OBJ_TEXTURE_TOKEN -> result.getTextureVertices().add(parseTextureVertex(wordsInLine, lineInd));
				case OBJ_NORMAL_TOKEN -> result.getNormals().add(parseNormal(wordsInLine, lineInd));
				case OBJ_FACE_TOKEN -> result.getPolygons().add(parseFace(wordsInLine, lineInd));
				default -> {}
			}
		}

		return result;
	}

	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		int vertexCount = wordsInLineWithoutToken.size();

		if (vertexCount < 3) {
			throw new ObjReaderException("Polygon must have at least 3 vertices.", lineInd);
		}

		// Создаем полигон с известным размером
		Polygon polygon = new Polygon(vertexCount);

		// Подготовим массивы
		int[] vertexIndices = new int[vertexCount];
		int[] textureIndices = new int[vertexCount];
		int[] normalIndices = new int[vertexCount];

		// Флаги наличия данных
		boolean hasTextures = false;
		boolean hasNormals = false;

		// Парсим каждое слово
		for (int i = 0; i < vertexCount; i++) {
			String word = wordsInLineWithoutToken.get(i);
			int[] indices = parseFaceWord(word, lineInd);

			// Вершина всегда должна быть
			vertexIndices[i] = indices[0];

			// Текстурные координаты (могут отсутствовать)
			if (indices[1] != -1) {
				textureIndices[i] = indices[1];
				hasTextures = true;
			} else {
				textureIndices[i] = -1; // маркер отсутствия
			}

			// Нормали (могут отсутствовать)
			if (indices[2] != -1) {
				normalIndices[i] = indices[2];
				hasNormals = true;
			} else {
				normalIndices[i] = -1; // маркер отсутствия
			}
		}

		// Устанавливаем индексы вершин (всегда есть)
		polygon.setVertexIndices(vertexIndices);

		// Устанавливаем текстурные координаты (если есть)
		if (hasTextures) {
			// Проверяем, что все вершины имеют текстурные координаты
			boolean allHaveTextures = true;
			for (int i = 0; i < vertexCount; i++) {
				if (textureIndices[i] == -1) {
					allHaveTextures = false;
					break;
				}
			}

			if (allHaveTextures) {
				polygon.setTextureVertexIndices(textureIndices);
			} else {
				// Если не все вершины имеют текстурные координаты, создаем пустой массив
				polygon.setTextureVertexIndices(new int[0]);
			}
		} else {
			polygon.setTextureVertexIndices(new int[0]);
		}

		// Устанавливаем нормали (если есть)
		if (hasNormals) {
			// Проверяем, что все вершины имеют нормали
			boolean allHaveNormals = true;
			for (int i = 0; i < vertexCount; i++) {
				if (normalIndices[i] == -1) {
					allHaveNormals = false;
					break;
				}
			}

			if (allHaveNormals) {
				polygon.setNormalIndices(normalIndices);
			} else {
				// Если не все вершины имеют нормали, создаем пустой массив
				polygon.setNormalIndices(new int[0]);
			}
		} else {
			polygon.setNormalIndices(new int[0]);
		}

		return polygon;
	}

	// Возвращает массив из 3 элементов: [vertexIndex, textureIndex, normalIndex]
	// -1 означает отсутствие индекса
	protected static int[] parseFaceWord(String wordInLine, int lineInd) {
		try {
			String[] wordIndices = wordInLine.split("/");
			int[] result = new int[3];
			// Инициализируем -1 (отсутствие индекса)
			result[0] = -1;
			result[1] = -1;
			result[2] = -1;

			switch (wordIndices.length) {
				case 1 -> {
					// f v1 v2 v3
					result[0] = Integer.parseInt(wordIndices[0]) - 1;
				}
				case 2 -> {
					// f v1/vt1 v2/vt2 v3/vt3
					result[0] = Integer.parseInt(wordIndices[0]) - 1;
					result[1] = Integer.parseInt(wordIndices[1]) - 1;
				}
				case 3 -> {
					// f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
					//  f v1//vn1 v2//vn2 v3//vn3
					result[0] = Integer.parseInt(wordIndices[0]) - 1;

					if (!wordIndices[1].isEmpty()) {
						result[1] = Integer.parseInt(wordIndices[1]) - 1;
					}

					if (!wordIndices[2].isEmpty()) {
						result[2] = Integer.parseInt(wordIndices[2]) - 1;
					}
				}
				default ->
						throw new ObjReaderException("Invalid element size.", lineInd);
			}

			return result;

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}
}