package blog.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    // Set up mongo database
    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    // the collection gets a table in the database named blog
    private MongoCollection<Document> collection = database.getCollection("blog");


    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received create blog request");
        Blog newBlog  = request.getBlog();
        Document doc = new Document("author_id",newBlog.getAuthorId())
                .append("title",newBlog.getTitle())
                .append("content", newBlog.getContent())
                .append("date_created",newBlog.getDateCreated());

        // create a doc in the mongo DB
        System.out.println("Inserting blog ...");
        collection.insertOne(doc);
        // Get the id of the created doc from the Mongo DB
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog: " +id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                //toBuilder() enables update of an object created with the builder pattern
                .setBlog(newBlog.toBuilder().setId(id).build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Received Read Blog Request");

        String blogId = request.getBlogId();
        // find in the collection all the matching elements with the id from the request and return the first one
        System.out.println("Searching for a blog");

        Document result = null;
        try {
            // check if the request Id exists in the database
            result = collection.find(eq("_id", new ObjectId(blogId))).first();
        } catch (Exception e){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("The Blog with the ID "+blogId+ " was not found")
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException());

        }
       if(result == null){
           System.out.println("Blog not found");
           responseObserver.onError(Status.NOT_FOUND
                   .withDescription("The Blog with the ID "+blogId+ " was not found")
                   .asRuntimeException());
       } else {
           System.out.println("Blog found, sending response ... ");
//           parse the document found to a blog object and return to the client

           Blog blog = documentToBlog(result);
      responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
           responseObserver.onCompleted();
           System.out.println("Response sent");
       }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("Received an update blog request");
        Blog blog = request.getBlog();
        String blogId = blog.getId();
        System.out.println("Search for the desired blog to update...");
        Document result = null;
        try {
            result = collection.find(eq("_id", new ObjectId(blogId))).first();
        } catch (Exception e){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("The Blog with the ID "+blogId+ " was not found")
                    .augmentDescription(e.getLocalizedMessage())
                    .asRuntimeException());

        }
        if(result == null){
            System.out.println("Blog to update not found");
            responseObserver.onNext(UpdateBlogResponse.newBuilder().setStatus("Update failed!!!").build());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("The Blog with the ID "+blogId+ " was not found")
                    .asRuntimeException());
        } else {
            // create a replacement object
            Document replacement = new Document("author_id",blog.getAuthorId())
                .append("title",blog.getTitle())
                    .append("content", blog.getContent())
                    .append("date_created",blog.getDateCreated())
                            .append("_id", new ObjectId(blogId));

            System.out.println("Replacing log in the database");
            collection.replaceOne(eq("_id",result.getObjectId("_id")), replacement);
            System.out.println("blog replaced: Sending as a response ...");
            responseObserver.onNext(UpdateBlogResponse.newBuilder()
                    .setBlog(documentToBlog(replacement)).setStatus("Update Successful!").build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("Received Blog Delete Request");
        String blogId = request.getBlogId();
        DeleteResult result = null;

        try {
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e){
            System.out.println("Blog not found");
            responseObserver.onNext(DeleteBlogResponse.newBuilder().setStatus("Delete Failed").build());
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Blog with the Id " +blogId + " doesn't exist from exception")
                            .asRuntimeException());
        }
        if(result.getDeletedCount() == 0){
            System.out.println("Blog not found");
            responseObserver.onNext(DeleteBlogResponse.newBuilder()
                    .setStatus("Delete Failed, no blog with id "+blogId+ " exists").build());
//            responseObserver.onError(
//                    Status.NOT_FOUND
//                            .withDescription("Blog with the Id " +blogId + " doesn't exist"+result.getDeletedCount())
//                            .asRuntimeException());
            responseObserver.onCompleted();
        } else {
            System.out.println("Blog deleted");
            responseObserver.onNext(DeleteBlogResponse.newBuilder()
                    .setStatus("Blog with Id " +blogId+ " successfuly deleted").build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listBlogs(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Received List blogs request ...");
        collection.find().forEach(document -> responseObserver.onNext(
                ListBlogResponse.newBuilder().setBlog(documentToBlog(document)).build()
        ));
        responseObserver.onCompleted();
    }

    private Blog documentToBlog(Document result){
         return  Blog.newBuilder()
           .setAuthorId(result.getString("author_id"))
           .setTitle(result.getString("title"))
           .setContent(result.getString("content"))
           .setDateCreated(result.getString("date_created"))
           .setId(result.getObjectId("_id").toString())
           .build();

    }
}
