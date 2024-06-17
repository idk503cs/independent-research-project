package uk.ac.york.idk503.performancetest.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.PerformanceTestRunner;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Component
public class CompletableFutureMergeSort implements Sortable, Testable {
    private static final Logger LOG = LoggerFactory.getLogger(CompletableFutureMergeSort.class);
    private static final Executor pool = new ForkJoinPool(50);

    public int[] sort(final int[] data) {
        if (data.length <= 1) {
            return data;
        }

        final int middle = data.length / 2;
        final int[] left = java.util.Arrays.copyOfRange(data, 0, middle);
        final int[] right = java.util.Arrays.copyOfRange(data, middle, data.length);

        try {

            final CompletableFuture<int[]> mergeFuture = CompletableFuture
                    .supplyAsync(() -> merge(sort(left), sort(right)), pool);

            return mergeFuture.get();

        } catch (InterruptedException e) {
            LOG.error("Completable future merge sort failure - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return new int[0];
        } catch (Exception e) {
            LOG.error("Completable future merge sort failure - {}", e.getMessage());
            return new int[0];
        }
    }

    private static int[] merge(final int[] left, final int[] right) {
        final int[] merged = new int[left.length + right.length];
        int leftIdx = 0;
        int rightIdx = 0;
        int mergeIdx = 0;

        while (leftIdx < left.length && rightIdx < right.length) {
            merged[mergeIdx++] = left[leftIdx] <= right[rightIdx] ? left[leftIdx++] : right[rightIdx++];
        }

        while (leftIdx < left.length) {
            merged[mergeIdx++] = left[leftIdx++];
        }
        while (rightIdx < right.length) {
            merged[mergeIdx++] = right[rightIdx++];
        }
        return merged;
    }

    public boolean test() {
        try {
            final var randomData = PerformanceTestRunner.getRandomArray();
            final var sortedData = new CompletableFutureMergeSort().sort(randomData);

            Arrays.sort(randomData);
            return Arrays.equals(sortedData, randomData);
        } catch (Exception e) {
            LOG.error("CompletableFutureMergeSort failure");
            return false;
        }
    }
}