package com.namastecode.fileupload;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;

@ControllerAdvice
public class FileUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleSizeExceeded(Model model, MaxUploadSizeExceededException exception) {
        model.addAttribute("message",
                "File exceeds size limit!");
        return "file-upload";
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMultiPartError(Model model, MultipartException e) {
        model.addAttribute("message", e.getMessage());
        return "file-upload";
    }


    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIOError(Model model, IOException e) {
        model.addAttribute("message",
                "Storage error. Try again.");
        return "file-upload";
    }
}