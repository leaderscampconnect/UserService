package com.campconnect.userservice.feign;

import com.campconnect.userservice.dto.CampSiteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "api-camping", path = "/api")
public interface CampSiteClient {

    @GetMapping("/site-camping")
    List<CampSiteResponse> getAllCampSites();

    @GetMapping("/site-camping/{id}")
    CampSiteResponse getCampSiteById(@PathVariable("id") Long id);
}