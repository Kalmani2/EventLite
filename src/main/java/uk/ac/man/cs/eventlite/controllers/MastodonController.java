package uk.ac.man.cs.eventlite.controllers;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import okhttp3.OkHttpClient;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class MastodonController {

    private final MastodonClient client;

    public MastodonController() {

        this.client = new MastodonClient.Builder("mas.to", new OkHttpClient.Builder(), new Gson())
                .accessToken("ZxdWgfW6hsZoS6poNPcmVxXi6S_D0I9tGktvw-xIdGM")
                .useStreamingApi()
                .build();
    }

    @PostMapping("/events/{id}/share")
    public String shareEvent(
            @PathVariable("id") long id,
            @RequestParam(name = "content") String content,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Statuses statuses = new Statuses(client);

            // Post your status/update
            statuses.postStatus(
                    content,       // The content of the post
                    null,          // Not replying to any post
                    new ArrayList<>(), // No media attachments
                    false,         // Not sensitive
                    null           // No CW/spoiler text
            ).execute();

            redirectAttributes.addFlashAttribute("mastodonSuccess", true);
            redirectAttributes.addFlashAttribute("mastodonPost", content);

        } catch (Mastodon4jRequestException e) {
            // Handle Mastodon API exceptions
            redirectAttributes.addFlashAttribute("mastodonError",
                    "Failed to post to Mastodon: " + e.getMessage());
        } catch (Exception e) {
            // Handle all other exceptions
            redirectAttributes.addFlashAttribute("mastodonError",
                    "Unexpected error: " + e.getMessage());
        }

        return "redirect:/events/" + id + "/details";
    }

    public List<Status> getTimelinePosts() {
        try {
            // Create a Timelines instance
            Timelines timelines = new Timelines(client);
            
            // (Option A) Simple direct call:
            // List<Status> statuses = timelines.getHome().execute().getPart();

            // (Option B) Using a Range + Pageable, as in your working snippet:
            Range range = new Range(); 
            Pageable<Status> pageable = timelines.getHome(range).execute();
            List<Status> statuses = pageable.getPart();

            // Sort newest-first by creation date
            statuses.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            // Return either up to 3, or all if fewer than 3
            return (statuses.size() <= 3) ? statuses : statuses.subList(0, 3);

        } catch (Mastodon4jRequestException e) {
            System.err.println("Error fetching Mastodon timeline: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Unexpected error while fetching timeline: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
