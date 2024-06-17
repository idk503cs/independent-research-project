package uk.ac.york.idk503.performancetest.sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.PerformanceTestRunner;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Component
public final class ParallelStreamMergeSort implements Sortable, Testable {
    private static final Logger LOG = LoggerFactory.getLogger(ParallelStreamMergeSort.class);

    public int[] sort(final int[] data) {
        if (data.length <= 1) {
            return data;
        }

        final int mid = data.length / 2;

        return mergeWithAtomic(
                sort(Arrays.copyOfRange(data, 0, mid)),
                sort(Arrays.copyOfRange(data, mid, data.length))
        );
    }

    private static int[] mergeWithAtomic(int[] left, int[] right) {
        final AtomicInteger mergeIdx = new AtomicInteger(0);
        final AtomicInteger leftIdx = new AtomicInteger(0);
        final AtomicInteger rightIdx = new AtomicInteger(0);
        final int[] merged = new int[left.length + right.length];

        IntStream.range(0, merged.length)
                .filter(x -> leftIdx.get() < left.length && rightIdx.get() < right.length)
                .forEach(num -> merged[mergeIdx.getAndIncrement()] = (left[leftIdx.get()] <= right[rightIdx.get()]) ? left[leftIdx.getAndIncrement()] : right[rightIdx.getAndIncrement()]);

        IntStream.range(0, left.length - leftIdx.get())
                .parallel()
                .forEach(num -> merged[mergeIdx.get()+num] = left[leftIdx.get()+num]);

        mergeIdx.getAndAdd(left.length - leftIdx.get());

        IntStream.range(0, right.length - rightIdx.get())
                .parallel()
                .forEach(num -> merged[mergeIdx.get()+num] = right[rightIdx.get()+num]);

        return merged;
    }

    public boolean test() {
        try {
            final var randomData = PerformanceTestRunner.getRandomArray();
            final var sortedData = new ParallelStreamMergeSort().sort(randomData);

            Arrays.sort(randomData);
            return Arrays.equals(sortedData, randomData);
        } catch (Exception e) {
            LOG.error("ParallelStreamMergeSort failure");
            return false;
        }
    }
}
