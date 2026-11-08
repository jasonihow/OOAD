import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

class PdfExporter {
    private static final double A4_WIDTH = 595.28;
    private static final double A4_HEIGHT = 841.89;

    private PdfExporter() {
    }

    static void export(BufferedImage image, File outputFile) throws IOException {
        ByteArrayOutputStream jpegBytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", jpegBytes)) {
            throw new IOException("無法將畫布影像轉換成 JPEG。");
        }

        double pageWidth = image.getWidth() > image.getHeight() ? A4_HEIGHT : A4_WIDTH;
        double pageHeight = image.getWidth() > image.getHeight() ? A4_WIDTH : A4_HEIGHT;
        double scale = Math.min(pageWidth / image.getWidth(), pageHeight / image.getHeight());
        double imageWidth = image.getWidth() * scale;
        double imageHeight = image.getHeight() * scale;
        double imageX = (pageWidth - imageWidth) / 2;
        double imageY = (pageHeight - imageHeight) / 2;

        String content = String.format(Locale.ROOT, "q\n%.4f 0 0 %.4f %.4f %.4f cm\n/Im0 Do\nQ\n",
                imageWidth, imageHeight, imageX, imageY);
        byte[] contentBytes = content.getBytes(StandardCharsets.US_ASCII);
        byte[] imageBytes = jpegBytes.toByteArray();

        List<byte[]> objects = new ArrayList<>();
        objects.add(ascii("<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(ascii("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objects.add(ascii(String.format(Locale.ROOT,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %.2f %.2f] "
                        + "/Resources << /XObject << /Im0 4 0 R >> >> /Contents 5 0 R >>",
                pageWidth, pageHeight)));
        objects.add(streamObject(String.format(Locale.ROOT,
                "<< /Type /XObject /Subtype /Image /Width %d /Height %d "
                        + "/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length %d >>",
                image.getWidth(), image.getHeight(), imageBytes.length), imageBytes));
        objects.add(streamObject(String.format(Locale.ROOT, "<< /Length %d >>", contentBytes.length), contentBytes));

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        write(pdf, ascii("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.size());
            write(pdf, ascii(String.format(Locale.ROOT, "%d 0 obj\n", i + 1)));
            write(pdf, objects.get(i));
            write(pdf, ascii("\nendobj\n"));
        }

        int xrefOffset = pdf.size();
        write(pdf, ascii(String.format(Locale.ROOT, "xref\n0 %d\n", objects.size() + 1)));
        write(pdf, ascii("0000000000 65535 f \n"));
        for (int offset : offsets) {
            write(pdf, ascii(String.format(Locale.ROOT, "%010d 00000 n \n", offset)));
        }
        write(pdf, ascii(String.format(Locale.ROOT,
                "trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n",
                objects.size() + 1, xrefOffset)));

        Files.write(outputFile.toPath(), pdf.toByteArray());
    }

    private static byte[] streamObject(String dictionary, byte[] stream) throws IOException {
        ByteArrayOutputStream object = new ByteArrayOutputStream();
        write(object, ascii(dictionary + "\nstream\n"));
        write(object, stream);
        write(object, ascii("\nendstream"));
        return object.toByteArray();
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static void write(ByteArrayOutputStream output, byte[] bytes) throws IOException {
        output.write(bytes);
    }
}
