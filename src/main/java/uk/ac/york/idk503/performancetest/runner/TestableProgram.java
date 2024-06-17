package uk.ac.york.idk503.performancetest.runner;

import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.database.component.loader.*;
import uk.ac.york.idk503.performancetest.multistage.*;
import uk.ac.york.idk503.performancetest.rest.service.*;
import uk.ac.york.idk503.performancetest.sort.*;

@Component
public record TestableProgram(ParallelStreamDataLoad psDataLoad, StructuredConcurrencyDataLoad scDataLoad,
                              NoConcurrencyUtilityDataLoad ncuDataLoad, ExecutorServiceDataLoad esDataLoad,
                              CompletableFutureDataLoad cfDataLoad, CompletableFutureService cfService,
                              ExecutorServiceService esService, ParallelStreamService psService,
                              StructuredConcurrencyService scService, NoConcurrencyUtilityService ncuService,
                              CompletableFutureMultistage cfMultistage, StructuredConcurrencyMultistage scMultistage,
                              ExecutorServiceMultistage esMultistage, NoConcurrencyUtilityMultistage ncuMultistage,
                              ParallelStreamMultistage psMultistage, CompletableFutureMergeSort cfMergeSort,
                              ExecutorServiceMergeSort esMergeSort, NoConcurrencyUtilityMergeSort ncuMergeSort,
                              ParallelStreamMergeSort psMergeSort, StructuredConcurrencyMergeSort scMergeSort) {

}