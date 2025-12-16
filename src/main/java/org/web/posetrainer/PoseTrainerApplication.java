package org.web.posetrainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PoseTrainerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoseTrainerApplication.class, args);
    }

}
