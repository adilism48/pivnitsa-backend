package kg.megalab.pivnitsabackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import kg.megalab.pivnitsabackend.dto.event.EventResponse;
import kg.megalab.pivnitsabackend.exception.EventNotFoundException;
import kg.megalab.pivnitsabackend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventWebController {

    @Value("${app.mobile.deep-link-prefix}")
    private String deepLinkPrefix;
    @Value("${app.mobile.ios-store}")
    private String iosStoreUrl;
    @Value("${app.mobile.android-store}")
    private String androidStoreUrl;
    @Value("${app.mobile.canonicalUrl}")
    private String canonicalUrl;

    private final EventService eventService;


    @GetMapping(value = "/share/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getSharePage(@PathVariable Long id,
                               @RequestHeader(value = "User-Agent", required = false) String userAgent,
                               Model model) {

        EventResponse event = eventService.getEventById(id);

        model.addAttribute("title", event.title());
        model.addAttribute("description", event.description());
        model.addAttribute("imageUrl", event.bannerUrl() != null ? event.bannerUrl() : "");
        model.addAttribute("deepLink", deepLinkPrefix + id);
        model.addAttribute("storeUrl", resolveStoreUrl(userAgent));
        model.addAttribute("canonicalUrl", canonicalUrl + id);
        model.addAttribute("eventFound", true);

        return "event-share";
    }

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model, HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");

        model.addAttribute("title", "Мероприятие не найдено");
        model.addAttribute("description", "Возможно, оно было удалено или ссылка устарела");
        model.addAttribute("imageUrl", "");
        model.addAttribute("storeUrl", resolveStoreUrl(userAgent));
        model.addAttribute("canonicalUrl", "");
        model.addAttribute("eventFound", false);

        return "event-share";
    }

    private String resolveStoreUrl(String userAgent) {
        if (userAgent == null) return androidStoreUrl;
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return iosStoreUrl;
        }
        return androidStoreUrl;
    }
}
