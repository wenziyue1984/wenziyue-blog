package com.wenziyue.blog.biz.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wenziyue
 */
public class QRGenUtils {

    public static BufferedImage generate(String text,
                                         int size,
                                         Color fg, Color bg,
                                         int marginModules,
                                         ErrorCorrectionLevel ecLevel,
                                         BufferedImage logo,       // 可为 null
                                         String caption)           // 可为 null
            throws Exception {
        // 1) 编码参数：UTF-8 / 纠错 / 留白（单位：模块）
        Map<EncodeHintType,Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ecLevel);
        hints.put(EncodeHintType.MARGIN, marginModules);

        // 2) 生成矩阵并着色
        BitMatrix matrix = new QRCodeWriter()
                .encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        MatrixToImageConfig colors = new MatrixToImageConfig(fg.getRGB(), bg.getRGB());
        BufferedImage qr = MatrixToImageWriter.toBufferedImage(matrix, colors);

        // 3) 叠加 Logo（建议 Logo 边长 ≈ 码边长的 0.15～0.20）
        if (logo != null) {
            int target = Math.round(size * 0.18f);
            Image scaled = logo.getScaledInstance(target, target, Image.SCALE_SMOOTH);
            Graphics2D g = qr.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = (qr.getWidth() - target) / 2;
            int y = (qr.getHeight() - target) / 2;
            // 给 Logo 垫个白底，提升对比度
            g.setColor(Color.WHITE);
            g.fillRoundRect(x - 6, y - 6, target + 12, target + 12, 12, 12);
            g.drawImage(scaled, x, y, null);
            g.dispose();
        }

        // 4) 添加底部文案：新建更高画布，把二维码与文字一起画
        if (caption != null && !caption.isEmpty()) {
            int padding = 20;
            int captionArea = 56;
            BufferedImage canvas = new BufferedImage(size, size + captionArea, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g2.drawImage(qr, 0, 0, null);
            g2.setColor(Color.DARK_GRAY);
            Font font = new Font("SansSerif", Font.PLAIN, 18);
            g2.setFont(font);
            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D box = font.getStringBounds(caption, frc);
            int cx = (int) Math.round((size - box.getWidth()) / 2);
            int cy = size + padding + (int) Math.round(box.getHeight());
            // 半透明白底，保证文字可读
            g2.setComposite(AlphaComposite.SrcOver.derive(0.88f));
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(Math.max(0, cx - 12), size + 6, (int) box.getWidth() + 24, 38, 10, 10);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(Color.BLACK);
            g2.drawString(caption, cx, cy);
            g2.dispose();
            qr = canvas;
        }

        return qr;
    }

    public static void main(String[] args) throws Exception {
//        BufferedImage logo = ImageIO.read(Path.of("logo.png").toFile()); // 可选
        BufferedImage img = generate(
                // 码内数据：URL/文本/名片/Wi-Fi 皆可
                "https://example.com/?q=你好",
                600,
                Color.BLACK, Color.WHITE,
                4, ErrorCorrectionLevel.H,
                null,
                "示例二维码"
        );
//        Files.createDirectories(Path.of("out"));
//        ImageIO.write(img, "png", Path.of("out/qr.png").toFile());

        Path outDir = java.nio.file.Paths.get("out");
        java.nio.file.Files.createDirectories(outDir);
        java.nio.file.Path outFile = outDir.resolve("qr.png");
        javax.imageio.ImageIO.write(img, "png", outFile.toFile());
        System.out.println("实际写入到: " + outFile.toAbsolutePath());
    }
}
