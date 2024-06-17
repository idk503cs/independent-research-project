package uk.ac.york.idk503.performancetest.multistage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Component
public final class ExecutorServiceMultistage implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceMultistage.class);

    public boolean test() {
        final ExecutorService executorService = Executors.newFixedThreadPool(50);

        try {
            final List<TextImage> generatedTextImages = getGeneratedTextImages(executorService);
            final List<TextImage> resizedTextImages = getResizedTextImages(generatedTextImages, executorService);
            final List<TextImage> watermarkedTextImage = getWatermarkedTextImage(resizedTextImages, executorService);

            return Integer.valueOf(TARGET).equals(watermarkedTextImage.size());
        } catch (Exception e) {
            LOG.error("ExecutorService image processing failure - {}", e.getMessage());
            return false;
        }  finally {
            executorService.shutdown();
            executorService.close();
        }
    }

    private static List<TextImage> getGeneratedTextImages(final ExecutorService executorService) {
        final List<Callable<Void>> generatedTextImages = new ArrayList<>();
        List<TextImage> textImages = new CopyOnWriteArrayList<>();

        try {
            for(int i = 0; i< TARGET; i++) {
                final String fileName = String.valueOf(i);
                generatedTextImages.add(() -> {
                    textImages.add(new TextImage(fileName));
                    return null;
                });
            }

            executorService.invokeAll(generatedTextImages);
        } catch (InterruptedException e) {
            LOG.error("ExecutorService failure generating images - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e){
            LOG.error("ExecutorService failure generating images - {}", e.getMessage());
            return Collections.emptyList();
        }

        return textImages;
    }


    private static List<TextImage> getResizedTextImages(
        final List<TextImage> generatedTextImages, ExecutorService executorService) {
        final List<Callable<Void>> resizedTextImages = new ArrayList<>();
        final List<TextImage> textImages = new CopyOnWriteArrayList<>();

        try {
            generatedTextImages.forEach(image ->
                    resizedTextImages.add(() -> {
                        textImages.add(image.resize());
                        return null;
                    })
            );

            executorService.invokeAll(resizedTextImages);
        } catch (InterruptedException e) {
            LOG.error("ExecutorService failure while resizing image - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e){
            LOG.error("ExecutorService failure while resizing image - {}", e.getMessage());
            return Collections.emptyList();
        }

        return textImages;
    }

    private static List<TextImage> getWatermarkedTextImage(
        final List<TextImage> resizedTextImages, final ExecutorService executorService) {
        final List<Callable<Void>> watermarkedTextImage = new ArrayList<>();
        final List<TextImage> textImages = new CopyOnWriteArrayList<>();

        try {
            resizedTextImages.forEach(image -> {
                try {
                    watermarkedTextImage.add(() -> {
                        textImages.add(image.addWatermark());
                        return null;
                    });
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            });

            executorService.invokeAll(watermarkedTextImage);
        } catch (InterruptedException e) {
            LOG.error("ExecutorService failure while adding a watermark - {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("ExecutorService failure while adding a watermark - {}", e.getMessage());
            return Collections.emptyList();
        }
        return textImages;
    }
}
