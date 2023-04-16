package org.stargate.rest.json;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.Response;
import javax.inject.Inject;
import java.util.Map;
import java.util.Collections;

import io.stargate.bridge.proto.QueryOuterClass;
import io.stargate.sgv2.api.common.cql.builder.QueryBuilder;
import io.stargate.sgv2.api.common.cql.builder.Replication;
import io.stargate.sgv2.api.common.grpc.StargateBridgeClient;

// add authorization
@ApplicationScoped
@SecurityRequirement(name = "Token")

@Path("/kvstore/v1")
public class KeyValueResource {
  @Inject
  StargateBridgeClient bridge;
  @Inject
  KVCassandra kvcassandra;
  ObjectMapper objectMapper = new ObjectMapper();

  public KeyValueResource() {
  }

  // create and delete databases
  /**
   * @param db_name_json
   * @return
   * @throws KvstoreException
   * @throws JsonProcessingException
   */
  @POST
  @Path("databases")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public KVResponse createDB(String db_name_json) throws KvstoreException, JsonProcessingException {
    JsonNode jsonNode = objectMapper.readTree(db_name_json);
    String db_name = jsonNode.get("db_name").asText();
    KVResponse resonse = kvcassandra.createKeyspace(db_name);
    return resonse;
  }

  /**
   * @param db_name
   * @return
   * @throws KvstoreException
   */
  @DELETE
  @Path("{db_name}")
  public KVResponse deleteDB(@PathParam("db_name") String db_name) throws KvstoreException {
    if (db_name == null) {
      // throw new KvstoreException(400, "bad request");
      return new KVResponse(400, "db_name should not be null");
    }
    KVResponse response = kvcassandra.deleteKeyspace(db_name);
    return response;
  }

  /**
   * @param db_name
   * @param table_name_json
   * @return
   * @throws KvstoreException
   * @throws JsonProcessingException
   */
  @POST
  @Path("databases/{db_name}/tables")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public KVResponse createTable(@PathParam("db_name") String db_name, String table_name_json)
      throws KvstoreException, JsonProcessingException {
    JsonNode jsonNode = objectMapper.readTree(table_name_json);
    String table_name = jsonNode.get("table_name").asText();
    KVResponse response = kvcassandra.createTable(db_name, table_name);
    return response;
  }

  /**
   * @param db_name
   * @param table_name
   * @return
   * @throws KvstoreException
   */
  @DELETE
  @Path("{db_name}/{table_name}")
  public KVResponse deleteTable(@PathParam("db_name") String db_name, @PathParam("table_name") String table_name)
      throws KvstoreException {
    if (db_name == null || table_name == null) {
      throw new KvstoreException(400, "bad request");
    }
    KVResponse response = kvcassandra.deleteTable(db_name, table_name);
    return response;
  }

  /**
   * @return
   * @throws KvstoreException
   */
  @GET
  @Path("databases")
  @Produces(MediaType.APPLICATION_JSON)
  public KVResponse listDBs() throws KvstoreException {
    KVResponse response = kvcassandra.listKeyspaces();
    return response;
  }

  /**
   * @param db_name
   * @return
   * @throws KvstoreException
   */
  @GET
  @Path("{db_name}/tables")
  @Produces(MediaType.APPLICATION_JSON)
  public KVResponse listTables(@PathParam("db_name") String db_name) throws KvstoreException {
    if (db_name == null) {
      throw new KvstoreException(400, "bad request");
    }
    KVResponse response = kvcassandra.listTables(db_name);
    return response;
  }

  /**
   * @param db_name
   * @param table_name
   * @param kvPair
   * @return
   * @throws KvstoreException
   */
  @PUT
  @Path("{db_name}/{table_name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public KVResponse putKeyVal(@PathParam("db_name") String db_name,
      @PathParam("table_name") String table_name, KeyValPair kvPair) throws KvstoreException {
    if (db_name == null || table_name == null || kvPair == null || kvPair.key == null || kvPair.value == null) {
      throw new KvstoreException(400, "bad request");
    }
    KVResponse response = kvcassandra.putKeyVal(db_name, table_name, kvPair.key, kvPair.value);
    return response;
  }

  /**
   * @param db_name
   * @param table_name
   * @param kvPair
   * @return
   * @throws KvstoreException
   */
  @GET
  @Path("{db_name}/{table_name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public KVResponse getKeyVal(@PathParam("db_name") String db_name,
      @PathParam("table_name") String table_name, KeyValPair kvPair) throws KvstoreException {
    if (db_name == null || table_name == null || kvPair == null || kvPair.key == null) {
      throw new KvstoreException(400, "bad request");
    }
    KVResponse response = kvcassandra.getVal(db_name, table_name, kvPair.key);
    return response;
  }

  /**
   * @param db_name
   * @param table_name
   * @param kvPair
   * @return
   * @throws KvstoreException
   */
  @PATCH
  @Path("{db_name}/{table_name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public KVResponse updateKeyVal(@PathParam("db_name") String db_name,
      @PathParam("table_name") String table_name, KeyValPair kvPair) throws KvstoreException {
    if (db_name == null || table_name == null || kvPair == null || kvPair.key == null || kvPair.value == null) {
      throw new KvstoreException(400, "bad request");
    }
    // m_kvservice.putKeyVal(db_id, kvPair.key, kvPair.value, true);
    KVResponse response = kvcassandra.updateVal(db_name, table_name, kvPair.key, kvPair.value);
    return response;
  }

  /**
   * @param db_name
   * @param table_name
   * @param kvPair
   * @return
   * @throws KvstoreException
   */
  @DELETE
  @Path("{db_name}/{table_name}/key")
  @Consumes(MediaType.APPLICATION_JSON)
  public KVResponse deleteKey(@PathParam("db_name") String db_name,
      @PathParam("table_name") String table_name, KeyValPair kvPair) throws KvstoreException {
    if (db_name == null || table_name == null || kvPair == null || kvPair.key == null || kvPair.value != null) {
      throw new KvstoreException(400, "bad request");
    }
    // m_kvservice.deleteKey(db_id, kvPair.key);
    KVResponse response = kvcassandra.deleteKey(db_name, table_name, kvPair.key);
    return response;
  }

}

