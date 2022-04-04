package in.mcxiv.app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

public class ImagesToPDF {
    public static void main(String[] args) throws IOException {

        try (PDDocument doc = new PDDocument()) {

            Files.walk(Path.of("images"), 1)
                    .filter(path -> path.toFile().isFile())
                    .sorted(Comparator.comparingInt(ImagesToPDF::intify))
                    .map(ImagesToPDF::doThatThingSafely)
                    .filter(Objects::nonNull)
                    .map(image -> cropImage(image, args))
                    .forEach(image -> {
                        try {

                            PDPage page = new PDPage(new PDRectangle(952, 1224));
                            doc.addPage(page);
                            var content = new PDPageContentStream(doc, page);

                            PDImageXObject object = LosslessFactory.createFromImage(doc, image);
                            content.drawImage(object, 0, 0);
                            content.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            doc.save(new File("output.pdf"));

        }
    }

    private static BufferedImage cropImage(BufferedImage image, String[] args) {
        BufferedImage newImage = new BufferedImage(952, 1224, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(image, -64, -344, 1080, 1920, null);
        graphics.dispose();
        return newImage;
    }

    private static BufferedImage doThatThingSafely(Path path) {
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException e) {
            System.out.println("Unable to read " + path);
            return null;
        }
    }

    private static int intify(Path path) {
        String name = path.getFileName().toString().replaceAll("[^\\d]", "");
        return name.isEmpty() ? 0 : Integer.parseInt(name);
    }
}
