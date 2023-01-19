package com.othmane.DbToCsv;

import org.hibernate.Session;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@EnableScheduling
public class controllers {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;


    @GetMapping("startJob")
    //every day at 1am
    //@Scheduled(cron = "0 0 1 1/1 * ?")

    public BatchStatus startBatch(@RequestParam("film_rating") String film_rating) throws Exception {

        System.out.println("batch started ............... ");

        JobParameters jobParameter = new JobParametersBuilder()
                .addDate("date", new Date())
                .addString("film_rating", film_rating)
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(job, jobParameter);
        while (jobExecution.isRunning()) {
            System.out.println("......");
        }

        System.out.println(jobExecution.getStatus());

        return jobExecution.getStatus();
    }

}
