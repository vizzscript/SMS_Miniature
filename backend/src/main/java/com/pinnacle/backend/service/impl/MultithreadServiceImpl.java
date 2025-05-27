package com.pinnacle.backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pinnacle.backend.model.FinalModel;
import com.pinnacle.backend.repository.FinalRepository;
import com.pinnacle.backend.service.MultithreadService;
import com.pinnacle.backend.util.FinalModelProcessorTask;

@Service
public class MultithreadServiceImpl implements MultithreadService {
    @Autowired
    private FinalRepository finalRepo;
    @Autowired
    private ExecutorService executorService;

    @Override
    public void processFinalModelsConcurrently() {
        // Fetch distinct memIds
        List<Long> memIds = finalRepo.findAll()
                .stream()
                .map(model -> model.getClient().getMemId())
                .distinct()
                .toList();

        List<Future<String>> futures = new ArrayList<>();

        for (Long memId : memIds) {
            List<FinalModel> models = finalRepo.findByClient_MemId(memId);
            Callable<String> task = new FinalModelProcessorTask(memId, models);
            Future<String> future = executorService.submit(task);
            futures.add(future);
        }

        // Collect results
        for (Future<String> future : futures) {
            try {
                System.out.println(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // executorService.shutdown();
    }

}
