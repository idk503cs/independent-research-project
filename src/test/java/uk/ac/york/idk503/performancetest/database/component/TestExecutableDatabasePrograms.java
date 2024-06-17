package uk.ac.york.idk503.performancetest.database.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.york.idk503.performancetest.StaticTestConfig;
import uk.ac.york.idk503.performancetest.database.component.loader.*;
import uk.ac.york.idk503.performancetest.database.repository.MemoryInfoRepository;
import uk.ac.york.idk503.performancetest.database.repository.ThreadInfoRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestExecutableDatabasePrograms extends StaticTestConfig {

    MemoryInfoRepository memoryInfoRepository = mock(MemoryInfoRepository.class);
    ThreadInfoRepository threadInfoRepository = mock(ThreadInfoRepository.class);
    Executable[] executables = new Executable[]{
            new CompletableFutureDataLoad(memoryInfoRepository, threadInfoRepository),
            new ExecutorServiceDataLoad(memoryInfoRepository, threadInfoRepository),
            new NoConcurrencyUtilityDataLoad(memoryInfoRepository, threadInfoRepository),
            new ParallelStreamDataLoad(memoryInfoRepository, threadInfoRepository),
            new StructuredConcurrencyDataLoad(memoryInfoRepository, threadInfoRepository)
    };
    AtomicInteger memoryCounter = new AtomicInteger(0);
    AtomicInteger threadCounter = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            memoryCounter.addAndGet(1);
            return null;
        }).when(memoryInfoRepository).saveAndFlush(any());

        doAnswer(invocation -> {
            threadCounter.addAndGet(1);
            return null;
        }).when(threadInfoRepository).saveAndFlush(any());

        doAnswer(invocation -> {
            memoryCounter.addAndGet(1);
            return null;
        }).when(memoryInfoRepository).saveAndFlush(any());

        doAnswer(invocation -> {
            return Long.valueOf(memoryCounter.get());
        }).when(memoryInfoRepository).count();

        doAnswer(invocation -> {
            return Long.valueOf(threadCounter.get());
        }).when(threadInfoRepository).count();
    }

    @Test
    void runTestAndReturnCount() {
        var addedTimeInSeconds = 1;
        var addedTimeInNanos = addedTimeInSeconds * 1_000_000_000L;
        for(Executable executable : executables) {
            memoryCounter.set(0);
            threadCounter.set(0);
            var finishTimeInEpochSecond = LocalDateTime.now().plusSeconds(addedTimeInSeconds).toEpochSecond(ZoneOffset.UTC);
            var finishTimeInNanoSecond = System.nanoTime() + addedTimeInNanos;
            var actualRecordCounts = executable.runTestAndReturnCount(finishTimeInNanoSecond);

            assertTrue(System.nanoTime() + addedTimeInNanos >= finishTimeInNanoSecond);
            assertEquals(finishTimeInEpochSecond, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            var expectedRecordCount = Map.of("MEMORY", memoryCounter.get(), "THREAD", threadCounter.get());
            assertEquals(new TreeMap<>(expectedRecordCount).toString(), new TreeMap<>(actualRecordCounts).toString());

            System.out.println(actualRecordCounts + " " + executable.getClass().getSimpleName());
        }
    }
}