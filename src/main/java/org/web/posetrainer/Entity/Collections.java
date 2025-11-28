package org.web.posetrainer.Entity;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties

public class Collections  {
    private String id;
    private String title;
    private String description;
    private String category;
    private List<String> workoutTemplateIds;
    private String thumbnailUrl;
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
    private int order;
    private List<String> tags;
    private long createdAt;
    private long updatedAt;

}
