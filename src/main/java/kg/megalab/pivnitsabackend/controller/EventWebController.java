package kg.megalab.pivnitsabackend.controller;

import kg.megalab.pivnitsabackend.dto.event.EventResponse;
import kg.megalab.pivnitsabackend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventWebController {

    @Value("${app.mobile.deep-link-prefix:org.example.app://events/}")
    private String deepLinkPrefix;
    @Value("${app.mobile.ios-store:https://apps.apple.com/app/id123456789}")
    private String iosStoreUrl;
    @Value("${app.mobile.android-store:https://play.google.com/store/apps/details?id=com.piv.app}")
    private String androidStoreUrl;

    private final EventService eventService;

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getSharePage(@PathVariable Long id, Model model) {

        EventResponse event = eventService.getEventById(id);

        model.addAttribute("title", event.title());
        model.addAttribute("description", event.description());
        model.addAttribute("imageUrl", event.bannerUrl() != null ? event.bannerUrl() : "");
        model.addAttribute("deepLink", deepLinkPrefix);
        model.addAttribute("iosStore", iosStoreUrl);
        model.addAttribute("androidStore", androidStoreUrl);

        return "event-share";
    }
}
