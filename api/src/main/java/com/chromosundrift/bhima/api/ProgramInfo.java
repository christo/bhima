package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@JsonInclude(NON_EMPTY)

public class ProgramInfo {
    public static final ProgramInfo NULL_PROGRAM_INFO = new ProgramInfo("NULL", "no program", "dummy", generateNullImage(400, 100));

    private static BufferedImage generateNullImage(int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.gray);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.white);
        graphics.drawLine(0, 0, width, height);
        graphics.drawLine(0, height, width, 0);
        graphics.drawOval(0, 0, width, height);
        graphics.setColor(Color.red);
        graphics.fillOval(width / 4, height / 4, width / 2, height / 2);
        graphics.dispose();
        try {
            ImageIO.write(bi, "jpg", new File("test.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi;
    }

    private String name;
    private String id;
    private String type;

    @JsonSerialize(using = ImageSerializer.class)
    private BufferedImage thumbnail;

    public ProgramInfo() {
    }

    public ProgramInfo(String id, String name, String type, BufferedImage thumbnail) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BufferedImage getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(BufferedImage thumbnail) {
        this.thumbnail = thumbnail;
    }
}
