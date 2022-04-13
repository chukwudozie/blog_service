package blog.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;

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
}
