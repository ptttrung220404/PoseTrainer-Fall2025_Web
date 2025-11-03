package org.web.posetrainer.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web.posetrainer.Entity.Workouts;
import org.web.posetrainer.Service.WorkoutsService;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class WorkoutsController {
    @Autowired
    private WorkoutsService workoutsService;

    @PostMapping("/workouts")
    public String saveWorkouts(@RequestBody Workouts workouts) throws ExecutionException, InterruptedException {
        return workoutsService.saveWorkouts(workouts);
    }
    @GetMapping("/workouts/{title}")
    public Workouts getWorkouts(@PathVariable String title) throws ExecutionException, InterruptedException {
        return workoutsService.getWorkoutsDetails(title);
    }
    @PutMapping("/workouts")
    public String update(@RequestBody Workouts workouts) throws ExecutionException, InterruptedException {
        return workoutsService.updateWorkouts(workouts);
    }
    @DeleteMapping("/workouts/{title}")
    public String deleteWorkouts(@PathVariable String title) throws ExecutionException, InterruptedException {
        return workoutsService.deleteWorkouts(title);
    }
}
