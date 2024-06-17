package uk.ac.york.idk503.performancetest.multistage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.york.idk503.performancetest.runner.Testable;

import java.util.ArrayList;
import java.util.List;

@Component
public final class NoConcurrencyUtilityMultistage implements Testable {
    private static final Logger LOG = LoggerFactory.getLogger(NoConcurrencyUtilityMultistage.class);

    public boolean test() {
        try {
            final List<TextImage> generatedTextImages = new ArrayList<>();
            for (int i = 0; i < TARGET; i++) {
                generatedTextImages.add(new TextImage(String.valueOf(i)));
            }

            final List<TextImage> resizedTextImages = new ArrayList<>();
            for (TextImage textImage : generatedTextImages) {
                resizedTextImages.add(textImage.resize());
            }

            final List<TextImage> watermarkedTextImage = new ArrayList<>();
            for (TextImage textImage : resizedTextImages) {
                watermarkedTextImage.add(textImage.addWatermark());
            }

            return watermarkedTextImage.size() == TARGET;
        } catch (Exception e) {
            LOG.error("Sequential image processing failed - {}", e.getMessage());
            return false;
        }
    }
}
