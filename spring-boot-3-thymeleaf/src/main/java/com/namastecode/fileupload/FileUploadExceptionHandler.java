package com.namastecode.fileupload;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class FileUploadExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public String handleSizeExceeded(RedirectAttributes attrs) {
        attrs.addFlashAttribute("message",
                "File exceeds size limit!");
        return "redirect:/";
    }
}