package me.pendem.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class PartitionAndAsyncTester {

    public static void main(String args[]){
        int bulkInputSize = 100;
        List<SampleRequest> bulkRequests = new ArrayList<>();
        // Create a bulk Request.
        for(int i=0; i <bulkInputSize ; i++){
            bulkRequests.add(new SampleRequest(Math.random()));
        }
        System.out.println("size of bulkRequests->"+bulkRequests.size());
        int chunkSize = 10;
        List<List<SampleRequest>> partitionedBulkRequests = new ArrayList<>();
        for(int i=0 ; i < bulkRequests.size(); i+= chunkSize){
            int end = Math.min(bulkRequests.size(), i + chunkSize);
            partitionedBulkRequests.add(bulkRequests.subList(i, end));
        }
        System.out.println("size of partitionedBulkRequests->"+partitionedBulkRequests.size());

        CompletableFuture<SampleResponse>[] futuresArray = new CompletableFuture[partitionedBulkRequests.size()];
        for(int i=0 ; i < partitionedBulkRequests.size(); i++){
            List<SampleRequest> requests = partitionedBulkRequests.get(i);
            Supplier<SampleResponse> supplier = () -> invokeMeAsynchronously(requests);
            CompletableFuture<SampleResponse> future = CompletableFuture.supplyAsync(supplier);
            futuresArray[i] = future;
        }
        System.out.println("Start - Current Thread of Main Method-->"+Thread.currentThread().getName());
        CompletableFuture<?> combinedFutures = CompletableFuture.allOf(futuresArray);
        try {
            combinedFutures.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        for(int i=0; i < partitionedBulkRequests.size(); i++){
            try {
                if(futuresArray[i].isDone() && futuresArray[i].get().isStatus()){
                    System.out.println("SUCCESS");
                }else{
                    System.out.println("FAILED");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("End - Current Thread of Main Method-->"+Thread.currentThread().getName());


    }

    private static SampleResponse invokeMeAsynchronously(List<SampleRequest> requests){
        SampleResponse response = new SampleResponse();
        response.setStatus(true);
        System.out.println("Current Thread of Async Method-->"+Thread.currentThread().getName());
        return response;
    }

}
