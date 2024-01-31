package subway.line.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import subway.line.dto.request.CreateLineRequest;
import subway.line.dto.response.LineResponse;
import subway.line.service.LineService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/lines")
public class LineController {

    private final LineService lineService;

    @PostMapping
    public ResponseEntity<LineResponse> createLine(@RequestBody CreateLineRequest request) {
        LineResponse lineResponse = lineService.creteLine(request);
        return ResponseEntity.created(URI.create("/lines/" + lineResponse.getId())).body(lineResponse);
    }

    @GetMapping
    public ResponseEntity<List<LineResponse>> getLines() {
        return ResponseEntity.ok().body(lineService.getLines());
    }

    @GetMapping("/{lineId}")
    public ResponseEntity<LineResponse> getLineById(@PathVariable Long lineId) {

        LineResponse lineResponse = lineService.getLineById(lineId);

        return ResponseEntity.ok().body(lineResponse);
    }
}
