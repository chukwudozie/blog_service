package blog.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
           Blog blog = Blog.newBuilder()
                   .setAuthorId(result.getString("author_id"))
                   .setTitle(result.getString("title"))
                   .setContent(result.getString("content"))
                   .setDateCreated(result.getString("date_created"))
                   .setId(blogId)
                   .build();

           responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
           responseObserver.onCompleted();
           System.out.println("Response sent");
       }
    }
}
