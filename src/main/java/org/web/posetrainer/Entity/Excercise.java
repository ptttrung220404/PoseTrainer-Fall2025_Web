package org.web.posetrainer.Entity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Excercise {
    private String id;
    private String name;
    private String slug;
    private List<String> category;
    private List<String> muscles;
    private String level;
    private List<String> equipment;
    private List<String> tags;
    private Media media;
    private MediaPipe mediapipe;
    private DefaultConfig defaultConfig;
    private boolean isPublic;
    private long updatedAt;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Media implements Serializable {
        private String demoVideoUrl;
        private String thumbnailUrl;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MediaPipe implements Serializable {
        private String analyzerType;
        private String version;
    }
    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DefaultConfig implements Serializable {
        private int sets;
        private int reps;
        private int restSec;
        private String difficulty;
    }
}
