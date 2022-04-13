package blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Sum Server ... loading ...");

        Server server = ServerBuilder.forPort(5005)
                .addService(ProtoReflectionService.newInstance()) // Used to enable gRPC reflection
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            System.out.println("Shutdown Request Received");
            server.shutdown();
            System.out.println("Server successfully stopped");
        }));

        server.awaitTermination();
    }
}
