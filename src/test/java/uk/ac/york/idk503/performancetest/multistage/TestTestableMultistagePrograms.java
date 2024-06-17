package uk.ac.york.idk503.performancetest.multistage;

import org.junit.jupiter.api.Test;
import uk.ac.york.idk503.performancetest.StaticTestConfig;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestTestableMultistagePrograms extends StaticTestConfig {

    Testable[] testables = new Testable[]{
            new CompletableFutureMultistage(),
            new ExecutorServiceMultistage(),
            new NoConcurrencyUtilityMultistage(),
            new ParallelStreamMultistage(),
            new StructuredConcurrencyMultistage()
    };

    @Test
    void runTestAndReturnCount() {
        for(Testable testable : testables) {
            TextImage.addImageCounter.set(0);
            TextImage.addResizeCounter.set(0);
            TextImage.addWatermarkCounter.set(0);
            testable.test();
            System.out.println(testable.getClass().getSimpleName() + " : " + TARGET);
            assertEquals(TARGET, TextImage.addImageCounter.get());
            assertEquals(TARGET, TextImage.addResizeCounter.get());
            assertEquals(TARGET, TextImage.addWatermarkCounter.get());
        }
    }
}

class TextImage {
    public static AtomicInteger addWatermarkCounter = new AtomicInteger(0);
    public static AtomicInteger addResizeCounter = new AtomicInteger(0);
    public static AtomicInteger addImageCounter = new AtomicInteger(0);

    private TextImage(final BufferedImage textImage, final String textForImage) {}

    private String textForImage;

    public TextImage(final String textForImage){
        this(textForImage, null);
        this.textForImage = textForImage;
        addImageCounter.addAndGet(1);
    }

    public TextImage(final String textForImage, final Font font){
    }

    private static BufferedImage getTextImage(final String textForImage, final int width, final int height) {
        return null;
    }

    public TextImage save(final String formatName){
        return this;
    }

    public TextImage resize(final int width, final int height) {
        return this;
    }

    public TextImage resize() {
        addResizeCounter.addAndGet(1);
        return this;
    }

    public TextImage addWatermark() {
        addWatermarkCounter.addAndGet(1);
        return this;
    }

    public String getTextForImage() {
        return null;
    }

    @Override
    public String toString(){
        return textForImage;
    }
}
