package gui.ava.html;

import gui.ava.html.renderer.ImageRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Html2ImageTest {
    public static void main(String[] args) {
        File file = new File("/Users/bob/Downloads/test(1).html");
        String htmlStr = "";
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                htmlStr += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(htmlStr);
        // 2.0.1 版本
        Html2Image html2Image = Html2Image.fromHtml(htmlStr);
        ImageRenderer imageRenderer = html2Image.getImageRenderer();
        imageRenderer.setWidth(576);

        long now = System.currentTimeMillis();
        BufferedImage bufferedImage = imageRenderer.getBufferedImage(BufferedImage.TYPE_INT_RGB);
        System.out.println("getBufferedImage cost:" + (System.currentTimeMillis() - now));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        OutputStream os = null;
        try {
            os = new FileOutputStream("/Users/bob/Downloads/test(2).png");
            os.write(bytes, 0, bytes.length);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
