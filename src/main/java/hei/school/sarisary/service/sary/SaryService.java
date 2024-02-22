package hei.school.sarisary.service.sary;

import hei.school.sarisary.PojaGenerated;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
@PojaGenerated
public class SaryService {
  public byte[] toGRay(byte[] image) throws Exception {
    InputStream post_image = new ByteArrayInputStream(image);
    BufferedImage image_posted = ImageIO.read(post_image);
    InputStream image_for = new BufferedInputStream(new ByteArrayInputStream(image));
    String processing = URLConnection.guessContentTypeFromStream(image_for);
    for (int y = 0; y < image_posted.getHeight(); y++) {
      for (int x = 0; x < image_posted.getWidth(); x++) {
        Color color = new Color(image_posted.getRGB(x, y));
        int grayLevel = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        int rgb = 0xff000000 | (grayLevel << 16) | (grayLevel << 8) | grayLevel;
        image_posted.setRGB(x, y, rgb);
      }
    }

    post_image.close();

    ByteArrayOutputStream final_image = new ByteArrayOutputStream();
    if (processing.equals(MediaType.IMAGE_PNG_VALUE)) {
      ImageIO.write(image_posted, "png", final_image);
    } else if (processing.equals(MediaType.IMAGE_JPEG_VALUE)) {
      ImageIO.write(image_posted, "jpg", final_image);
    } else throw new Exception();

    final_image.flush();
    byte[] grayImage = final_image.toByteArray();
    final_image.close();

    return grayImage;
  }
}
