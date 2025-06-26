package io.github.spooki4.ai_image_tagger.controller;

import io.github.spooki4.ai_image_tagger.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

@Controller  // 이 클래스는 웹 페이지를 반환하는 컨트롤러입니다. (@RestController와 다름)
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 루트 URL("/")로 GET 요청이 오면 "index.html" 템플릿을 보여줍니다.
    @GetMapping("/")
    public String listUploadedFiles(Model model) {
        model.addAttribute("images", imageUploadService.findAll());
        return "index";
    }

    // "/upload" URL로 POST 요청(파일 업로드)이 오면 이 메소드가 처리합니다.
    @PostMapping("/upload")
    public String handleImageUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            // 서비스를 호출하여 파일 업로드 및 정보 저장을 수행합니다.
            imageUploadService.uploadAndAnalyze(file);
            // 성공 메시지를 리다이렉트 페이지에 전달합니다.
            redirectAttributes.addFlashAttribute("successMessage", "파일이 성공적으로 업로드되었습니다!");
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            // 스레드 인터럽트 예외가 발생했을 때 현재 스레드의 인터럽트 상태를 복원
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // 실패 메시지를 리다이렉트 페이지에 전달합니다.
            redirectAttributes.addFlashAttribute("errorMessage", "파일 업로드에 실패했습니다: " + e.getMessage());
        }

        // 처리가 끝나면 다시 루트 URL("/")로 리다이렉트합니다.
        return "redirect:/";
    }

    // 저장된 이미지를 웹에서 접근할 수 있도록 URL 매핑
    @GetMapping("/images/{filename:.+}")
    @ResponseBody  // 뷰를 통하지 않고 응답 본문에 직접 데이터 쓰기
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
