package uk.ac.york.idk503.performancetest.multistage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Component
public final class StructuredConcurrencyMultistage implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(StructuredConcurrencyMultistage.class);

    public boolean test() {
        try {
            final List<StructuredTaskScope.Subtask<TextImage>> generatedTextImages = getGeneratedTextImages();
            final List<StructuredTaskScope.Subtask<TextImage>> resizedTextImages = getResizedTextImages(generatedTextImages);
            final List<StructuredTaskScope.Subtask<TextImage>> watermarkedTextImage = getWatermarkedTextImage(resizedTextImages);

            return Integer.valueOf(TARGET).equals(watermarkedTextImage.size());
        } catch (Exception e) {
            LOG.error("Structured concurrency image processing failed.");
            return false;
        }
    }

    private static List<StructuredTaskScope.Subtask<TextImage>> getGeneratedTextImages() {
        final List<StructuredTaskScope.Subtask<TextImage>> generatedTextImages = new ArrayList<>();

        try (var scope = new StructuredTaskScope<TextImage>()) {
            for(int i = 0; i< TARGET; i++) {
                final String fileName = String.valueOf(i);
                generatedTextImages.add(scope.fork(() -> new TextImage(fileName)));
            }
            scope.join();

        } catch (InterruptedException e) {
            LOG.error("Structured Concurrency failure generating images - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Structured Concurrency failure generating images - {}", e.getMessage());
            return Collections.emptyList();
        }
        return generatedTextImages;
    }

    private static List<StructuredTaskScope.Subtask<TextImage>> getResizedTextImages(
            final List<StructuredTaskScope.Subtask<TextImage>> generatedTextImages) {
        final List<StructuredTaskScope.Subtask<TextImage>> resizedTextImages = new ArrayList<>();

        try (var scope = new StructuredTaskScope<TextImage>()) {
            generatedTextImages.forEach(image -> {
                try {
                    resizedTextImages.add(scope.fork(() -> image.get().resize()));
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            });

            scope.join();
        } catch (InterruptedException e) {
            LOG.error("Structured Concurrency failure while resizing image - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Structured Concurrency failure while resizing image - {}", e.getMessage());
            return Collections.emptyList();
        }
        return resizedTextImages;
    }

    private static List<StructuredTaskScope.Subtask<TextImage>> getWatermarkedTextImage(
            final List<StructuredTaskScope.Subtask<TextImage>> resizedTextImages) {
        final List<StructuredTaskScope.Subtask<TextImage>> watermarkedTextImage = new ArrayList<>();

        try (var scope = new StructuredTaskScope<TextImage>()) {
            resizedTextImages.forEach(image -> {
                try {
                    watermarkedTextImage.add(scope.fork(() -> image.get().addWatermark()));
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            });

            scope.join();
        } catch (InterruptedException e) {
            LOG.error("Structured Concurrency failure while adding watermark - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Structured Concurrency failure while adding watermark - {}", e.getMessage());
            return Collections.emptyList();
        }

        return watermarkedTextImage;
    }
}
