package com.motomami.Services;
import org.springframework.stereotype.Service;

@Service
public interface ProcessService {
    //metodo que todas las clases implementen de esta utilizarán.
    void readFileInfo(String pSource);

    void processInfoWithStatusNotProcessed(String pSource);

    void generateProviderInvoice(String pSource);
}
