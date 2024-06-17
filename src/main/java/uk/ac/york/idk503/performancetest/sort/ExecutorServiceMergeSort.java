package uk.ac.york.idk503.performancetest.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.PerformanceTestRunner;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class ExecutorServiceMergeSort implements Sortable, Testable {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceMergeSort.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public int[] sort(final int[] data) {
        if (data.length < 2) {
            return data;
        }

        final int middle = data.length / 2;
        final int[] left = java.util.Arrays.copyOfRange(data, 0, middle);
        final int[] right = java.util.Arrays.copyOfRange(data, middle, data.length);

        final var merge = executorService.submit(() -> merge(sort(left), sort(right)));

        try {
            return merge.get();
        } catch (InterruptedException e) {
            LOG.error("Executor service merge sort failure - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return new int[0];
        } catch (Exception e) {
            LOG.error("Executor service merge sort failure - {}", e.getMessage());
            return new int[0];
        }
    }

    private int[] merge(final int[] left, final int[] right) {
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

    public void shutdown() {
        executorService.shutdown();
    }

    public boolean test() {
        final var executorServiceMergeSort = new ExecutorServiceMergeSort();
        try {
            final var randomData = PerformanceTestRunner.getRandomArray();
            final var sortedData = executorServiceMergeSort.sort(randomData);

            Arrays.sort(randomData);
            return Arrays.equals(sortedData, randomData);
        } catch (Exception e) {
            LOG.error("ExecutorServiceMergeSort failure");
            return false;
        } finally {
            executorServiceMergeSort.shutdown();
        }
    }

}