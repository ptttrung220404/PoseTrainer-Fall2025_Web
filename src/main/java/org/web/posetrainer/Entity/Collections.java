package org.web.posetrainer.Entity;
import java.io.Serializable;
import java.util.List;

import com.google.cloud.firestore.annotation.IgnoreExtraProperties;
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
    private boolean isPublic;
    private String createdBy;
    private int order;
    private List<String> tags;
    private long createdAt;
    private long updatedAt;

}
