package com.nibm.pdsa.games.sixteenqueens.algorithm;

import com.nibm.pdsa.games.sixteenqueens.model.QueensSolveResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class BitmaskBacktrackingSolver {

    public QueensSolveResult solveSequential(int n, int sampleLimit) {
        validateBoardSize(n);
        long start = System.nanoTime();

        long mask = (1L << n) - 1L;
        AtomicLong count = new AtomicLong(0);
        List<String> samples = new ArrayList<>();
        int[] positions = new int[n];
        Arrays.fill(positions, -1);

        dfsSequential(n, 0, 0L, 0L, 0L, mask, positions, sampleLimit, count, samples);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new QueensSolveResult(count.get(), elapsedMs, samples);
    }

    public QueensSolveResult solveParallel(int n, int threadCount, int sampleLimit) {
        validateBoardSize(n);
        if (threadCount < 1) {
            throw new IllegalArgumentException("threadCount must be at least 1");
        }

        long start = System.nanoTime();
        long mask = (1L << n) - 1L;
        AtomicLong count = new AtomicLong(0);
        ConcurrentLinkedQueue<String> samples = new ConcurrentLinkedQueue<>();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        long available = mask;
        while (available != 0) {
            long bit = available & -available;
            available -= bit;
            final long firstBit = bit;

            futures.add(executor.submit(() -> {
                int[] positions = new int[n];
                Arrays.fill(positions, -1);
                positions[0] = Long.numberOfTrailingZeros(firstBit);
                dfsParallel(n, 1, firstBit, firstBit << 1, firstBit >> 1, mask, positions, sampleLimit, count, samples);
            }));
        }

        waitForAll(futures);
        executor.shutdown();

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        return new QueensSolveResult(count.get(), elapsedMs, new ArrayList<>(samples));
    }

    public boolean isValidSolution(int n, int[] positions) {
        validateBoardSize(n);
        if (positions == null || positions.length != n) {
            return false;
        }

        boolean[] usedCols = new boolean[n];
        boolean[] usedMainDiag = new boolean[(2 * n) - 1];
        boolean[] usedAntiDiag = new boolean[(2 * n) - 1];

        for (int row = 0; row < n; row++) {
            int col = positions[row];
            if (col < 0 || col >= n) {
                return false;
            }
            int main = row - col + (n - 1);
            int anti = row + col;
            if (usedCols[col] || usedMainDiag[main] || usedAntiDiag[anti]) {
                return false;
            }
            usedCols[col] = true;
            usedMainDiag[main] = true;
            usedAntiDiag[anti] = true;
        }

        return true;
    }

    public int[] parseAnswer(String answer, int n) {
        validateBoardSize(n);
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer must not be blank");
        }

        String[] parts = answer.split(",");
        if (parts.length != n) {
            throw new IllegalArgumentException("Answer must contain exactly " + n + " comma-separated integers");
        }

        int[] positions = new int[n];
        for (int i = 0; i < n; i++) {
            try {
                positions[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Answer contains non-numeric value at index " + i);
            }
        }
        return positions;
    }

    private void dfsSequential(int n,
                               int row,
                               long cols,
                               long mainDiag,
                               long antiDiag,
                               long mask,
                               int[] positions,
                               int sampleLimit,
                               AtomicLong count,
                               List<String> samples) {
        if (row == n) {
            count.incrementAndGet();
            if (samples.size() < sampleLimit) {
                samples.add(formatSolution(positions));
            }
            return;
        }

        long available = mask & ~(cols | mainDiag | antiDiag);
        while (available != 0) {
            long bit = available & -available;
            available -= bit;
            positions[row] = Long.numberOfTrailingZeros(bit);

            dfsSequential(n,
                    row + 1,
                    cols | bit,
                    ((mainDiag | bit) << 1) & mask,
                    (antiDiag | bit) >> 1,
                    mask,
                    positions,
                    sampleLimit,
                    count,
                    samples);
        }
    }

    private void dfsParallel(int n,
                             int row,
                             long cols,
                             long mainDiag,
                             long antiDiag,
                             long mask,
                             int[] positions,
                             int sampleLimit,
                             AtomicLong count,
                             ConcurrentLinkedQueue<String> samples) {
        if (row == n) {
            count.incrementAndGet();
            if (samples.size() < sampleLimit) {
                samples.offer(formatSolution(positions));
            }
            return;
        }

        long available = mask & ~(cols | mainDiag | antiDiag);
        while (available != 0) {
            long bit = available & -available;
            available -= bit;
            int[] next = Arrays.copyOf(positions, n);
            next[row] = Long.numberOfTrailingZeros(bit);

            dfsParallel(n,
                    row + 1,
                    cols | bit,
                    ((mainDiag | bit) << 1) & mask,
                    (antiDiag | bit) >> 1,
                    mask,
                    next,
                    sampleLimit,
                    count,
                    samples);
        }
    }

    private String formatSolution(int[] positions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < positions.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(positions[i]);
        }
        return sb.toString();
    }

    private void validateBoardSize(int n) {
        if (n < 8 || n > 16) {
            throw new IllegalArgumentException("Board size must be between 8 and 16");
        }
    }

    private void waitForAll(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parallel solver interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Parallel solver failed", e);
            }
        }
    }
}
