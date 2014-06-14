package jp.kugiya.httpsample;

import org.rythmengine.Rythm;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.Verticle;


public class ClientVerticle extends Verticle {

  HttpClient client;

  public void start() {
    container.logger().info("ClientVerticle deployed.");
    client = vertx.createHttpClient();
    client.setHost("ycpi-api.flickr.com").setPort(80);
    vertx.eventBus().registerHandler("webclient_address", this::handleClientEvent);
  }

  void handleClientEvent(Message<String> msg) {
    container.logger().info(Rythm.render("Recieved tag:@tag", msg));
    client.getNow(
        Rythm.render("/services/feeds/photos_public.gne?tags=@message&format=json", msg.body()),
        (HttpClientResponse res) -> {
      container.logger().info(Rythm.render("Got response. status_code=[@status].", res.statusCode()));
      res.bodyHandler((Buffer body) -> {
        String json = body.toString();
        // かっこに囲まれた変なJSONを返すので削る。
        json = json.substring(json.indexOf('(') + 1, json.length() - 1);
        json = json.replaceAll("\\\\'", "'");
        msg.reply(json);
      });
    });
  }
}
