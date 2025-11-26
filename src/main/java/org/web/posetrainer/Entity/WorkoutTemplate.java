package org.web.posetrainer.Entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutTemplate {
    private String id;
    private String title;
    private String description;
    private String level;
    private List<String> focus;
    private String goalFit;
    private List<WorkoutItem> items;
    private int estDurationMin;

    @JsonProperty("isPublic")
    @PropertyName("isPublic")
    private boolean isPublic;

    // Ngăn Lombok sinh `isIsPublic()` gây conflict
    @JsonProperty("isPublic")
    @PropertyName("isPublic")
    public boolean getIsPublic() {
        return isPublic;
    }

    @JsonProperty("isPublic")
    @PropertyName("isPublic")
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

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
