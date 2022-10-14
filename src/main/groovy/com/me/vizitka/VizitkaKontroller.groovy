package com.me.vizitka


import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@Controller
class VizitkaKontroller {

    @GetMapping("/")
    Mono<ResponseEntity> root(ServerHttpRequest serverHttpRequest ) {
        return index(serverHttpRequest)
    }

    @PostMapping("/")
    Mono<ResponseEntity> rootPost(ServerHttpRequest serverHttpRequest) {
        boolean isAdmin = serverHttpRequest.getRemoteAddress().getAddress().getAddress() == [127, 0, 0, 1]
        if (isAdmin) return form(serverHttpRequest)
        else return send404(serverHttpRequest)
    }

    @PostMapping(value = "/", consumes = "multipart/form-data")
    Mono<ResponseEntity> test(@RequestPart String text,
                        @RequestPart FilePart foto,
                        @RequestHeader("Content-Length") long contentLength,
                        ServerHttpRequest serverHttpRequest) {
        //println "total length is " + contentLength
        if (contentLength > 16 * 1024 * 1024) throw new RuntimeException("file is too big")
        //TODO: handle errors better
        //TODO: make sure false Content-Length doesn't break too much
        var newFoto = new byte[contentLength]
        var byteBuffer = ByteBuffer.wrap(newFoto)
        Flux<DataBuffer> dataBuffers = foto.content()
        dataBuffers
                .doOnComplete(() -> {
                    int fileSize = byteBuffer.position()
                    if (fileSize) {
                        Data.foto = Arrays.copyOfRange(byteBuffer.array(), 0, fileSize)
                        //TODO: you are probably forcing users to wait for the file to be saved on disk here
                        //maybe add extra Mono here
//                        try (FileOutputStream fos = new FileOutputStream("public/foto.jpg"), false) {
//                            fos.write(Data.foto, 0, fileSize)
//                        }
                        Files.write(Paths.get("public/foto.jpg"), Data.foto, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING )
                    }
                })
                .subscribe(fragment -> {
                    byteBuffer.put fragment.asByteBuffer()
                    //byteBuffer.put throws java.nio.BufferOverflowException if you feed it too much data
                })

        Data.data = text
        Data.dataToHtml()
        Data.storeData()
        //TODO: make storeData async once you understand how to use reactor

        return index(serverHttpRequest).delaySubscription(dataBuffers)
        //without delaySubscription, webflux will start sending reply without waiting for file upload to end
        //...probably
        //TODO
    }

    @GetMapping("/foto.jpg")
    ResponseEntity<Resource> foto() {

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                //.header("Cache-Control", "max-age=604800") //TODO: add proper caching headers
                .header("Cache-Control", "max-age=0, no-cache, must-revalidate, proxy-revalidate")
                .body(new ByteArrayResource(Data.foto));
    }

    @RequestMapping("**")
    public Mono<ResponseEntity> everythingElse(ServerHttpRequest serverHttpRequest ) {
        return send404(serverHttpRequest)
    }

    @ExceptionHandler(Exception)
    ResponseEntity return500(Exception ex) {
        println ex.toString()
        return ResponseEntity.status(500).body(Data.rootTop + "500 internal server error" + Data.rootBottom)
    }

    Mono<ResponseEntity> send404(ServerHttpRequest serverHttpRequest ) {
        return Mono.just(ResponseEntity.status(404).body(Data.rootTop + "404 not found" + Data.rootBottom))
    }

    Mono<ResponseEntity> index(ServerHttpRequest serverHttpRequest) {
        boolean isAdmin = serverHttpRequest.getRemoteAddress().getAddress().getAddress() == [127, 0, 0, 1]
        String body = Data.rootTop
        body += '<img src="foto.jpg" width="200" height="150"/>'
        body += Data.dataHtml
        if (isAdmin) body += '<form action="/" method="post"><button name="edit" type="submit">Edit</button></form>'
        body += Data.rootBottom
        return Mono.just(ResponseEntity.ok(body))
    }

    Mono<ResponseEntity> form(ServerHttpRequest serverHttpRequest ) {
        boolean isAdmin = serverHttpRequest.getRemoteAddress().getAddress().getAddress() == [127, 0, 0, 1]
        if (!isAdmin) return send404(serverHttpRequest)
        String body = Data.rootTop
        body += Data.formTop
        body += Data.data
        body += Data.formBottom
        body += Data.rootBottom
        return Mono.just(ResponseEntity.ok(body))
    }

}