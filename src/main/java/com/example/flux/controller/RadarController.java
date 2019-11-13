package com.example.flux.controller;

import com.example.flux.model.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
public class RadarController {

    private static final Logger log = LoggerFactory.getLogger(RadarController.class);

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ResponseEntity<Flux<Place>> getPlaces(@RequestParam("lat_pos") Double latPos,
                                                 @RequestParam("lon_pos") Double lonPos,
                                                 @RequestParam("lat_ref") Optional<Double> latRef,
                                                 @RequestParam("lon_ref") Optional<Double> lonRef,
                                                 @RequestParam("keywords") Optional<String> keywords,
                                                 @RequestParam(value = "radius", defaultValue = "1000") Integer radius,
                                                 @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                                 @RequestParam(value = "limit", defaultValue = "25") Integer limit,
                                                 Principal principal) {

        Map<String, Object> parameters = Stream.of(new Object[][]{
                {"lat_pos", latPos},
                {"lon_pos", lonPos},
                {"lat_ref", latRef},
                {"lon_ref", lonRef},
                {"keywords", keywords},
                {"radius", radius},
                {"offset", offset},
                {"limit", limit}
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]));

        log.debug("RadarController [getPlaces] endpoint has been called",
                kv("path", "/"),
                kv("method", HttpMethod.GET),
                kv("params", parameters)
        );

        return ResponseEntity.ok().body(findByLatLonAndKeywords(latPos, lonPos, latRef.orElse(latPos), lonRef.orElse(lonPos),
                keywords.map(isPresent -> Optional.of(isPresent.split(","))).orElse(Optional.empty()),
                radius, offset, limit));
    }

    private Flux<Place> findByLatLonAndKeywords(Double latPos, Double lonPos, Double latRef, Double lonRef,
                                                Optional<String[]> keywords, Integer radius, Integer offset,
                                                Integer limit) {
        return Mono.<List<Place>>create(sink -> {
            // Create a CompletableFuture
            CompletableFuture<Place> placeAsync = CompletableFuture.supplyAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } finally {
                    Place place = new Place();
                    place.setId("1");
                    place.setName("Place Async");
                    return place;
                }
            });

            // Attach a callback to the Future using thenApply()
            placeAsync.thenAccept(place -> sink.success(Arrays.asList(place)));
        })
        .flatMapMany(hits -> Flux.fromIterable(hits))
        .doOnNext(result -> log.debug("[findByLatLonAndKeywords] found place: {}", kv("data", result)));
    }

}
