package com.motomami.tasks;

import com.motomami.Services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.motomami.Utils.Constants.C_SOURCE_CUSTOMER;

@Component
public class GetCustomersTask {
    
    @Autowired
    ProcessService pService;

    @Scheduled(cron = "${cron.task.getCustomer}")
    public void task(){
        try{
            System.out.println("\nEsta tarea se lanza cada 15 segundos");
            pService.readFileInfo(C_SOURCE_CUSTOMER);
        } catch (Exception e){
            System.err.println("Error al ejecutar la task GetCustomersTask: " + e.getMessage());
        }
    }
}
