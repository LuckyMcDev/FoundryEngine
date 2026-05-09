package de.luckymcdev.foundryengine.common.bbmodel.data;

import de.luckymcdev.foundryengine.client.Client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.Base64;

public class Texture {

    private String path;
    private String name;
    private String folder;
    private String namespace;
    private String id;
    private String group;
    private Float width;
    private Float height;
    private Float uv_width;
    private Float uv_height;
    private boolean particle;
    private boolean use_as_default;
    private boolean layers_enabled;
    private String sync_to_project;
    private String render_mode;
    private String render_sides;
    private Float frame_time;
    private String frame_order_type;
    private String frame_order;
    private boolean frame_interpolate;
    private boolean visible;
    private boolean internal;
    private boolean saved;
    private String uuid;
    private String relative_path;
    private String source;

    private transient Image image;

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getFolder() {
        return folder;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public Float getWidth() {
        return width;
    }

    public Float getHeight() {
        return height;
    }

    public Float getUv_width() {
        return uv_width;
    }

    public Float getUv_height() {
        return uv_height;
    }

    public boolean isParticle() {
        return particle;
    }

    public boolean isUse_as_default() {
        return use_as_default;
    }

    public boolean isLayers_enabled() {
        return layers_enabled;
    }

    public String getSync_to_project() {
        return sync_to_project;
    }

    public String getRender_mode() {
        return render_mode;
    }

    public String getRender_sides() {
        return render_sides;
    }

    public Float getFrame_time() {
        return frame_time;
    }

    public String getFrame_order_type() {
        return frame_order_type;
    }

    public String getFrame_order() {
        return frame_order;
    }

    public boolean isFrame_interpolate() {
        return frame_interpolate;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isSaved() {
        return saved;
    }

    public String getUuid() {
        return uuid;
    }

    public String getRelative_path() {
        return relative_path;
    }

    public String getSource() {
        return source;
    }

    public Image getImage() {
        if (image == null) {
            try {
                String[] parts = source.split(",");
                String imageString = parts[1];

                // create a buffered image
                byte[] imageByte;

                imageByte = Base64.getDecoder().decode(imageString);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                image = ImageIO.read(bis);
                bis.close();

                return image;
            } catch (Exception e) {
                Client.LOGGER.error(e.getMessage());
                return null;
            }
        }
        return image;
    }
}
