package com.motomami.tasks;

import com.motomami.Services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GenerateInvoicesTask {

    @Autowired
    ProcessService pService;


//    @Scheduled(cron = "${cron.task.generateinvoices}")
//    public void task(){
//        try{
//            System.out.println("\nEsta tarea se lanza cada 15 segundos");
//            pService.generateProviderInvoice(C_SOURCE_INVOICE);
//        } catch (Exception e){
//            System.err.println("Error al ejecutar la task GenerateInvoicesTask: " + e.getMessage());
//        }
//    }
}
