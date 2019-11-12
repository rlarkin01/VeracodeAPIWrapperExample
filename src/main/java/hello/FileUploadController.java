package hello;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hello.storage.StorageFileNotFoundException;
import hello.storage.StorageService;

import javax.xml.xpath.XPathExpressionException;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        try {
            String jsResults = Veracode.retrieveResults("TC JavaScript", null);
            String javaResults = Veracode.retrieveResults("TC Java", null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("reports", storageService.loadResults().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/reports/{reportname:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveReport(@PathVariable String reportname) {

        Resource file = storageService.loadAsResource(reportname);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("appName") String appName,
                                   @RequestParam("submissionName") String submitter,
                                   RedirectAttributes redirectAttributes) {

        Path storedPath = storageService.store(file);

        String scanResult = null;
        try {
            String filePath = storedPath.toAbsolutePath().toString();
            scanResult = Veracode.uploadAndScanWithVeracode(filePath, appName, submitter);
        } catch (Exception e) {
            e.printStackTrace();
            scanResult = e.getMessage();
        }

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + " for scanning, result:" + scanResult);

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
