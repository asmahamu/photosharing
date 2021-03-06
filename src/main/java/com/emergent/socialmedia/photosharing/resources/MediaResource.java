package com.emergent.socialmedia.photosharing.resources;

import com.emergent.socialmedia.photosharing.resources.dto.request.CommentsRequestDTO;
import com.emergent.socialmedia.photosharing.resources.dto.response.ExceptionResponseContainerDTO;
import com.emergent.socialmedia.photosharing.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@RestController
@RequestMapping(value = MediaResource.REST_USERID_ENDPOINT_PREFIX)
public class MediaResource {

    @Autowired
    private MediaService mediaService;

    public static final String REST_USERID_ENDPOINT_PREFIX = "/{user_id}";
    public static final String REST_GET_FEED_ENDPOINT = "/feed";
    public static final String REST_MEDIA = "/media/";
    public static final String REST_MEDIA_ACTION = REST_MEDIA+"{media_id}";
    public static final String REST_LIKE_DISLIKE_ENDPOINT = REST_MEDIA_ACTION+"/like"; // PUT, DELETE
    public static final String REST_GET_ALL_CREATE_COMMENT_BY_USER_ENDPOINT = REST_MEDIA_ACTION+"/comment"; // POST
    public static final String REST_DELETE_COMMENT_ENDPOINT = REST_MEDIA_ACTION+"/comment/{comment_id}"; // DELETE
    public static final String REST_GET_LIKED_MEDIA = "/liked/";
    public static final String REST_GET_COMMENTED_MEDIA = "/commented/";


    @PostMapping(path=REST_MEDIA,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> fileUpload(@NotBlank @PathVariable(name="user_id") Long userId,
                                             @RequestParam("file") MultipartFile file){
            return ResponseEntity.ok(mediaService.storeMedia(userId, file));
    }

    @GetMapping(path=REST_MEDIA_ACTION)
    public ResponseEntity<Object> fileDownload(@NotBlank @PathVariable(name="media_id") Long mediaId){
        Resource resource = mediaService.getMedia(mediaId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping(path=REST_GET_FEED_ENDPOINT,
                produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> getPhotoFeedWithAfterAndLimit(@NotBlank @PathVariable(name="user_id") Long userId,
                                                                @RequestParam(value = "after", required = false) Long after,
                                                                @RequestParam(value = "limit", required = false) Integer limit){
        return ResponseEntity.ok(mediaService.getAllMediaOrderByCreatedAtDesc(userId, after, limit));
    }

    @PutMapping(path=REST_LIKE_DISLIKE_ENDPOINT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> like(@NotBlank @PathVariable(name="user_id") Long userId,
                                       @NotBlank @PathVariable(name="media_id") Long mediaId){
        return ResponseEntity.ok(mediaService.like(userId, mediaId));
    }
    @DeleteMapping(path=REST_LIKE_DISLIKE_ENDPOINT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> dislike(@NotBlank @PathVariable(name="user_id") Long userId,
                                          @NotBlank @PathVariable(name="media_id") Long mediaId){
        return ResponseEntity.ok(mediaService.dislike(userId, mediaId));
    }

    @GetMapping(path=REST_GET_ALL_CREATE_COMMENT_BY_USER_ENDPOINT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> getComments(@NotBlank @PathVariable(name="user_id") Long userId,
                                              @NotBlank @PathVariable(name="media_id") Long mediaId,
                                              @RequestParam(value = "after", required = false) Long after,
                                              @RequestParam(value = "limit", required = false) Integer limit){
        return ResponseEntity.ok(mediaService.getAllCommentsOrderByCreatedAtDesc(userId, mediaId, after, limit));
    }
    @PostMapping(path=REST_GET_ALL_CREATE_COMMENT_BY_USER_ENDPOINT,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> comment(@NotBlank @PathVariable(name="user_id") Long userId,
                                          @NotBlank @PathVariable(name="media_id") Long mediaId,
                                          @Valid @RequestBody CommentsRequestDTO CommentsRequestDTO){
        return ResponseEntity.ok(mediaService.comment(userId, mediaId, CommentsRequestDTO.getComment()));
    }

    @DeleteMapping(path=REST_DELETE_COMMENT_ENDPOINT,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> uncomment(@NotBlank @PathVariable(name="user_id") Long userId,
                                            @NotBlank @PathVariable(name="media_id") Long mediaId,
                                            @NotBlank @PathVariable(name="comment_id") Long commentId){
        return ResponseEntity.ok(mediaService.uncomment(userId, mediaId, commentId));
    }

    @GetMapping(path=REST_GET_LIKED_MEDIA, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> getAllPhotosLikedByUserId(@NotBlank @PathVariable(name="user_id") Long userId){
        return ResponseEntity.ok(mediaService.getAllMediaLikedByUserId(userId));
    }

    @GetMapping(path=REST_GET_COMMENTED_MEDIA, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> getAllPhotosCommentedByUserId(@NotBlank @PathVariable(name="user_id") Long userId){
        return ResponseEntity.ok(mediaService.getAllMediaCommentedByUserId(userId));
    }
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponseContainerDTO> handleException(MethodArgumentNotValidException exception) {

        String errorMsg = exception.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(exception.getMessage());
        ExceptionResponseContainerDTO exceptionResponse = new ExceptionResponseContainerDTO(new Date(), "Request Validation error",
                errorMsg);

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
