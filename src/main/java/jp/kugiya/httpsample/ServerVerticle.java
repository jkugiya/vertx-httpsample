package jp.kugiya.httpsample;

import org.rythmengine.Rythm;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;


public class ServerVerticle extends Verticle {

  public void start() {
    container.deployVerticle(ClientVerticle.class.getCanonicalName());
    HttpServer server = vertx.createHttpServer();
    RouteMatcher routeMatcher = new RouteMatcher();
    routeMatcher.get("/tag/:theTag", this::handleTag);
    routeMatcher.getWithRegEx(".*", this::handleOthers);
    server.requestHandler(routeMatcher).listen(8080);
  }

  private void handleTag(HttpServerRequest req) {
    String tag = req.params().get("theTag");
    container.logger().info(Rythm.render("Looking for:@searchWord", tag));
    vertx.eventBus().send("webclient_address", tag, (Message<String> result) -> {
      String json = result.body();
      JsonObject jsonObj = new JsonObject(json);
      String html = Rythm.render(PAGE_TEMPLATE, tag, jsonObj.getArray("items"));
      req.response().end(html);
    });
  }

  private void handleOthers(HttpServerRequest req) {
    req.response().end("ERROR: fill the tag.");
  }

  private static final String PAGE_TEMPLATE =
      "@import org.vertx.java.core.json.JsonArray\n"
      + "@import org.vertx.java.core.json.JsonObject\n"
      + "@args String searchWord, JsonArray items\n"
      + "<html><h1>Result for @searchWord </h1>\n"
      + "@for(items) {\n"
      + "@{ JsonObject item = (JsonObject)_; }\n"
      + "<h3>@item.getString(\"title\")</h3>\n"
      + "<a href=\"@item.getString(\"link\")\">"
      + "<img src=\"@item.getObject(\"media\").getString(\"m\")\" />"
      + "</a>\n"
      + "}\n" + "</html>";
}
