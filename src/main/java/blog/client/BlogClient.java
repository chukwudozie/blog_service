package blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public static void main(String[] args) {

        BlogClient client = new BlogClient();
        client.run();

    }
    private  void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",50054)
                .usePlaintext().build();

        BlogServiceGrpc.BlogServiceBlockingStub client = BlogServiceGrpc.newBlockingStub(channel);

        Blog blog = Blog.newBuilder()
                .setTitle("My First Blog")
                .setContent("Hello! this is my first blog created gRPC and MongoDB")
                .setDateCreated("13/04/2022")
                .setAuthorId("Emeka")
                .build();
       CreateBlogResponse response =  client.createBlog(CreateBlogRequest.newBuilder()
                .setBlog(blog)
                .build());
        System.out.println("Received a create blog response");
        System.out.println(response.toString());

        channel.shutdown();

    }
}
