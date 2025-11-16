package com.mio.graph;

import com.mio.graph.model.Arc;
import com.mio.graph.model.Stop;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphVisualizer {

    public static void renderRoute(int lineId,
                                   int orient,
                                   List<Arc> arcs,
                                   Map<Long, Stop> stops,
                                   Path outFile) throws IOException {
       
        List<Long> nodes = new ArrayList<>();
        if (arcs.isEmpty()) {
            
            BufferedImage img = new BufferedImage(800, 120, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
                g.setColor(Color.BLACK);
                g.drawString("Route " + lineId + " orient " + orient + " (no arcs)", 10, 20);
            } finally {
                g.dispose();
            }
            ImageIO.write(img, "jpg", outFile.toFile());
            return;
        }

        nodes.add(arcs.get(0).getFromStopId());
        for (Arc a : arcs) {
            nodes.add(a.getToStopId());
        }

     
        int nodeCount = nodes.size();
        int spacing = 140;
        int margin = 40;
        int width = Math.max(800, margin * 2 + Math.max(0, nodeCount - 1) * spacing + 160);
        int height = 240;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            int centerY = height / 2;
            int radius = 28;
            Font font = new Font("SansSerif", Font.PLAIN, 12);
            g.setFont(font);

           
            for (int i = 0; i < nodeCount - 1; i++) {
                int x1 = margin + i * spacing;
                int x2 = margin + (i + 1) * spacing;
                int y1 = centerY;
                int y2 = centerY;
                
                g.setColor(Color.DARK_GRAY);
                g.setStroke(new BasicStroke(3));
                g.drawLine(x1 + radius, y1, x2 - radius, y2);
                
                drawArrowHead(g, x2 - radius, y2);
            }

           
            for (int i = 0; i < nodeCount; i++) {
                int x = margin + i * spacing;
                int y = centerY;
                long stopId = nodes.get(i);
                String label = stops.containsKey(stopId) ? stops.get(stopId).getShortName() : Long.toString(stopId);
                
                g.setColor(new Color(0x2B7A78));
                g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
               
                g.setColor(Color.BLACK);
                int labelY = y + radius + 16;
                String idStr = String.valueOf(stopId);
                
                g.drawString(label, x - (g.getFontMetrics().stringWidth(label) / 2), labelY);
            }

            
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString(String.format("Route %d - Orientation %d", lineId, orient), 10, 20);

        } finally {
            g.dispose();
        }

        ImageIO.write(img, "jpg", outFile.toFile());
    }

    private static void drawArrowHead(Graphics2D g, int x, int y) {
        int size = 10;
        int hx = x;
        int hy = y;
        Polygon p = new Polygon();
        p.addPoint(hx, hy);
        p.addPoint(hx - size, hy - size / 2);
        p.addPoint(hx - size, hy + size / 2);
        g.fill(p);
    }
}
