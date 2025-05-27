package com.pinnacle.backend.util;

import com.pinnacle.backend.model.FinalModel;
import java.util.List;
import java.util.concurrent.Callable;

public class FinalModelProcessorTask implements Callable<String> {
    private Long memId;
    private List<FinalModel> finalModels;

    public FinalModelProcessorTask(Long memId2, List<FinalModel> finalModels) {
        this.memId = memId2;
        this.finalModels = finalModels;
    }

    @Override
    public String call() {
        for (FinalModel model : finalModels) {
            System.out.println("Processing FinalModel ID: " + model.getId() + " for memId: " + memId);
            // Add processing logic here
        }
        return "Completed processing for memId: " + memId;
    }
}