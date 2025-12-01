package edu.sitm.master;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Partitioner cuenta líneas de un CSV sin cargar todo en memoria y genera
 * rangos (startLine..endLine) para cada partición.
 */
public class Partitioner {

    public List<int[]> partitionRanges(String csvPath, int numPartitions) throws IOException {
        long total = countLines(csvPath);
        if (total == 0) return new ArrayList<>();

        int per = (int) Math.max(1, total / numPartitions);
        List<int[]> out = new ArrayList<>();

        int start = 1;
        for (int i = 0; i < numPartitions; i++) {
            int end = (i == numPartitions - 1) ? (int) total : (start + per - 1);
            if (end > total) end = (int) total;
            if (start > end) break;
            out.add(new int[]{start, end});
            start = end + 1;
        }
        return out;
    }

    public long countLines(String csvPath) throws IOException {
        long lines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            while (br.readLine() != null) lines++;
        }
        return lines;
    }
}
