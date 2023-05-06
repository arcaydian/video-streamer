package com.video.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.video.service.StreamService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/video")
public class StreamController {

    @Autowired
    private StreamService streamService;

    @GetMapping("")
    public String appInfo() {
        return "Rate limited application with intentional buffering in the video";
    }


    @GetMapping("/{fileName}")
    public Mono < ResponseEntity < byte[] >> streamVideo(@RequestHeader(value = "Range", required = false) String httpRangeList,
        @PathVariable("fileName") String fileName) {
        return Mono.just(streamService.streamVideoInChunks(fileName, httpRangeList));
    }
}
