package org.web.posetrainer.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/feedbacks")
@PreAuthorize("hasRole('ADMIN')")
public class FeedbacksController {
}
