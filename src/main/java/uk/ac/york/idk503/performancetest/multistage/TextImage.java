package uk.ac.york.idk503.performancetest.multistage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class TextImage {
    private static final Logger LOG = LoggerFactory.getLogger(TextImage.class);
    private static final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 36);
    private static final BufferedImage initialImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private static final BufferedImage watermarkImage = new TextImage("Stamp™", font).bufferedImage;

    private final BufferedImage bufferedImage;
    private final String textForImage;

    private TextImage(final BufferedImage bufferedImage, final String textForImage) {
        this.bufferedImage = bufferedImage;
        this.textForImage = textForImage;
    }

    public TextImage(final String textForImage){
        this(textForImage, font);
    }

    public TextImage(final String textForImage, final Font font){
        final Graphics2D graphics2D = initialImage.createGraphics();
        graphics2D.setFont(font);

        final FontMetrics fontMetrics = graphics2D.getFontMetrics();
        final int width = fontMetrics.stringWidth(textForImage);
        final int height = fontMetrics.getHeight();
        graphics2D.dispose();

        this.textForImage = textForImage;
        this.bufferedImage = getTextImage(textForImage, width, height);
    }

    private static BufferedImage getTextImage(final String textForImage, final int width, final int height) {
        final BufferedImage textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics2D = textImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(font);

        final FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.setColor(Color.BLUE);
        graphics2D.drawString(textForImage, 0, fontMetrics.getAscent());
        graphics2D.dispose();

        return textImage;
    }

    public TextImage save(final String formatName){
        try {
            ImageIO.write(bufferedImage, formatName, new File(textForImage + '.' + formatName));
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return this;
    }

    public TextImage resize(final int width, final int height) {
        final var imageBuffer = getTextImage(textForImage, width, height);
        return new TextImage(imageBuffer, textForImage);
    }

    public TextImage resize() {
        final int DEFAULT_WIDTH=140;
        final int DEFAULT_HEIGHT=80;
        return resize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    public TextImage addWatermark() {
        final BufferedImage mergeImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        final Graphics2D graphics2D = mergeImage.createGraphics();
        graphics2D.drawImage(bufferedImage, 0, 0, null);
        graphics2D.drawImage(watermarkImage, 0, 35, null);
        graphics2D.dispose();

        return new TextImage(mergeImage, textForImage);
    }

    public String getTextForImage() {
        return textForImage;
    }
}
