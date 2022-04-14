package blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;

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

// ------------------ For Reading Blog ----------------------
        String blogId  = response.getBlog().getId();
        System.out.println("Reading blog ...");
        ReadBlogResponse readBlogResponse = client.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId(blogId).build());
        System.out.println(readBlogResponse.toString());

        // testing error handling
//        System.out.println("Reading blog not existing ...");
//        ReadBlogResponse readBlogResponseNotFound = client.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId("fake_id").build());

//   ----------------- For Updating Blog  --------------

        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setTitle("My First Blog (Updated)")
                .setContent("Hello! this is my first blog created gRPC and MongoDB (updated)")
                .setDateCreated("14/04/2022")
                .setAuthorId("Changed Author")
                .build();
        System.out.println("Updating blog ... ");
      UpdateBlogResponse updateResponse =
              client.updateBlog(UpdateBlogRequest.newBuilder().setBlog(newBlog).build());
        System.out.println("Updated Blog: ");
        System.out.println(updateResponse.toString());

//    -------------- For Deleting Blog ------------------
        System.out.println("Deleting Blog ...");
        DeleteBlogResponse deleteBlogResponse = client.deleteBlog(
                DeleteBlogRequest.newBuilder().setBlogId(blogId).build()
        );
        System.out.println("Blog Deleted");
        System.out.println(deleteBlogResponse.toString());

//    -------------- For Listing all Blogs --------------------------
        System.out.println("Listing all blogs from DB");
// Here we list the blogs in our database
        Iterator<ListBlogResponse> listResponse  =  client.listBlogs(ListBlogRequest.newBuilder().build());
        listResponse.forEachRemaining(listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString()));


        channel.shutdown();

    }
}
