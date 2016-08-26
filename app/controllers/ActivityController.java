package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.libs.ws.*;
import play.mvc.*;

import javax.inject.Inject;
import java.util.concurrent.*;

/**
 * Created by plab on 24/08/16.
 */
public class ActivityController extends Controller {

    @Inject
    WSClient ws;

    private final String token = "0ab1050b5cd22ca62fed85bea9cc128e4d012a55";
    private final String activitiesAPI = "https://api.pipedrive.com/v1/activities/";
    private final long timeout = 3000;

    public Result addActivity(int dealId) {
        JsonNode requestData = request().body().asJson();

        if (requestData == null)
            return badRequestResult("Não foi possível obter os dados da requisição, utilize Content-Type:application/json.");

        requestData = ((ObjectNode)requestData).put("deal_id", dealId);
        return requestWithBody(activitiesAPI, "POST", requestData);
    }

    public CompletionStage<Result> getActivity(int id) {
        WSRequest request = createRequest( activitiesAPI + id, "GET" );

        return request.execute().thenApply(response ->
                status(response.getStatus() , response.asJson())
        );
    }

    public CompletionStage<Result> deleteActivity(int id) {
        WSRequest request = createRequest( activitiesAPI + id, "DELETE" );

        return request.execute().thenApply(response ->
                status(response.getStatus() , response.asJson())
        );
    }

    public Result updateActivity(int id) {
        JsonNode requestData = request().body().asJson();

        if (requestData == null)
            return badRequestResult("Não foi possível obter os dados da requisição, utilize Content-Type:application/json.");

        return requestWithBody(activitiesAPI+id, "PUT", requestData);
    }

    private WSRequest createRequest(String url, String method) {
        WSRequest request = ws.url(url);
        request.setQueryParameter("api_token", token);
        request.setRequestTimeout(timeout);
        request.setMethod(method);

        return request;
    }

    private Result requestWithBody(String url, String method, JsonNode data) {
        WSRequest request = createRequest(url, method);
        request.setBody(data);

        CompletionStage<Result> promise = request.execute().thenApply(response ->
                status(response.getStatus() , response.asJson())
        );

        return promise.toCompletableFuture().join();
    }

    private Result badRequestResult(String message) {
        JsonNode resultData = Json.newObject()
                                .put("success", false)
                                .put("message", message);

        return badRequest(resultData);
    }
}
