package uk.ac.york.idk503.performancetest.sort;

import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.PerformanceTestRunner;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.Arrays;

@Component
public final class NoConcurrencyUtilityMergeSort implements Sortable, Testable {

    public int[] sort(final int[] data) {
        if (data.length <= 1) {
            return data;
        }

        final int middle = data.length / 2;
        final int[] left = java.util.Arrays.copyOfRange(data, 0, middle);
        final int[] right = java.util.Arrays.copyOfRange(data, middle, data.length);

        return merge(sort(left), sort(right));
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
            final var sortedData = new NoConcurrencyUtilityMergeSort().sort(randomData);

            Arrays.sort(randomData);
            return Arrays.equals(sortedData, randomData);
        } catch (Exception e) {
            return false;
        }
    }
}