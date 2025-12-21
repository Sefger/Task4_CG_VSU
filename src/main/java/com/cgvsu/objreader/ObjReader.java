package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();
		// Используем более быстрый метод разделения строк, чем Scanner
		String[] lines = fileContent.split("\\r?\\n");

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			// Используем простой split по пробелам
			String[] words = line.split("\\s+");
			String token = words[0];

			switch (token) {
				case OBJ_VERTEX_TOKEN -> result.addVertex(parseVertex(words, i + 1));
				case OBJ_TEXTURE_TOKEN -> result.addTextureVertex(parseTextureVertex(words, i + 1));
				case OBJ_NORMAL_TOKEN -> result.addNormal(parseNormal(words, i + 1));
				case OBJ_FACE_TOKEN -> result.addPolygon(parseFace(words, i + 1));
			}
		}
		return result;
	}

	protected static Vector3f parseVertex(String[] words, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(words[1]),
					Float.parseFloat(words[2]),
					Float.parseFloat(words[3]));
		} catch (Exception e) {
			throw new ObjReaderException("Error parsing vertex", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(String[] words, int lineInd) {
		try {
			return new Vector2f(
					Float.parseFloat(words[1]),
					Float.parseFloat(words[2]));
		} catch (Exception e) {
			throw new ObjReaderException("Error parsing texture vertex", lineInd);
		}
	}

	protected static Vector3f parseNormal(String[] words, int lineInd) {
		try {
			return new Vector3f(
					Float.parseFloat(words[1]),
					Float.parseFloat(words[2]),
					Float.parseFloat(words[3]));
		} catch (Exception e) {
			throw new ObjReaderException("Error parsing normal", lineInd);
		}
	}

	protected static Polygon parseFace(String[] words, int lineInd) {
		int n = words.length - 1; // Кол-во вершин в полигоне

		// Временные списки (к сожалению, пока не знаем размер индексов внутри слова)
		List<Integer> vList = new ArrayList<>(n);
		List<Integer> tList = new ArrayList<>(n);
		List<Integer> nList = new ArrayList<>(n);

		for (int i = 1; i <= n; i++) {
			parseFaceWord(words[i], vList, tList, nList, lineInd);
		}

		// Конвертируем временные списки в быстрые примитивные массивы
		Polygon result = new Polygon(vList.size());
		result.setVertexIndices(toIntArray(vList));

		if (!tList.isEmpty()) {
			result.setTextureVertexIndices(toIntArray(tList));
		}
		if (!nList.isEmpty()) {
			result.setNormalIndices(toIntArray(nList));
		}

		return result;
	}

	protected static void parseFaceWord(
			String word,
			List<Integer> vList,
			List<Integer> tList,
			List<Integer> nList,
			int lineInd) {
		try {
			String[] parts = word.split("/", -1); // -1 сохраняет пустые строки типа 1//3

			// Вершина (v) всегда есть
			vList.add(Integer.parseInt(parts[0]) - 1);

			if (parts.length > 1 && !parts[1].isEmpty()) {
				tList.add(Integer.parseInt(parts[1]) - 1);
			}

			if (parts.length > 2 && !parts[2].isEmpty()) {
				nList.add(Integer.parseInt(parts[2]) - 1);
			}
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Invalid index format", lineInd);
		}
	}

	private static int[] toIntArray(List<Integer> list) {
		return list.stream().mapToInt(Integer::intValue).toArray();
	}
}