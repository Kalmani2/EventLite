package uk.ac.man.cs.eventlite.controllers;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Handler;
import com.sys1yagi.mastodon4j.api.Shutdownable;
import com.sys1yagi.mastodon4j.api.entity.Notification;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Streaming;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;

import java.util.concurrent.TimeUnit;

@Controller
public class MastodonController {

    private final MastodonClient client;

    public MastodonController() {
        // Create MastodonClient with hardcoded access token
        this.client = new MastodonClient.Builder(
            "mstdn.jp",
            new OkHttpClient.Builder(),
            new Gson()
        )
        .accessToken("ZiVgrWT06shZUPoOWkZD70cf7jRU7AAkqmUGrixVGN4")
        .useStreamingApi()
        .build();
    }

    public static void main(String[] args) {
        // require authentication even if public streaming
        String accessToken = "ZiVgrWT06shZUPoOWkZD70cf7jRU7AAkqmUGrixVGN4";

        MastodonClient client = new MastodonClient.Builder("mstdn.jp", new OkHttpClient.Builder(), new Gson())
                .accessToken(accessToken)
                .useStreamingApi()
                .build();
        Handler handler = new Handler() {
            @Override
            public void onStatus(@NotNull Status status) {
                System.out.println(status.getContent());
            }

            @Override
            public void onNotification(@NotNull Notification notification) {

            }

            @Override
            public void onDelete(long id) {

            }
        };
        Streaming streaming = new Streaming(client);
        try {
            Shutdownable shutdownable = streaming.localPublic(handler);
            Thread.sleep(10000L);
            shutdownable.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}