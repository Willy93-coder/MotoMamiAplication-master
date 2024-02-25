package com.motomami.controllers;

import com.motomami.Services.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.motomami.Utils.Constants.*;


@RestController
public class MotomamiController {

    @Autowired
    ProcessService pService;
    @RequestMapping("/")
    String hellow(){
        return "Página de inicio";
    }

    @RequestMapping(value =("/readInfo/{resource}"), method = RequestMethod.GET, produces = "application/json")
    String callReadInfo(@PathVariable String resource){
        try{
            System.out.println("\nMe estan llamando desde la web");
            switch (resource.toUpperCase()){
                case C_SOURCE_PARTS:
                    pService.readFileInfo(C_SOURCE_PARTS);
                    break;
                case C_SOURCE_CUSTOMER:
                    pService.readFileInfo(C_SOURCE_CUSTOMER);
                    break;
                case C_SOURCE_VEHICLE:
                    pService.readFileInfo(C_SOURCE_VEHICLE);
                    break;
                default:
            }
        } catch (Exception e){
            System.err.println("No funcionan las tareas de leer: " + e.getMessage());
        }
        System.out.println("El valor de resource en leer es: " + resource);
        return "Leyendo";
    }

    @RequestMapping(value =("/processInfo/{resource}"), method = RequestMethod.GET, produces = "application/json")
    String callProcessInfo(@PathVariable String resource){
        try{
            System.out.println("\nMe estan llamando desde la web de procesar");
            switch (resource.toUpperCase()){
                case C_SOURCE_PARTS:
                    pService.processInfoWithStatusNotProcessed(C_SOURCE_PARTS);
                    break;
                case C_SOURCE_CUSTOMER:
                    pService.processInfoWithStatusNotProcessed(C_SOURCE_CUSTOMER);
                    break;
                case C_SOURCE_VEHICLE:
                    pService.processInfoWithStatusNotProcessed(C_SOURCE_VEHICLE);
                    break;
                default:
            }
        } catch (Exception e){
            System.err.println("No funcionan las tareas de procesar: " + e.getMessage());
        }
        System.out.println("El valor de resource en procesar es: "+ resource);
        return "Procesando";
    }

    @RequestMapping(value =("/generateInvoice/{resource}"), method = RequestMethod.GET, produces = "application/json")
    String callGenerateInvoice(@PathVariable String resource){
        try{
            System.out.println("\nMe estan llamando desde la web de generar facturas");
            switch (resource.toUpperCase()){
                case C_SOURCE_PARTS:
                    pService.generateProviderInvoice(C_SOURCE_INVOICE);
                    break;
                default:
            }
        } catch (Exception e){
            System.err.println("No funcionan las tareas de generar facturas: " + e.getMessage());
        }
        System.out.println("El valor de resource en generar factura es: "+ resource);
        return "Generando factura";
    }
}