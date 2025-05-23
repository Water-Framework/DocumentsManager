package it.water.documents.manager.api.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.RestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.documents.manager.model.Document;
import it.water.documents.manager.model.Folder;
import it.water.service.rest.api.security.LoggedIn;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * @Generated by Water Generator
 * Rest Api Interface for DocumentsManager entity.
 * This interfaces exposes all CRUD methods with default JAXRS annotations.
 */
@Path("/documents")
@Api(produces = MediaType.APPLICATION_JSON, tags = "DocumentsManager API")
@FrameworkRestApi
public interface DocumentsManagerRestApi extends RestApi {

    @LoggedIn
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "Document Save API", httpMethod = "POST", produces = MediaType.APPLICATION_JSON, consumes = MediaType.MULTIPART_FORM_DATA)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    Document save(@Multipart(Document.DOCUMENT_ENTITY_HTTP_PART_NAME) Document document, @Multipart(Document.DOCUMENT_CONTENT_HTTP_PART_NAME) InputStream file);

    @LoggedIn
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "Document Update API", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON, consumes = MediaType.MULTIPART_FORM_DATA)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    Document update(@Multipart(Document.DOCUMENT_ENTITY_HTTP_PART_NAME) Document document, @Multipart(value = Document.DOCUMENT_CONTENT_HTTP_PART_NAME, required = false) InputStream file);


    @LoggedIn
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/{id}", notes = "Document Find API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    Document find(@PathParam("id") long id);

    @LoggedIn
    @Path("/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/content", notes = "Fetch Document Content API", httpMethod = "GET", produces = MediaType.APPLICATION_OCTET_STREAM)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
        //returning Object to be cross framework and cross technology
    Object fetchContent(@QueryParam("path") String path, @QueryParam("fileName") String fileName);

    @LoggedIn
    @Path("/content/id/{documentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/content/id/{documentId}", notes = "Fetch Document Content API using document id", httpMethod = "GET", produces = MediaType.APPLICATION_OCTET_STREAM)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
        //returning Object to be cross framework and cross technology
    Object fetchContent(@PathParam("documentId") long documentId);

    @LoggedIn
    @Path("/content/uid/{documentUID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/content/uid/{documentUID}", notes = "Fetch Document Content API using document UID", httpMethod = "GET", produces = MediaType.APPLICATION_OCTET_STREAM)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
        //returning Object to be cross framework and cross technology
    Object fetchContent(@PathParam("documentUID") String documentUID);


    @LoggedIn
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "Document Find All API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    PaginableResult<Document> findAll();


    @LoggedIn
    @Path("/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/{id}", notes = "Document Delete API", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    void remove(@PathParam("id") long id);

    @LoggedIn
    @Path("/folders")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "Folder Save API", httpMethod = "POST", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    Folder saveFolder(Folder folder);


    @LoggedIn
    @PUT
    @Path("/folders")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "Folder Update API", httpMethod = "PUT", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    Folder updateFolder(Folder folder);


    @LoggedIn
    @Path("/folders/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/{id}", notes = "Folder Find API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    Folder findFolder(@PathParam("id") long id);


    @LoggedIn
    @GET
    @Path("/folders")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/", notes = "Folder Find All API", httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    PaginableResult<Folder> findAllFolders();


    @LoggedIn
    @Path("/folders/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/{id}", notes = "Folder Delete API", httpMethod = "DELETE", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Not authorized"),
            @ApiResponse(code = 409, message = "Validation Failed"),
            @ApiResponse(code = 422, message = "Duplicated Entity"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    void removeFolder(@PathParam("id") long id);
}
