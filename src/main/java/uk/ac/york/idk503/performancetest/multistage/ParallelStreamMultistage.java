package uk.ac.york.idk503.performancetest.multistage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.List;
import java.util.stream.IntStream;

@Component
public final class ParallelStreamMultistage implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(ParallelStreamMultistage.class);

    public boolean test() {
        try {
            final List<TextImage> textImages = IntStream.range(0, TARGET)
                    .parallel()
                    .mapToObj(fileName -> new TextImage(String.valueOf(fileName)))
                    .map(TextImage::resize)
                    .map(TextImage::addWatermark)
                    .toList();

            return textImages.size() == TARGET;
        } catch (Exception e) {
            LOG.error("ParallelStream image processing failed - {}", e.getMessage());
            return false;
        }
    }
}