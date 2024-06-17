package uk.ac.york.idk503.performancetest.sort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.york.idk503.performancetest.StaticTestConfig;
import uk.ac.york.idk503.performancetest.runner.PerformanceTestRunner;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSortablePrograms extends StaticTestConfig {

    final int[] randomArray = PerformanceTestRunner.getRandomArray();
    final int[] expectedArray = Arrays.stream(randomArray).sorted().toArray();

    final Sortable[] sortables = new Sortable[]{
            new CompletableFutureMergeSort(),
            new ExecutorServiceMergeSort(),
            new NoConcurrencyUtilityMergeSort(),
            new ParallelStreamMergeSort(),
            new StructuredConcurrencyMergeSort()
    };

    @BeforeEach
    void setUp() {
    }

    @Test
    void sort() throws Exception {
        for(Sortable sortable : sortables){
            int[] actualArray = sortable.sort(randomArray);
            System.out.println(sortable.getClass().getSimpleName() + ":" + Arrays.toString(actualArray));
            assertTrue(Arrays.equals(expectedArray, actualArray));
        }
    }
}