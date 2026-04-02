package com.example.Gold_Frontend.advice;

import com.example.Gold_Frontend.model.Breadcrumb;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalBreadcrumbAdvice {

    @ModelAttribute("breadcrumbs")
    public List<Breadcrumb> populateBreadcrumbs(HttpServletRequest request) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb("Home", "/"));

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (path != null) {
            String[] parts = path.split("/");
            String url = "";
            for (int i = 1; i < parts.length; i++) {
                url += "/" + parts[i];
                String name = capitalize(parts[i]);
                // Last part = current page → url=null
                if (i == parts.length - 1) {
                    breadcrumbs.add(new Breadcrumb(name, null));
                } else {
                    breadcrumbs.add(new Breadcrumb(name, url));
                }
            }
        }
        return breadcrumbs;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).replace("-", " ");
    }
}