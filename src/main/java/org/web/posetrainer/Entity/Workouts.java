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
public class Workouts implements Serializable {
    private String id;
    private String title;
    private String description;
    private String level;
    private List<String> focus;
    private String goalFit;
    private List<WorkoutItem> items;
    private int estDurationMin;
    private boolean isPublic;
    private String createdBy;
    private int version;
    private long updatedAt;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
    public static class WorkoutItem implements Serializable {
        private int order;
        private String exerciseId;
        private ExerciseConfig configOverride;
    }
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
    public static class ExerciseConfig implements Serializable {
        private int sets;
        private int reps;
        private int restSec;
        private String difficulty;
    }
}
